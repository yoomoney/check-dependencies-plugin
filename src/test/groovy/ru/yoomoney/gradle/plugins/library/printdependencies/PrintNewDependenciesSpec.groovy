package ru.yoomoney.gradle.plugins.library.printdependencies

import org.gradle.util.VersionNumber
import ru.yoomoney.gradle.plugins.library.AbstractPluginSpec


class PrintNewDependenciesSpec extends AbstractPluginSpec {

    def setup() {
        buildFile << """
                majorVersionChecker {
                    enabled = false
                }
        """.stripIndent()
    }

    def "Print new version for dependency by inclusion"() {

        given:
        buildFile << """
            checkDependencies {
                    inclusionPrefixesForPrintDependencies = ['org.apache']
            }
                
            dependencies {
                implementation localGroovy(),
                        'org.apache.tomcat.embed:tomcat-embed-core:9.0.10',
                        'joda-time:joda-time:1.6.2'                     
            } 
            
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("printNewDependenciesByInclusion")

        then:
        result.standardOutput.contains("org.apache.tomcat.embed:tomcat-embed-core 9.0.10 ->")
        !result.standardOutput.contains("joda-time:joda-time 1.6.2 ->")

        def update = result.standardOutput.findAll("org\\.apache\\.tomcat\\.embed:tomcat-embed-core 9\\.0\\.10 -> (\\d+)\\.(\\d+)\\.(\\d+)")
        def semver = update[0] =~ /(\d+)\.(\d+)\.(\d+)/
        VersionNumber.parse(semver[1][0]) > VersionNumber.parse(semver[0][0])
    }

    def "Print new version for all dependency"() {

        given:
        buildFile << """
            
            checkDependencies {
                    inclusionPrefixesForPrintDependencies = ['org.apache']
            }
                
            dependencies {
                implementation localGroovy(),
                        'org.apache.tomcat.embed:tomcat-embed-core:9.0.10',
                        'joda-time:joda-time:1.6.2'  
            } 
          
                """.stripIndent()
        when:
        def result = runTasksSuccessfully("printNewDependencies")

        then:
        result.standardOutput.contains("org.apache.tomcat.embed:tomcat-embed-core 9.0.10 ->")
        result.standardOutput.contains("joda-time:joda-time 1.6.2 ->")
    }

    def "Tasks show dependencies not execute when run 'build'"() {

        given:
        buildFile << """
            
            checkDependencies {
                    inclusionPrefixesForPrintDependencies = ['org.apache']
            }
                
            dependencies {
                implementation localGroovy(),
                        'org.apache.tomcat.embed:tomcat-embed-core:9.0.10',
                        'joda-time:joda-time:1.6.2'                     
            } 
           
                """.stripIndent()
        when:
        def result = runTasksSuccessfully("build")

        then:
        !result.standardOutput.contains("org.apache.tomcat.embed:tomcat-embed-core 9.0.10 ->")
        !result.standardOutput.contains("joda-time:joda-time 1.6.2 ->")
    }

}
