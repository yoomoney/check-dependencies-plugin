package ru.yandex.money.gradle.plugins.library.printdependencies

import ru.yandex.money.gradle.plugins.library.AbstractPluginSpec


class PrintDependeciesSpec extends AbstractPluginSpec {

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

    def "Print new version for inner dependency"() {

        given:
        buildFile << """
                        
            dependencies {
                compile 'ru.yandex.money.common:yamoney-json-utils:1.0.0',
                        'ru.yandex.money.common:yamoney-xml-utils:1.0.0',
                        'com.google.guava:guava:18.0'                       
            } 
            
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("printNewInnerDependenciesVersions")

        then:
        result.standardOutput.contains("ru.yandex.money.common:yamoney-json-utils 1.0.0 ->")
        result.standardOutput.contains("ru.yandex.money.common:yamoney-xml-utils 1.0.0 ->")
        !result.standardOutput.contains("com.google.guava:guava 22.0 ->")

    }

    def "Print new version for outer dependency"() {

        given:
        buildFile << """
            
            dependencies {
                compile 'ru.yandex.money.common:yamoney-json-utils:1.0.0',
                        'com.google.guava:guava:22.0'
                       
            } 
          
                """.stripIndent()
        when:
        def result = runTasksSuccessfully("printNewOuterDependenciesVersions")

        then:
        result.standardOutput.contains("com.google.guava:guava 22.0 ->")
        !result.standardOutput.contains("ru.yandex.money.common:yamoney-json-utils 1.0.0 ->")
    }

    def "Print new version for outer and inner dependency"() {

        given:
        buildFile << """
            
            dependencies {
                compile 'ru.yandex.money.common:yamoney-json-utils:1.0.0',
                        'com.google.guava:guava:22.0'
            } 
            
                """.stripIndent()
        when:
        def result = runTasksSuccessfully("printNewOuterDependenciesVersions", "printNewInnerDependenciesVersions")

        then:
        result.standardOutput.contains("com.google.guava:guava 22.0 ->")
        result.standardOutput.contains("ru.yandex.money.common:yamoney-json-utils 1.0.0 ->")
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
        !result.standardOutput.contains("com.google.guava:guava 22.0 ->")
        !result.standardOutput.contains("ru.yandex.money.common:yamoney-json-utils 1.0.0 ->")
    }

}
