
import static Utils.loadYaml
import static Utils.textColor
import static Utils.accessOrExit
import static Utils.createBase64Token
import static Utils.createCmd
import static Utils.waitOrKillCmd

import groovy.json.JsonSlurper

def config = loadYaml("config.yaml")

println ()
println textColor("REMOTE LOGIN VERIFICATION!!", 'green', 'default', 'bold')
println ()

accessOrExit(config.remote.base_url, config.remote.token, config.remote.user, config.remote.password)

def token = createBase64Token(config.remote.user, config.remote.password, config.remote.token)
def authHeader = "Authorization: Basic $token"

def qgFolder = config.store.base + config.store.qg
def qpFolder = config.store.base + config.store.qp
def remQgatesFile = "${config.store.base}/remote-qgates.json"
def remQProfFile = "${config.store.base}/remote-qprofiles.json"

println textColor("Download REMOTE QGate & Qprofile LIST!!", 'green', 'default', 'bold')
println ()
def cmdStr = """
    mkdir -p $config.store.base
    mkdir -p $qgFolder
    mkdir -p $qpFolder

    curl -s -S -f -X GET \\
        -o "$remQgatesFile" -L '$config.remote.base_url/api/qualitygates/list'  \\
        -H '$authHeader' && echo Gates Dowloaded OK
"""
def cmdFile = createCmd(cmdStr)
def result = waitOrKillCmd(cmdFile)

cmdStr = """
    curl -s -S -f -X GET \\
        -o "$remQProfFile" -L '$config.remote.base_url/api/qualityprofiles/search'  \\
        -H '$authHeader' && echo Profiles Dowloaded OK
"""
cmdFile = createCmd(cmdStr)
result = waitOrKillCmd(cmdFile)

def qGates = new JsonSlurper().parse(new File(remQgatesFile)).qualitygates
println textColor("Download QGates!!", 'green', 'default', 'bold')
println ()
for (qGate in qGates){
    if(qGate.isBuiltIn){ // skip built-in
        continue
    }
    def safeName = qGate.name.replaceAll("[^A-Za-z0-9\\-]", "_").toLowerCase()
    def qGateFile = "$qgFolder/${safeName}.json"
    // printf "\tQ Gate: %s, %s \n", qGate.name, safeName
    println textColor("\tDownload Q Gates: ${qGate.name} => $qGateFile", 'green')
    cmdStr = """
    curl -s -S -f -X GET \\
        -o "$qGateFile" -L '$config.remote.base_url/api/qualitygates/show?id=${qGate.id}'  \\
        -H '$authHeader' && echo "$qGateFile Dowloaded OK"
    """
    cmdFile = createCmd(cmdStr)
    waitOrKillCmd(cmdFile)
}
println "\n"

def qProfiles = new JsonSlurper().parse(new File(remQProfFile)).profiles
println textColor("Download QProfiles!!", 'green', 'default', 'bold')
println ()
for (qProfile in qProfiles){
    if(qProfile.isBuiltIn){ // skip built-in
        continue
    }
    def safeName = qProfile.name.replaceAll("[^A-Za-z0-9\\-]", "_").toLowerCase()
    def qProfileFile = "$qpFolder/${qProfile.language}-${safeName}.xml"
    println textColor("\tDownload Q Profile: ${qProfile.name} => $qProfileFile", 'green')
    cmdStr = """
    curl -s -S -f \\
        -o "$qProfileFile" \\
        -L '$config.remote.base_url/api/qualityprofiles/backup'  \\
        -H '$authHeader' \\
        --data "language=${qProfile.language}&qualityProfile=${qProfile.name}" \\
        && echo "$qProfileFile Dowloaded OK"
    """
    cmdFile = createCmd(cmdStr)
    waitOrKillCmd(cmdFile)
}
println "\n"

println ""
