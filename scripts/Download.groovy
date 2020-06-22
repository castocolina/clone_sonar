
import static Utils.loadYaml
import static Utils.textColor
import static Utils.accessOrExit

def config = loadYaml("config.yaml")

println ()
println textColor("REMOTE LOGIN VERIFICATION!!", 'green', 'default', 'bold')
println ()

accessOrExit(config.remote.base_url, config.remote.user, config.remote.password)
