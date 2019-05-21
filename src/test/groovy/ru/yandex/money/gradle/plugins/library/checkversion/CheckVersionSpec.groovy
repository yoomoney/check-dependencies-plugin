package ru.yandex.money.gradle.plugins.library.checkversion

import ru.yandex.money.gradle.plugins.library.AbstractPluginSpec


class CheckVersionSpec extends AbstractPluginSpec {

    def "Found conflict for yamoney libraries"() {

        given:
        buildFile << """
                   
            dependencies {
                compile 'ru.yandex.money.common:yamoney-json-utils:2.0.2',
                        'ru.yandex.money.common:yamoney-xml-utils:3.0.1',
                        'ru.yandex.money.common:yamoney-xml-utils:4.0.1',
                        'ru.yandex.money.common:yamoney-json-utils:4.0.3'
                       
            } 
            majorVersionChecker {
                    includeGroupIdPrefixes = ['ru.yamoney', 'ru.yandex.money']
            }
            
                """.stripIndent()

        when:
        def result = runTasksSuccessfully("dependencies")

        then:
        result.standardOutput.contains("There is major version conflict for dependency=ru.yandex.money.common:yamoney-xml-utils")
        result.standardOutput.contains("There is major version conflict for dependency=ru.yandex.money.common:yamoney-json-utils")
    }

    def "Not found conflict for outer libraries"() {

        given:
        buildFile << """
            dependencies {
                compile 'com.google.guava:guava:22.0',
                        'com.google.guava:guava:23.0'
                        
            }
            
            majorVersionChecker {
                    includeGroupIdPrefixes = ['ru.yamoney', 'ru.yandex.money']
            }
             
                """.stripIndent()
        when:
        def result = runTasksSuccessfully("dependencies")

        then:
        result.standardError.empty
    }

    def "Not found conflict for excluded libraries"() {

        given:
        buildFile << """
            dependencies {
                compile 'ru.yandex.money.common:yamoney-json-utils:2.0.2',
                        'ru.yandex.money.common:yamoney-xml-utils:3.0.1',
                        'ru.yandex.money.common:yamoney-xml-utils:4.0.1',
                        'ru.yandex.money.common:yamoney-json-utils:4.0.3'
                            
            }
            
            majorVersionChecker {
                    includeGroupIdPrefixes = ['ru.yamoney', 'ru.yandex.money']
                    excludeDependencies = ['ru.yandex.money.common:yamoney-json-utils', 'ru.yandex.money.common:yamoney-xml-utils']
            }
             
                """.stripIndent()
        when:
        def result = runTasksSuccessfully("dependencies")

        then:
        !result.standardOutput.contains("There is major version conflict for dependency=ru.yandex.money.common:yamoney-xml-utils")
        !result.standardOutput.contains("There is major version conflict for dependency=ru.yandex.money.common:yamoney-json-utils")
    }

    def "Found conflict for all libraries"() {

        given:
        buildFile << """
                dependencies {
                compile 'ru.yandex.money.common:yamoney-xml-utils:3.0.1',
                        'ru.yandex.money.common:yamoney-xml-utils:4.0.1',
                        'com.google.guava:guava:22.0',
                        'com.google.guava:guava:23.0'
                        
               } 
                
               
                """.stripIndent()
        when:
        def result = runTasksSuccessfully("dependencies")

        then:
        result.standardOutput.contains("There is major version conflict for dependency=com.google.guava:guava")
        result.standardOutput.contains("There is major version conflict for dependency=ru.yandex.money.common:yamoney-xml-utils")
    }


    def "Not found conflict for includeMajorVersionCheckLibraries"() {

        given:
        buildFile << """
                dependencies {
                compile 'ru.yandex.money.common:yamoney-xml-utils:3.0.1',
                        'ru.yandex.money.common:yamoney-xml-utils:4.0.1',
                        'ru.yandex.money.common:yamoney-enum-utils:2.0.2',
                        'ru.yandex.money.common:yamoney-enum-utils:4.0.3'
                        
               } 
               
               majorVersionChecker {
                    includeGroupIdPrefixes = ['ru.yamoney', 'ru.yandex.money']
                    excludeDependencies = ['ru.yandex.money.common:yamoney-enum-utils']

               }
                        
               
                """.stripIndent()
        when:
        def result = runTasksSuccessfully("dependencies")

        then:
        !(result.standardOutput.contains("There is major version conflict for dependency=ru.yandex.money.common:yamoney-enum-utils"))
        result.standardOutput.contains("There is major version conflict for dependency=ru.yandex.money.common:yamoney-xml-utils")
    }

    def "Found conflict, dependencies has major version equals '+'"() {

        given:
        buildFile << """
                dependencies {
                compile 'ru.yandex.money.common:yamoney-json-utils:1.0.2',
                        'ru.yandex.money.common:yamoney-json-utils:+'
                        
               } 
              
                """.stripIndent()
        when:
        def result = runTasksSuccessfully("dependencies")

        then:
        result.standardOutput.contains("There is major version conflict for dependency=ru.yandex.money.common:yamoney-json-utils")
    }

    def "Found conflict, dependencies has version with '+'"() {

        given:
        buildFile << """
                dependencies {
                compile 'ru.yandex.money.common:yamoney-json-utils:1.+',
                        'ru.yandex.money.common:yamoney-json-utils:2.+'
                        
               } 
              
                """.stripIndent()
        when:
        def result = runTasksSuccessfully("dependencies")

        then:
        result.standardOutput.contains("There is major version conflict for dependency=ru.yandex.money.common:yamoney-json-utils")
    }
}
