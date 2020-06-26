
import static Utils.loadYaml
import static Utils.textColor
import static Utils.accessOrExit
import static Utils.authBasic
import static Utils.createCmd
import static Utils.waitOrKillCmd

def config = loadYaml("config.yaml")

println ()
println textColor("REMOTE LOGIN VERIFICATION!!", 'green', 'default', 'bold')
println ()

accessOrExit(config.remote.base_url, config.remote.user, config.remote.password)

def basicAuth = "Basic " + authBasic(config.remote.user, config.remote.password)
def authHeader = "Authorization: $basicAuth"


println textColor("Download REMOTE QGates & Qprofiles!!", 'blue', 'default', 'bold')
println ()
def accessCmd = """
mkdir -p $config.store.base
curl -s -S -f -X GET \\
-o "${config.store.base}/qgates.json" -L '$config.remote.base_url/api/qualitygates/list'  \\
-H '$authHeader' && echo Gates Dowloaded OK
"""
def cmdFile = createCmd(accessCmd)
def result = waitOrKillCmd(cmdFile)

accessCmd = """
curl -s -S -f -X GET \\
-o "${config.store.base}/qprofiles.json" -L '$config.remote.base_url/api/qualityprofiles/search'  \\
-H '$authHeader' && echo Profiles Dowloaded OK
"""
cmdFile = createCmd(accessCmd)
result = waitOrKillCmd(cmdFile)



def qgFolder = config.store.base + config.store.qg
def qpFolder = config.store.base + config.store.qp
