#!/usr/bin/python
import os, sys, traceback
import urllib, urllib2
from poster.encode import multipart_encode
from poster.streaminghttp import register_openers
import json, xml.dom.minidom
import formdata, globaldef
from globaldef import tracelocalerror, HIGHLIGHT, HIGHLIGHT_END

print ("{}VERIFIED CREDENTIALS FOR TARGET!{}".format(HIGHLIGHT, HIGHLIGHT_END))
try:
    params = urllib.urlencode({'format' : 'json'})

    request = urllib2.Request(globaldef.url_local_auth_validate+"?"+params)
    request.add_header("Authorization", "Basic %s" % globaldef.local_base64credentials)
    response = urllib2.urlopen(request)
    validationdata = response.read();
    print("VALID === {0} -- {1}\n\n".format(response.getcode(), validationdata))
except Exception as ex:
    tracelocalerror(ex)
    exit(1)

valid = json.loads(validationdata)['valid']

if not valid :
    print("Error en el acceso a la API local")
    exit(1)

# Configurar Jacoco
# sonar.jacoco.reportMissing.force.zero=true (default false)
print ("{}JACOCO CONFIG!{}".format(HIGHLIGHT, HIGHLIGHT_END))
try:
    params = urllib.urlencode({'format' : 'json', 'id': globaldef.JACOCO_PROPERTY, 'value': 'true'})

    request = urllib2.Request(globaldef.url_local_properties, params)
    request.add_header("Authorization", "Basic %s" % globaldef.local_base64credentials)
    response = urllib2.urlopen(request)
    jacocodata = response.read();
    print("JACOCO CONFIG === {0} -- {1}\n\n".format(response.getcode(), jacocodata))

    request = urllib2.Request(globaldef.url_local_properties+'/'+globaldef.JACOCO_PROPERTY)
    request.add_header("Authorization", "Basic %s" % globaldef.local_base64credentials)
    response = urllib2.urlopen(request)
    jacocodata = response.read();
    print(globaldef.url_local_properties+'/'+globaldef.JACOCO_PROPERTY)
    print("JACOCO CONFIG VALUE === {0} -- {1}\n\n".format(response.getcode(), jacocodata))
except Exception as ex:
    tracelocalerror(ex)
    exit(1)

print(" RESTORE PROFILES!!!")

for filename in os.listdir(globaldef.dir_profiles):
    if filename.endswith(".xml"):
        profile_path=os.path.join(globaldef.dir_profiles, filename)
        xml_obj = xml.dom.minidom.parse(profile_path)
        pname = globaldef.getText(xml_obj.getElementsByTagName("name")[0]).encode('utf-8').strip()
        print("\tRESTORE >>> " + pname + " ---> " + profile_path)

        # filedata = open(profile_path, 'rb').read()
        # params = {}
        # files = {'backup': {'filename': os.path.basename(profile_path), 'content': filedata}}
        # data, headers = formdata.encode_multipart(params, files)

        # request = urllib2.Request(globaldef.url_local_profile_restore, data, headers)
        # request.add_header("Authorization", "Basic %s" %globaldef.local_base64credentials)
        try:
            ## response = urllib2.urlopen(request)
            
            # Register the streaming http handlers with urllib2
            register_openers()

            datagen, headers = multipart_encode({"backup": open(profile_path)})

            # Create the Request object
            request = urllib2.Request(globaldef.url_local_profile_restore, datagen, headers)
            print(globaldef.local_base64credentials)
            request.add_header("Authorization", "Basic %s" %globaldef.local_base64credentials)

            response = urllib2.urlopen(request)
        except Exception as ex:
            # print(ex.reason)
            tracelocalerror(ex)
            exit(1)

        rtext = response.read()
        print("\t  {0} -- {1}\n".format(response.getcode(), rtext))
        continue
    else:
        continue

with open(globaldef.fprofiles) as json_file:
    profiles = json.load(json_file)['profiles']
    print("\n\nDEAFULT profiles >> "+ globaldef.url_local_profile_default)
    for profile in profiles:
        key = profile['key']
        sname = profile['name'].encode('utf-8').strip()
        isDefault = profile['isDefault']
        language = profile['language']

        if isDefault:
            params = urllib.urlencode({'key': key})
            request = urllib2.Request(globaldef.url_local_profile_default, params)
            request.add_header("Authorization", "Basic %s" %globaldef.local_base64credentials)
            print("\t{}SET default Profile >>> lang = {:<4}, NAME = {}{}".format(HIGHLIGHT, language, sname, HIGHLIGHT_END))
            try:
                response = urllib2.urlopen(request)
            except Exception as ex:
                tracelocalerror(ex)
                # exit(1)

            rtext = response.read()
            print("\t  {0} -- {1}\n".format(response.getcode(), rtext))

try:
    response = urllib2.urlopen(globaldef.url_local_list_qualitygates)
    globaldef.dumpJSONStream(response, globaldef.flocalqgates)
except Exception as ex:
    tracelocalerror(ex)
    exit(1)

with open(globaldef.fqgates) as json_file:
    qgates = json.load(json_file)
    print("\n\nLOAD QGates >>")
    defaultsid = str(qgates['default'])

    for qgate in qgates['qualitygates']:
        sname = qgate['name'].encode('utf-8').strip()
        sid = str(qgate['id'])
        localsid = None
        print("\tCONFIG QGate >>> id = {0:<2}, NAME = {1} ".format(sid, sname))
        isdefault = sid == defaultsid
        #Check exist local qgate
        exist = globaldef.existqgate(sname)
        localsid = globaldef.existqgateid(sname)
        #if exist remove all conditions
        if not localsid == None:
            print ("\t\t--EXIST")
            params = urllib.urlencode({'id': localsid})
            qgurl = globaldef.url_local_show_qualitygate + '?' + params
            try:
                response = urllib2.urlopen(qgurl)
                json_local_qgate = json.load(response);
                conditions = []
                if 'conditions' in json_local_qgate.keys():
                    conditions = json_local_qgate['conditions']
                for condition in conditions:
                    scid = str(condition['id'])
                    params = urllib.urlencode({'format': 'json', 'id': scid})
                    request = urllib2.Request(globaldef.url_local_delete_condition, params)
                    request.add_header("Authorization", "Basic %s" %globaldef.local_base64credentials)
                    print("\t\tDELETE CONDITION >>> id = {:<4}".format(scid))
                    response = urllib2.urlopen(request)
                    rtext = response.read()
                    print("\t\t  {0} -- {1}\n".format(response.getcode(), rtext))

            except Exception as ex:
                tracelocalerror(ex)
                exit(1)
        else: #else create new
            print ("\t\t++NOT EXIST")
            params = urllib.urlencode({'name': sname})
            request = urllib2.Request(globaldef.url_local_create_qualitygate, params)
            request.add_header("Authorization", "Basic %s" %globaldef.local_base64credentials)
            print("\t\tCREATE QGATE >>> name = {}".format(sname))
            try:
                response = urllib2.urlopen(request)
                rtext = response.read()
                localgate = json.loads(rtext)
                localsid = str(localgate['id'])
                print("\t\t  {0} -- {1}\n".format(response.getcode(), rtext))
            except Exception as ex:
                tracelocalerror(ex)
                exit(1)

        remote_qg_file = globaldef.getremoteqgatefile(sname)
        gateid = localsid

        with open(remote_qg_file) as json_file:
            remotegate = json.load(json_file)
            for condition in remotegate['conditions']:
                condition['gateId'] = gateid
                params = urllib.urlencode(condition)
                request = urllib2.Request(globaldef.url_local_create_condition, params)
                request.add_header("Authorization", "Basic %s" %globaldef.local_base64credentials)
                print("\t\tCREATE CONDITION >>> {}".format(params))
                try:
                    response = urllib2.urlopen(request)
                    rtext = response.read()
                    print("\t\t  {0} -- {1}\n".format(response.getcode(), rtext))
                except Exception as ex:
                    tracelocalerror(ex)
                    exit(1)

        if isdefault:
            params = urllib.urlencode({'id':localsid})
            request = urllib2.Request(globaldef.url_local_setdef_qualitygate, params)
            request.add_header("Authorization", "Basic %s" %globaldef.local_base64credentials)
            print("\t\t\t{}SET default QGate {} === NAME = {}{}".format(HIGHLIGHT, params, sname, HIGHLIGHT_END))
            try:
                response = urllib2.urlopen(request)
                rtext = response.read()
                print("\t\t\t {0} -- {1}\n".format(response.getcode(), rtext))
            except Exception as ex:
                tracelocalerror(ex)
                exit(1)

#
