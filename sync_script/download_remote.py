#!/usr/bin/python
import os, sys
import urllib, urllib2
import json, xml.dom.minidom
import globaldef
from globaldef import traceremoteerror

try:
    response = urllib2.urlopen(globaldef.url_remote_server_index)
    data_server = json.load(response)
except Exception as ex:
    traceremoteerror(ex)
    exit(1)

if  (data_server['status'] == 'UP' or data_server['status'] == 'SETUP'):
    print("SONAR remote server status is ==== '{}'!".format(data_server['status']))
else:
    print("SONAR remote server status is ==== '{}'!".format(data_server['status']))
    with open(SONAR_TMP_SYNC + '/remote_no_up', 'w') as remotefile:
        json.dump(data_server, remotefile)
    exit(1)

print('===============================================')

#DOWNLOAD PROFILES
try:
    response = urllib2.urlopen(globaldef.url_remote_profile_list)
    globaldef.dumpJSONStream(response, globaldef.fprofiles)
    print("\tQUALITY PROFILES DOWNLOADED {0}".format(globaldef.fprofiles))
except Exception as ex:
    traceremoteerror(ex)
    exit(1)

#DOWNLOAD QALITY GATES
try:
    response = urllib2.urlopen(globaldef.url_remote_list_qualitygates)
    globaldef.dumpJSONStream(response, globaldef.fqgates)
    print("\tQUALITY GATES DOWNLOADED {0}".format(globaldef.fqgates))
except Exception as ex:
    traceremoteerror(ex)
    exit(1)

with open(globaldef.fprofiles) as json_file:
    pdata = json.load(json_file)
    for idx, profile in enumerate(pdata["profiles"]):
        # for key, name, language, languageName, isInherited, isDefault in profile.iteritems():
        isBuiltIn = profile['isBuiltIn']
        if isBuiltIn:
            continue
        key = profile['key']
        sname = profile['name'].encode('utf-8').strip()
        sname_satinized = globaldef.satinizeqgname(sname)
        language = profile['language']
        isDefault = profile['isDefault']

        print("\tDownload profile >>> lang = {0}, DEF = {1}, NAME = {2} ".format(language, str(isDefault), sname))
        fprofile = globaldef.dir_profiles+"/"+sname_satinized+"-"+key+'.xml'

        try:
            params = urllib.urlencode({'key': key})
            response = urllib2.urlopen(globaldef.url_remote_profile_backup, params)
            globaldef.profileDumpStream(response, fprofile)
            if idx == 3:
                break
        except Exception as ex:
            traceremoteerror(ex)
            exit(1)


print ("\n\n")

with open(globaldef.fqgates) as json_file:
    qgates = json.load(json_file)
    for qgate in qgates['qualitygates']:
        sname = qgate['name'].encode('utf-8').strip()
        sid = str(qgate['id'])
        print("\tDownload Quality Gate >>> id = {0:<3}, NAME = {1} ".format(sid, sname))
        sname_satinized = globaldef.satinizeqgname(sname)
        fqgate = globaldef.dir_qgates + "/{0}-{1}.json".format(sid, sname_satinized)
        try:
            params = urllib.urlencode({'id': sid})
            qurl = globaldef.url_remote_show_qualitygate + '?' + params
            print("\t ====>> "+ qurl)
            response = urllib2.urlopen(qurl)
            globaldef.dumpJSONStream(response, fqgate)
        except Exception as ex:
            traceremoteerror(ex)
            exit(1)
