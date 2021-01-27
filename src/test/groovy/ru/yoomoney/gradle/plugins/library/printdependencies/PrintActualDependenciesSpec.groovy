package ru.yoomoney.gradle.plugins.library.printdependencies

import org.apache.commons.io.IOUtils
import ru.yoomoney.gradle.plugins.library.AbstractPluginSpec

class PrintActualDependenciesSpec extends AbstractPluginSpec {

    def setup() {
        buildFile << """  
                majorVersionChecker {
                    enabled = false
                }
        """.stripIndent()
    }

    def "Print actual versions for dependency by inclusion"() {

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
        def result = runTasksSuccessfully("printActualDependenciesByInclusion")

        then:
        result.standardOutput.contains("===============Actual dependencies by inclusion===============")
        def update = result.standardOutput.findAll("\\n\\[((.|\\n)*)\\n\\]")
        update.size() == 1
        def foundjson = update.get(0)
        def resourceString = IOUtils.toString(getClass().getResourceAsStream("/actual_dependencies_by_inclusion.json"))

        foundjson.replace(" ", "")
                .contains(resourceString.replace(" ", ""))

        def reportString = IOUtils.toString(new FileInputStream(new File("build/nebulatest/" +
                "ru.yoomoney.gradle.plugins.library.printdependencies.PrintActualDependenciesSpec/" +
                "Print-actual-versions-for-dependency-by-inclusion/build/report/dependencies/actual_dependencies_by_inclusion.json")))
        reportString.replace(" ", "")
                .contains(resourceString.replace(" ", ""))

    }

    def "Print actual versions for all dependency"() {

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
        def result = runTasksSuccessfully("printActualDependencies")

        then:
        result.standardOutput.contains("===============Actual dependencies===============")
        def update = result.standardOutput.findAll("\\n\\[((.|\\n)*)\\n\\]")
        update.size() == 1
        def foundjson = update.get(0)
        def resourceString = IOUtils.toString(getClass().getResourceAsStream("/actual_dependencies.json"))

        foundjson.replace(" ", "")
                .contains(resourceString.replace(" ", ""))

        def reportString = IOUtils.toString(new FileInputStream(new File("build/nebulatest/" +
                "ru.yoomoney.gradle.plugins.library.printdependencies.PrintActualDependenciesSpec/" +
                "Print-actual-versions-for-all-dependency/build/report/dependencies/actual_all_dependencies.json")))
        reportString.replace(" ", "").contains(resourceString.replace(" ", ""))

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
        !result.standardOutput.contains("===============Actual dependencies by inclusion===============")
        !result.standardOutput.contains("===============Actual dependencies===============")
    }

}
