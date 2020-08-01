@Grab('org.yaml:snakeyaml:1.26')

import org.yaml.snakeyaml.Yaml
import java.nio.charset.StandardCharsets
import groovy.json.JsonSlurper

class Utils {

    static loadYaml (CharSequence filename) {
        def yamlFile = new File(filename)
        return new Yaml().load(new FileInputStream(yamlFile))
    }

    static accessOrExit (CharSequence serverUrl, String token, CharSequence user, CharSequence passwd) {
        def accessCmd = """
        curl -s -S -f -X POST -L '$serverUrl/api/authentication/login' \\
        -H 'Content-Type: application/x-www-form-urlencoded' \\
        --data 'login=$user&password=$passwd' && echo OK
        """

        if(token) {
            accessCmd = """
            curl -s -S -f -L '$serverUrl/api/user_tokens/search' \\
            -u $token: > /dev/null && echo OK
            """
        }

        def cmdFile = createCmd(accessCmd)
        def result = waitOrKillCmd(cmdFile)
        if(!result[0]){
            printError("ACESS DENIED!!")
            System.exit(1)
        }
    }

    static createBase64Token(CharSequence user = '', CharSequence pass = "", CharSequence token = '') {
        if(token){
            user = token
            pass = ""
        }
        return Base64.getEncoder().encodeToString(("$user:$pass").getBytes(StandardCharsets.UTF_8))
    }

    static createCmd (CharSequence cmdStr) {
        def tmpCmdPath
        def cmdFile = File.createTempFile(".tmpCmd", ".sh").with {
            // deleteOnExit()
            write "#!/bin/bash\n"
            append "export LC_ALL=en_US.UTF-8\n"
            append "${cmdStr.toString()}\n"
            tmpCmdPath = absolutePath
        }
        return "bash $tmpCmdPath"
    }

    static waitOrKillCmd(String cmd, int ms = 10000, boolean printSuccessResult = true) {
        def currProcess = "$cmd".execute()
        def sOut = new StringBuilder(), sErr = new StringBuilder()
        currProcess.consumeProcessOutput(sOut, sErr)
        currProcess.waitForOrKill(ms)
        if(printSuccessResult && sOut?.toString()?.trim()) {
            printSuccess(sOut)
        }
        if(sErr?.toString()?.trim()) {
            printError(cmd, "CMD")
            printError(sErr)
        }
        return [sOut, sErr]
    }

    static printSuccess (CharSequence text, CharSequence prefix = 'SUCCESS: 👍') {
        def cPrefix = textColor(prefix?.toString(), 'blue', 'default', 'bold')
        def cText = textColor(text?.toString(), 'blue')
        println("$cPrefix\n$cText")
    }

    static printError (CharSequence text, CharSequence prefix = 'ERROR: 👎') {
        def cPrefix = textColor(prefix?.toString(), 'red', 'default', 'bold')
        def cText = textColor(text?.toString(), 'red')
        println("$cPrefix\n$cText")
    }

    // https://gist.github.com/crevier/f2529b8b474ecddeb606770d148e761f
    static textColor (CharSequence text, String fgColor = 'default', String bgColor = 'default', String... effects = ['normal']) {
        def fgEffects = [normal: 0, bold: 1, italic: 3, underline: 4, blink: 5, rapidBlink: 6, reverseVideo: 7]
        def textEffects = ""
        effects.each {
            textEffects += "${fgEffects[it]};"
        }
        def fgColors = [black: 30, red: 31, green: 32, yellow: 33, blue: 34, magenta: 35, cyan: 36, white: 37, default: 39]
        def bgColors = [black: 40, red: 41, green: 42, yellow: 43, blue: 44, magenta: 45, cyan: 46, white: 47, default: 49]
        return new String((char) 27) + "[${textEffects}${fgColors[fgColor]};${bgColors[bgColor]}m${text}" + new String((char) 27) + "[0m"
    }

    static findByName(def list, String name, boolean ignoreCase = false, property = 'name'){
        def result
        for (item in list){
            def iName = item[property]
            if (iName?.equals(name) || (ignoreCase && iName?.equalsIgnoreCase(name))) {
                result = item
                break
            }
        }
        return result
    }

    static findByNameAndLang(def list, String name, String lang, boolean ignoreCase = false){
        def result
        for (item in list){
            def iName = item['name']
            def iLang = item['language']
            if (iName?.equals(name) || (ignoreCase && iName?.equalsIgnoreCase(name))) {
                result = item
            }
            if (result && lang &&
                (iLang?.equals(lang) || (ignoreCase && iLang?.equalsIgnoreCase(lang)))
                    ) {
                break
            }
        }
        return result
    }

    static downloadFile(String targetFile, String url, String authHeader) {
        def cmdStr = """
        curl -s -S -f -X GET \\
            -o "$targetFile" -L '$url'  \\
            -H '$authHeader' && echo Dowloaded OK
        """
        def cmdFile = createCmd(cmdStr)
        return waitOrKillCmd(cmdFile)
    }

    static wipeConditions(String baseURL, String authHeader, Integer gateId) {
        def currGate = new JsonSlurper().parse(new URL("$baseURL/api/qualitygates/show?id=$gateId"))
        def currConditions = currGate.conditions
        for (condition in currConditions) {
            println textColor("\t\tDELETE condition >> id: $condition.id, metric: $condition.metric", 'yellow')
            def cmdStr = """
            curl -s -S -f -X POST -L '$baseURL/api/qualitygates/delete_condition'  \\
                -H "Content-Type: application/x-www-form-urlencoded" -H '$authHeader' \\
                --data "id=$condition.id" && \\
                printf "\nOK"
            """
            def cmdFile = createCmd(cmdStr)
            waitOrKillCmd(cmdFile)
        }
        
    }

    static updateConditions(String baseURL, String authHeader, Integer gateId, ArrayList newConditions) {
        def currGate = new JsonSlurper().parse(new URL("$baseURL/api/qualitygates/show?id=$gateId"))
        def currConditions = currGate.conditions
        for (condition in newConditions) {
            def localCond = findByName(currConditions, condition.metric, true, 'metric')
            if (localCond) {
                println textColor("\t\tEXISTING condition >> id: $localCond.id, metric: ${condition.metric}. Will be UPDATED!", 'yellow')
                def cmdStr = """
                curl -s -S -f -X POST -L '$baseURL/api/qualitygates/update_condition'  \\
                    -H "Content-Type: application/x-www-form-urlencoded" -H '$authHeader' \\
                    --data "id=$localCond.id&error=$condition.error&metric=$condition.metric&op=$condition.op" && \\
                    printf "\nOK"
                """
                def cmdFile = createCmd(cmdStr)
                waitOrKillCmd(cmdFile)
            } else {
                println textColor("\t\tNEW condition >> metric: ${condition.metric}. Will be CREATED!", 'cyan')
                def cmdStr = """
                curl -s -S -f -X POST -L '$baseURL/api/qualitygates/create_condition'  \\
                    -H "Content-Type: application/x-www-form-urlencoded" -H '$authHeader' \\
                    --data "gateId=$gateId&error=$condition.error&metric=$condition.metric&op=$condition.op" && \\
                    printf "\nOK"
                """
                def cmdFile = createCmd(cmdStr)
                waitOrKillCmd(cmdFile)
            }
        }

    }

}
