import static Utils.loadYaml
import static Utils.textColor
import static Utils.accessOrExit
import static Utils.createBase64Token
import static Utils.createCmd
import static Utils.waitOrKillCmd
import static Utils.findByName
import static Utils.findByNameAndLang
import static Utils.printSuccess
import static Utils.printError
import static Utils.downloadFile
import static Utils.wipeConditions
import static Utils.updateConditions

import groovy.json.JsonSlurper

def config = loadYaml("config.yaml")
def SEPARATOR = '====================================================='

println ()
println textColor("LOCAL LOGIN VERIFICATION!!", 'green', 'default', 'bold')
println ()

accessOrExit(config.local.base_url, config.local.token, config.local.user, config.local.password)

def token = createBase64Token(config.local.user, config.local.password, config.local.token)
def authHeader = "Authorization: Basic $token"

assert config.to_merge.qp.from.size() > 1

def qpFrom = config.to_merge.qp.from
def qpSize = qpFrom.size()
def qpTarget = config.to_merge.qp.to
def qpLang = config.to_merge.qp.lang
def qpBase = qpFrom[0]
def qpOthers = qpFrom.subList(1, qpSize)
// --------------
def qgFrom = config.to_merge.qg.from
def qgSize = qgFrom.size()
def qgTarget = config.to_merge.qg.to
def qgLang = config.to_merge.qg.lang
def qgBase = qgFrom[0]
def qgOthers = qgFrom.subList(1, qgSize)
// --------------
def localQgatesFile = "${config.store.base}/local-qgates.json"
def localQProfFile = "${config.store.base}/local-qprofiles.json"


println()
println textColor("$SEPARATOR", 'green', 'default', 'bold')
println ()

println textColor("Download LOCAL QGate & Qprofile LIST!!", 'green', 'default', 'bold')
def cmdStr = ""
def cmdFile = ""

def result = downloadFile (localQgatesFile, "$config.local.base_url/api/qualitygates/list", authHeader)
result = downloadFile (localQProfFile, "$config.local.base_url/api/qualityprofiles/search", authHeader)

println textColor("Check if origin profiles exist", 'yellow')
def qProfiles = new JsonSlurper().parse(new File(localQProfFile)).profiles
for (qProfile in qpFrom) {
    def localQProfile = findByNameAndLang(qProfiles, qProfile, qpLang)
    if(localQProfile){
        printSuccess("Profile exist ==> ($localQProfile.language) [$localQProfile.name] '$localQProfile.key' ")
    } else {
        printError("Profile not found [$qProfile]")
        println ()
        System.exit(1)
    }
}
println ()

println textColor("Check if origin gates exist", 'yellow', 'default', 'bold')
def qGates = new JsonSlurper().parse(new File(localQgatesFile)).qualitygates
for (qGate in qgFrom) {
    def localQGate = findByName(qGates, qGate)
    if(localQGate){
        printSuccess("Gate exist ==> ([$localQGate.name] '$localQGate.id' ")
    } else {
        printError("Gate not found [$qGate]")
        println ()
        System.exit(1)
    }
}
println ()
println textColor("$SEPARATOR", 'green', 'default', 'bold')
println ()

println textColor("Copy target from first QProfile", 'yellow', 'default', 'bold')
def localQProfileBase = findByNameAndLang(qProfiles, qpBase, qpLang)
def localQProfileTarget = findByNameAndLang(qProfiles, qpTarget, qpLang)
cmdStr = """
curl -s -S -f -X POST -L '$config.local.base_url/api/qualityprofiles/copy'  \\
    -H "Content-Type: application/x-www-form-urlencoded"  -H '$authHeader' \\
    --data "fromKey=$localQProfileBase.key&toName=$qpTarget"  && printf "\nQProfile COPIED!!!\n"
"""
cmdFile = createCmd(cmdStr)
result = waitOrKillCmd(cmdFile)
println ()

downloadFile (localQProfFile, "$config.local.base_url/api/qualityprofiles/search", authHeader)
qProfiles = new JsonSlurper().parse(new File(localQProfFile)).profiles
localQProfileTarget = findByNameAndLang(qProfiles, qpTarget, qpLang)

for (qProfile in qpOthers) {
    println textColor("Activate rules from $qProfile", 'green')
    def localQProfile = findByNameAndLang(qProfiles, qProfile, qpLang)

    cmdStr = """
    curl -s -S -f -X POST -L '$config.local.base_url/api/qualityprofiles/activate_rules'  \\
        -H "Content-Type: application/x-www-form-urlencoded"  -H '$authHeader' \\
        --data "activation=true&qprofile=$localQProfile.key&targetKey=$localQProfileTarget.key"  && printf "\nRules ACTIVATED!!!\n"
    """
    cmdFile = createCmd(cmdStr)
    result = waitOrKillCmd(cmdFile)
}
println ()
println textColor("$SEPARATOR", 'green', 'default', 'bold')
println ()

println textColor("Create or refresh target QGate", 'yellow', 'default', 'bold')
def localQGateBase = findByName(qGates, qgBase)
def localQGateTarget = findByName(qGates, qgTarget)

if(localQGateTarget){
    println textColor("Gate exist and will be wiped ==> [$localQGateTarget.name] '$localQGateTarget.id'", 'red', 'white')
    wipeConditions(config.local.base_url, authHeader, localQGateTarget.id)
} else {
    printSuccess("Target QGate does not exist and will be created => $qgTarget")
    cmdStr = """
    curl -s -S -f -X POST -L '$config.local.base_url/api/qualitygates/create'  \\
        -H "Content-Type: application/x-www-form-urlencoded"  -H '$authHeader' \\
        --data "name=$qgTarget"  && printf "\nQGate CREATED!!!\n"
    """
    cmdFile = createCmd(cmdStr)
    result = waitOrKillCmd(cmdFile)
}
println ()

downloadFile (localQgatesFile, "$config.local.base_url/api/qualitygates/list", authHeader)
qGates = new JsonSlurper().parse(new File(localQgatesFile)).qualitygates
localQGateTarget = findByName(qGates, qgTarget)

for (qGate in qgFrom) {
    println textColor("\tMERGE metricts from '$qGate' to '$qgTarget'", "green")
    def localQGate = findByName(qGates, qGate)
    def currGate = new JsonSlurper().parse(new URL("$config.local.base_url/api/qualitygates/show?id=$localQGate.id"))
    updateConditions(config.local.base_url, authHeader, localQGateTarget.id, currGate.conditions)
}

println ()
println ()
