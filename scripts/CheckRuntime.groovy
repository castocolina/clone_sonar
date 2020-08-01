

gVersion = GroovySystem.version
jVersion = Runtime.version()

def gMajor = getMajor(gVersion)
def jMajor = jVersion.major()

println "Groovy: $gVersion running on Java: $jVersion"
println "Major versions: $gMajor - $jMajor"

assert gMajor >= 3 : "Groovy 3+ required"
assert jMajor >= 11 : "Java 11+ required"

def getMajor(String fullVersion) {
    def result = (fullVersion =~ /\d+/).findAll()
    return new Integer(result[0])
}
