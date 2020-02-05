#!/usr/bin/python
import os, sys, traceback
import urllib, urllib2, base64
import json, xml.dom.minidom
import re

REMOTE_BASE_URL = os.environ['SONAR_REMOTE_BASE_URL']
LOCAL_BASE_URL = os.environ['SONAR_LOCAL_BASE_URL']
LOCAL_SERVER_USER = os.environ['SONAR_LOCAL_USER']
LOCAL_SERVER_PASSWD = os.environ['SONAR_LOCAL_PASSWD']
SONAR_TMP_SYNC = os.environ['SONAR_TMP_SYNC']

local_base64credentials = base64.b64encode('%s:%s' % (LOCAL_SERVER_USER, LOCAL_SERVER_PASSWD))

HIGHLIGHT="\x1b[6;30;42m"
HIGHLIGHT_END="\x1b[0m"

JACOCO_PROPERTY = 'sonar.jacoco.reportMissing.force.zero'

url_remote_server_index=REMOTE_BASE_URL+"/api/system/status"
url_remote_profile_list=REMOTE_BASE_URL+"/api/qualityprofiles/search"
url_remote_list_qualitygates=REMOTE_BASE_URL+"/api/qualitygates/list"
url_remote_profile_backup=REMOTE_BASE_URL+"/api/qualityprofiles/backup"
url_remote_show_qualitygate=REMOTE_BASE_URL+"/api/qualitygates/show"

url_local_auth_validate=LOCAL_BASE_URL+"/api/authentication/validate"
url_local_properties=LOCAL_BASE_URL+"/api/properties"
url_local_profile_restore=LOCAL_BASE_URL+"/api/qualityprofiles/restore"
url_local_profile_default=LOCAL_BASE_URL+"/api/qualityprofiles/set_default"
url_local_list_qualitygates=LOCAL_BASE_URL+'/api/qualitygates/list'
url_local_show_qualitygate=LOCAL_BASE_URL+'/api/qualitygates/show'
url_local_unsetdef_qualitygate=LOCAL_BASE_URL+'/api/qualitygates/unset_default'
url_local_setdef_qualitygate=LOCAL_BASE_URL+'/api/qualitygates/set_as_default'
url_local_delete_condition=LOCAL_BASE_URL+"/api/qualitygates/delete_condition"
url_local_create_qualitygate=LOCAL_BASE_URL+"/api/qualitygates/create"
url_local_create_condition=LOCAL_BASE_URL+"/api/qualitygates/create_condition"

dir_profiles= SONAR_TMP_SYNC + '/profiles'
dir_qgates= SONAR_TMP_SYNC + '/qgates'
flocalqgates = SONAR_TMP_SYNC + "/local-sonar-quality_gates.json"

fprofiles = SONAR_TMP_SYNC + "/sonar-profiles.json"
fqgates = SONAR_TMP_SYNC + "/sonar-quality_gates.json"

def formatJSON(filename):
    with open(filename) as json_file:
        data = json.load(json_file)

    with open(filename, 'w') as outfile:
        json.dump(data, outfile, sort_keys=True, indent=4)

def dumpJSONStream(stream, filename):
    data = stream.read()
    with open(filename, 'w') as outfile:
        outfile.write(data)
    formatJSON(filename)

def profileDumpStream(stream, filename):
    data = stream.read()
    xml_obj = xml.dom.minidom.parseString(data)
    pretty_xml_as_string = xml_obj.toprettyxml().encode('utf-8').strip()
    with open(filename, 'w') as xml_file:
        xml_file.write(pretty_xml_as_string)

#===================================================================================
def getText(node):
    nodelist = node.childNodes
    rc = []
    for node in nodelist:
        if node.nodeType == node.TEXT_NODE:
            rc.append(node.data)
    return ''.join(rc)

def existqgate(name):
    exist = False
    with open(flocalqgates) as json_file:
        localqgates = json.load(json_file)

        for qgate in localqgates['qualitygates']:
            sname = qgate['name'].encode('utf-8').strip()
            if name == sname:
                exist = True
                break

    json_file.close()
    return exist

def existqgateid(name):
    localid = None
    with open(flocalqgates) as json_file:
        localqgates = json.load(json_file)

        for qgate in localqgates['qualitygates']:
            sname = qgate['name'].encode('utf-8').strip()
            if name == sname:
                localid = str(qgate['id'])
                break

    json_file.close()
    return localid

def satinizeqgname(name):
    return re.sub('[^0-9a-zA-Z]+', '_', name)
def getremoteqgatefile(qgname):
    sname_satinized = satinizeqgname(qgname)
    fqgate = None
    for filename in os.listdir(dir_qgates):
        if filename.endswith(sname_satinized+".json"):
            fqgate=os.path.join(dir_qgates, filename)
            with open(fqgate) as json_file:
                remotegate = json.load(json_file)
                sname = remotegate['name'].encode('utf-8').strip()
                if qgname == sname:
                    break
                else:
                    fqgate=None
            json_file.close()

    return fqgate

def existremotecondition(qgname, cmetric, cop):
    exist = False
    fqgate = getremoteqgatefile(qgname)

    with open(fqgate) as json_file:
        remotegate = json.load(json_file)

        for condition in remotegate['conditions']:
            metric = condition['metric'].encode('utf-8').strip()
            op = condition['op'].encode('utf-8').strip()
            if cmetric == metric and cop == op:
                exist = True
                break

    json_file.close()
    return exist
def traceremoteerror(ex):
    with open(SONAR_TMP_SYNC + '/remote_error', 'w') as localFile:
        localFile.write(str(ex))
        localFile.write(str(sys.exc_info()[0]))
        traceback.print_exc(file=localFile)
        print "Unexpected error:", sys.exc_info()[0]
        print "Unexpected error:", ex
        traceback.print_exc(file=sys.stdout)

def tracelocalerror(ex):
    with open(SONAR_TMP_SYNC + '/local_error', 'w') as localFile:
        localFile.write(str(ex))
        localFile.write(str(sys.exc_info()[0]))
        traceback.print_exc(file=localFile)
        print "Unexpected error:", sys.exc_info()[0]
        print "Unexpected error:", ex
        traceback.print_exc(file=sys.stdout)
        print

        print(traceback.format_exc())
