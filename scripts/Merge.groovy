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

import groovy.json.JsonSlurper

def config = loadYaml("config.yaml")

println ()
println textColor("LOCAL LOGIN VERIFICATION!!", 'green', 'default', 'bold')
println ()

accessOrExit(config.local.base_url, config.local.token, config.local.user, config.local.password)

def token = createBase64Token(config.local.user, config.local.password, config.local.token)
def authHeader = "Authorization: Basic $token"

println textColor("Merge Quality profiles!!", 'green', 'default', 'bold')
assert config.to_merge.qp.from.size() > 1

def qpFrom = config.to_merge.qp.from
def qpSize = qpFrom.size()
def qpTarget = config.to_merge.qp.to
def qpLang = config.to_merge.qp.lang
def qpBase = qpFrom[0]
def qpOthers = qpFrom.subList(1, qpSize)
def localQgatesFile = "${config.store.base}/local-qgates.json"
def localQProfFile = "${config.store.base}/local-qprofiles.json"

println textColor("FROM ==> $qpBase + $qpOthers", 'red')
println textColor("TARGET ==> $qpTarget", 'blue')
println()


println textColor("Download LOCAL QGate & Qprofile LIST!!", 'green', 'default', 'bold')
println ()
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


def localQProfileBase = findByNameAndLang(qProfiles, qpBase, qpLang)
def localQProfileTarget = findByNameAndLang(qProfiles, qpTarget, qpLang)

cmdStr = """
curl -s -S -f -X POST -L '$config.local.base_url/api/qualityprofiles/copy'  \\
    -H "Content-Type: application/x-www-form-urlencoded"  -H '$authHeader' \\
    --data "fromKey=$localQProfileBase.key&toName=$qpTarget"  && echo "QProfile COPIED!!!"
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
        --data "activation=true&qprofile=$localQProfile.key&targetKey=$localQProfileTarget.key"  && echo "Rules ACTIVATED!!!"
    """
    cmdFile = createCmd(cmdStr)
    result = waitOrKillCmd(cmdFile)
}

println ()

def downloadFile(String targetFile, String url, String authHeader) {
    def cmdStr = """
    curl -s -S -f -X GET \\
        -o "$targetFile" -L '$url'  \\
        -H '$authHeader' && echo Profiles Dowloaded OK
    """
    def cmdFile = createCmd(cmdStr)
    return waitOrKillCmd(cmdFile)
}
