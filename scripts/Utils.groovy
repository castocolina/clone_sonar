@Grab('org.yaml:snakeyaml:1.26')

import org.yaml.snakeyaml.Yaml
import java.nio.charset.StandardCharsets

class Utils {

    static loadYaml (String filename) {
        def yamlFile = new File(filename)
        return new Yaml().load(new FileInputStream(yamlFile))
    }

    static accessOrExit (String serverUrl, String user, String passwd) {
        def accessCmd = """
        curl -s -S -f -X POST -L '$serverUrl/api/authentication/login' \\
        -H 'Content-Type: application/x-www-form-urlencoded' \\
        --data 'login=$user&password=$passwd' && echo OK
        """
        def cmdFile = createCmd(accessCmd)
        def result = waitOrKillCmd(cmdFile)
        if(!result[0]){
            printError("ACESS DENIED!!")
            System.exit(1)
        }
    }

    static authBasic(String user, String pass) {
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

    static findByName(def list, String name, boolean ignoreCase = false){
        def result
        for (item in list){
            def iName = item['name']
            if (iName?.equals(name) || (ignoreCase && iName?.equalsIgnoreCase(name))) {
                result = item
                break
            }
        }
        return result
    }
}
