
import static Utils.loadYaml
import static Utils.textColor
import static Utils.accessOrExit
import static Utils.authBasic
import static Utils.createCmd
import static Utils.waitOrKillCmd
import static Utils.findByName

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

println textColor("Upload Quality profiles to local!!", 'green', 'default', 'bold')
println ()
new File(qpFolder).eachFile {
    def qProfileFile = it
    def uploadCmd = """
    curl -s -S -f -L '$config.local.base_url/api/qualityprofiles/restore'  \\
    -H "Content-Type: multipart/form-data"  -H '$authHeader' \\
    --form "backup=@$qProfileFile" \\
    && echo " Profile Uploaded OK"
    """

    println "\t RESTORE FROM: $qProfileFile"

    cmdFile = createCmd(uploadCmd)
    waitOrKillCmd(cmdFile)
}

println textColor("Upload Quality gates to local!!", 'green', 'default', 'bold')
println ()
def existingQGates = new JsonSlurper().parse(new URL("${config.local.base_url}/api/qualitygates/list"))
new File(qgFolder).eachFile {
    def qGateFile = it
    def qGateObj = new JsonSlurper().parseText(qGateFile.text)
    def name = qGateObj.name

    def localQGate = findByName(existingQGates.qualitygates, name)
    def localQGateId = localQGate?.id


    if (localQGate) {
        println "\t\tQGate exist: $name"
        def localGateObj = new JsonSlurper().parse(new URL("${config.local.base_url}/api/qualitygates/show?id=$localQGateId"))
        for (condition in localGateObj.conditions) {
            println textColor ("\t\t  DELETE CONDITION $condition.id:$condition.metric", "yellow")
            def deleteCondCmd = """
            curl -s -S -f -X POST -L '$config.local.base_url/api/qualitygates/delete_condition'  \\
            -H "Content-Type: application/x-www-form-urlencoded"  -H '$authHeader' \\
            --data "id=$condition.id"  && echo "Condition DELETED!"
            """

            cmdFile = createCmd(deleteCondCmd)
            waitOrKillCmd(cmdFile)
        }


    } else {
        println "\t\tQGate not exist: $name"

        def uploadCmd = """
        curl -s -S -f -X POST -L '$config.local.base_url/api/qualitygates/create'  \\
        -H "Content-Type: application/x-www-form-urlencoded"  -H '$authHeader' \\
        --data "name=$name" 
        """

        println textColor("\t\t CREATE Q GATE: $name", "cyan", "default", "bold")

        cmdFile = createCmd(uploadCmd)
        def result = waitOrKillCmd(cmdFile)
        if (result && result[0]) {
            localQGateId = new JsonSlurper().parseText(result[0].toString()).id
        }
    }
    for (condition in qGateObj.conditions){
        println textColor("\t\t  CREATE CONDITION >> metric = $condition.metric for $localQGateId:$name", "cyan")
        def createCondCmd = """
        curl -s -S -f -X POST -L '$config.local.base_url/api/qualitygates/create_condition'  \\
        -H "Content-Type: application/x-www-form-urlencoded"  -H '$authHeader' \\
        --data "gateId=$localQGateId&error=$condition.error&metric=$condition.metric&op=$condition.op"  \\
        && echo " Condition CREATED!"
        """

        cmdFile = createCmd(createCondCmd)
        waitOrKillCmd(cmdFile)
    }
    println ()
}


println ()
