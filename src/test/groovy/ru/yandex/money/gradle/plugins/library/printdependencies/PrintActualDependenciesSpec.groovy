package ru.yandex.money.gradle.plugins.library.printdependencies

import org.apache.commons.io.IOUtils
import ru.yandex.money.gradle.plugins.library.AbstractPluginSpec

class PrintActualDependenciesSpec extends AbstractPluginSpec {

    def setup() {
        buildFile << """

                repositories {
                    maven { url 'http://nexus.yamoney.ru/content/repositories/thirdparty/' }
                    maven { url 'http://nexus.yamoney.ru/content/repositories/central/' }
                    maven { url 'http://nexus.yamoney.ru/content/repositories/releases/' }
                    maven { url 'http://nexus.yamoney.ru/content/repositories/public/' }
                }
                
                majorVersionChecker {
                    enabled = false
                }
        """.stripIndent()
    }

    def "Print actual versions for inner dependency"() {

        given:
        buildFile << """
                        
            dependencies {
                compile localGroovy(),
                        'ru.yandex.money.common:yamoney-json-utils:1.0.0',
                        'ru.yandex.money.common:yamoney-xml-utils:1.0.0',
                        'ru.yandex.money.common:yamoney-backend-platform-config:19.0.0',
                        'com.google.guava:guava:18.0'                       
            } 
            
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("printActualInnerDependenciesVersions")

        then:
        result.standardOutput.contains("===============Actual inner dependencies===============")
        def update = result.standardOutput.findAll("\\n\\[((.|\\n)*)\\n\\]")
        update.size() == 1
        def foundjson = update.get(0)
        def resourceString = IOUtils.toString(getClass().getResourceAsStream("/actual_inner_dependencies.json"))

        foundjson.contains(resourceString)

        def reportString = IOUtils.toString(new FileInputStream(new File("build/nebulatest/" +
                "ru.yandex.money.gradle.plugins.library.printdependencies.PrintActualDependenciesSpec/" +
                "Print-actual-versions-for-inner-dependency/build/report/dependencies/actual_inner_dependencies.json")))
        reportString.contains(resourceString)

    }

    def "Print actual versions for outer dependency"() {

        given:
        buildFile << """
            
            dependencies {
                compile localGroovy(),
                        'ru.yandex.money.common:yamoney-json-utils:1.0.0',
                        'com.google.guava:guava:22.0'
                       
            } 
          
                """.stripIndent()
        when:
        def result = runTasksSuccessfully("printActualOuterDependenciesVersions")

        then:
        result.standardOutput.contains("===============Actual outer dependencies===============")
        def update = result.standardOutput.findAll("\\n\\[((.|\\n)*)\\n\\]")
        update.size() == 1
        def foundjson = update.get(0)
        def resourceString = IOUtils.toString(getClass().getResourceAsStream("/actual_outer_dependencies.json"))

        foundjson.contains(resourceString)

        def reportString = IOUtils.toString(new FileInputStream(new File("build/nebulatest/" +
                "ru.yandex.money.gradle.plugins.library.printdependencies.PrintActualDependenciesSpec/" +
                "Print-actual-versions-for-outer-dependency/build/report/dependencies/actual_outer_dependencies.json")))
        reportString.contains(resourceString)

    }


    def "Tasks show dependencies not execute when run 'build'"() {

        given:
        buildFile << """
            
            dependencies {
                compile 'ru.yandex.money.common:yamoney-json-utils:1.0.0',
                        'com.google.guava:guava:22.0'
                       
            } 
           
                """.stripIndent()
        when:
        def result = runTasksSuccessfully("build")

        then:
        !result.standardOutput.contains("===============Actual inner dependencies===============")
        !result.standardOutput.contains("===============Actual outer dependencies===============")
    }

}
