
import static Utils.loadYaml
import static Utils.textColor
import static Utils.accessOrExit
import static Utils.authBasic
import static Utils.createCmd
import static Utils.waitOrKillCmd

import groovy.json.JsonSlurper

def config = loadYaml("config.yaml")

println ()
println textColor("LOCAL LOGIN VERIFICATION!!", 'green', 'default', 'bold')
println ()

accessOrExit(config.local.base_url, config.local.user, config.local.password)

def basicAuth = "Basic " + authBasic(config.local.user, config.local.password)
def authHeader = "Authorization: $basicAuth"

def qgFolder = config.store.base + config.store.qg
def qpFolder = config.store.base + config.store.qp

println textColor("Upload LOCAL Quality profiles to local!!", 'green', 'default', 'bold')
println ()
new File(qpFolder).eachFile {
    def qProfileFName = it
    def uploadCmd = """
    curl -s -S -f -L '$config.local.base_url/api/qualityprofiles/restore'  \\
    -H "Content-Type: multipart/form-data"  -H '$authHeader' \\
    --form "backup=@$qProfileFName" \\
    && echo " Profile Uploaded OK"
    """

    println "\t RESTORE FROM: $qProfileFName"

    cmdFile = createCmd(uploadCmd)
    waitOrKillCmd(cmdFile)
}
