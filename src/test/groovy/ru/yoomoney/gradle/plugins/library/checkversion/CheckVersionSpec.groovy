package ru.yoomoney.gradle.plugins.library.checkversion

import ru.yoomoney.gradle.plugins.library.AbstractPluginSpec


class CheckVersionSpec extends AbstractPluginSpec {

    def "Found conflict for libraries in inclusion list"() {

        given:
        buildFile << """
                   
            dependencies {
                implementation 'org.apache.tomcat.embed:tomcat-embed-core:10.0.0',
                        'joda-time:joda-time:1.6.2',
                        'joda-time:joda-time:2.10.9',
                        'org.apache.tomcat.embed:tomcat-embed-core:9.0.10'
                       
            } 
            majorVersionChecker {
                    includeGroupIdPrefixes = ['org.apache', 'joda-time']
            }
            
                """.stripIndent()

        when:
        def result = runTasksSuccessfully("dependencies")

        then:
        result.standardError.contains("There is major version conflict for dependency=org.apache.tomcat.embed:tomcat-embed-core")
        result.standardError.contains("There is major version conflict for dependency=joda-time:joda-time")
    }

    def "Found conflict for transitive dependencies"() {

        given:
        buildFile << """
                   
            dependencies {
                implementation 'org.apache.tomcat:tomcat-annotations-api:9.0.8',
                        'org.apache.tomcat.embed:tomcat-embed-core:10.0.0'
                       
            } 
            majorVersionChecker {
                    includeGroupIdPrefixes = ['org.apache']
            }
            
                """.stripIndent()

        when:
        def result = runTasksSuccessfully("dependencies")

        then:
        result.standardError.contains("There is major version conflict for dependency=org.apache.tomcat:tomcat-annotations-api")
    }

    def "Not found conflict for different configuration"() {

        given:
        buildFile << """
                   
            dependencies {
                implementation 'org.apache.tomcat.embed:tomcat-embed-core:10.0.0',
                        'joda-time:joda-time:1.6.2'
                       
                archives 'org.apache.tomcat.embed:tomcat-embed-core:9.0.10',
                        'joda-time:joda-time:2.10.9'
                       
            } 
            majorVersionChecker {
                    includeGroupIdPrefixes = ['org.apache', 'joda-time']
            }
            
                """.stripIndent()

        when:
        def result = runTasksSuccessfully("dependencies")

        then:
        !result.standardError.contains("There is major version conflict for dependency=org.apache.tomcat.embed:tomcat-embed-core")
        !result.standardError.contains("There is major version conflict for dependency=joda-time:joda-time")
    }

    def "Not found conflict for other libraries"() {

        given:
        buildFile << """
            dependencies {
                implementation 'com.google.guava:guava:22.0',
                        'com.google.guava:guava:23.0'
                        
            }
            
            majorVersionChecker {
                    includeGroupIdPrefixes = ['org.apache', 'joda-time']
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
                implementation 'org.apache.tomcat.embed:tomcat-embed-core:10.0.0',
                        'joda-time:joda-time:1.6.2',
                        'joda-time:joda-time:2.10.9',
                        'org.apache.tomcat.embed:tomcat-embed-core:9.0.10'
                            
            }
            
            majorVersionChecker {
                    includeGroupIdPrefixes = ['org.apache', 'joda-time']
                    excludeDependencies = ['joda-time:joda-time', 'org.apache.tomcat.embed:tomcat-embed-core']
            }
             
                """.stripIndent()
        when:
        def result = runTasksSuccessfully("dependencies")

        then:
        !result.standardError.contains("There is major version conflict for dependency=joda-time:joda-time")
        !result.standardError.contains("There is major version conflict for dependency=org.apache.tomcat.embed:tomcat-embed-core")
    }

    def "Found conflict for all libraries"() {

        given:
        buildFile << """
                dependencies {
                implementation 'joda-time:joda-time:1.6.2',
                        'joda-time:joda-time:2.10.9',
                        'com.google.guava:guava:22.0',
                        'com.google.guava:guava:23.0'
                        
               } 
                
               
                """.stripIndent()
        when:
        def result = runTasksSuccessfully("dependencies")

        then:
        result.standardError.contains("There is major version conflict for dependency=com.google.guava:guava")
        result.standardError.contains("There is major version conflict for dependency=joda-time")
    }


    def "Not found conflict for includeMajorVersionCheckLibraries"() {

        given:
        buildFile << """
                dependencies {
                implementation 'org.apache.tomcat.embed:tomcat-embed-core:10.0.0',
                        'joda-time:joda-time:1.6.2',
                        'joda-time:joda-time:2.10.9',
                        'org.apache.tomcat.embed:tomcat-embed-core:9.0.10'
                        
               } 
               
               majorVersionChecker {
                    includeGroupIdPrefixes = ['org.apache', 'joda-time']
                    excludeDependencies = ['joda-time:joda-time']

               }
                        
               
                """.stripIndent()
        when:
        def result = runTasksSuccessfully("dependencies")

        then:
        !(result.standardError.contains("There is major version conflict for dependency=joda-time:joda-time"))
        result.standardError.contains("There is major version conflict for dependency=org.apache.tomcat.embed:tomcat-embed-core")
    }

    def "Found conflict, dependencies has major version equals '+'"() {

        given:
        buildFile << """
                dependencies {
                implementation 'joda-time:joda-time:1.6.2',
                        'joda-time:joda-time:+'
                        
               } 
              
                """.stripIndent()
        when:
        def result = runTasksSuccessfully("dependencies")

        then:
        result.standardError.contains("There is major version conflict for dependency=joda-time:joda-time")
    }

    def "Found conflict, dependencies has version with '+'"() {

        given:
        buildFile << """
                dependencies {
                implementation 'joda-time:joda-time:1.+',
                        'joda-time:joda-time:2.+'
                        
               } 
              
                """.stripIndent()
        when:
        def result = runTasksSuccessfully("dependencies")

        then:
        result.standardError.contains("There is major version conflict for dependency=joda-time:joda-time")
    }
}
