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
                
        """.stripIndent()
    }

    def "Print new version for inner dependency"() {

        given:
        buildFile << """
            
            ext.isFeatureBranch = true
            
            dependencies {
                compile 'ru.yandex.money.common:yamoney-json-utils:1.0.0',
                        'ru.yandex.money.common:yamoney-xml-utils:1.0.0',
                        'com.google.guava:guava:22.0'                       
            } 
            
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("build")

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
            
            ext.isFeatureBranch = true
            
            checkDependencies {
                    showOuterDependencies = true
            }
                """.stripIndent()
        when:
        def result = runTasksSuccessfully("build")

        then:
        result.standardOutput.contains("com.google.guava:guava 22.0 ->")
    }

    def "Setting showDependencies works correctly "() {

        given:
        buildFile << """
            
            dependencies {
                compile 'ru.yandex.money.common:yamoney-json-utils:1.0.0',
                        'com.google.guava:guava:22.0'
                       
            } 
            
            ext.isFeatureBranch = true
            
            checkDependencies {
                    showOuterDependencies = true
                    showInnerDependencies = false
            }
                """.stripIndent()
        when:
        def result = runTasksSuccessfully("build")

        then:
        result.standardOutput.contains("com.google.guava:guava 22.0 ->")
        !result.standardOutput.contains("ru.yandex.money.common:yamoney-json-utils 1.0.0 ->")
    }


    def "Does not print dependencies if branch is not feature"() {

        given:
        buildFile << """
            
            dependencies {
                compile 'ru.yandex.money.common:yamoney-json-utils:1.0.0',
                        'com.google.guava:guava:22.0'
                       
            } 
            
            ext.isFeatureBranch = false
            
            checkDependencies {
                    showOuterDependencies = true
            }
                """.stripIndent()
        when:
        def result = runTasksSuccessfully("build")

        then:
        !result.standardOutput.contains("com.google.guava:guava 22.0 ->")
        !result.standardOutput.contains("ru.yandex.money.common:yamoney-json-utils 1.0.0 ->")
    }
}
