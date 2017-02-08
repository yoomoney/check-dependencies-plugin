package ru.yandex.money.gradle.plugins.library.dependencies

import ru.yandex.money.gradle.plugins.library.AbstractPluginSpec

/**
 * Функциональные тесты для CheckDependenciesPlugin, проверяющего корректность изменения версий используемых библиотек в проекте.
 *
 * @author Brovin Yaroslav (brovin@yamoney.ru)
 * @since 30.01.2017
 */
class CheckDependenciesPluginSpec extends AbstractPluginSpec {

    def "success CheckDependenciesTask without fixing library versions successfully"() {
        given:
        buildFile << """
                buildscript {
                    repositories {
                        maven { url 'http://nexus.yamoney.ru/content/repositories/central/' }
                        maven { url 'http://nexus.yamoney.ru/content/repositories/releases/' }
                    }
                    dependencies {
                        classpath 'io.spring.gradle:dependency-management-plugin:0.6.1.RELEASE'
                    }
                }
                """.stripIndent()

        when:
        def result = runTasksSuccessfully(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        !result.wasSkipped(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
        result.wasExecuted(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
    }

    def "success CheckDependenciesTask on checking empty project libraries and fixed versions from Spring IO platform"() {
        given:
        buildFile << """
                dependencyManagement {                    
                    // Фиксируем версии библиотек из pom.xml файла
                    imports {
                        mavenBom 'io.spring.platform:platform-bom:2.0.6.RELEASE'
                    }
                }
                """.stripIndent()

        when:
        def result = runTasksSuccessfully(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        !result.wasSkipped(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
        result.wasExecuted(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
    }

    def "success CheckDependenciesTask on checking project libraries and empty fixed versions list"() {
        given:
        buildFile << """
                dependencyManagement {                
                    // Запрещаем переопределять версии библиотек в обычной секции Gradle dependency
                    overriddenByDependencies = false
                }
                
                dependencies {
                    compile 'org.springframework:spring-core:4.2.5.RELEASE'
                    compile 'org.hamcrest:hamcrest-core:1.2'
                    
                    testCompile group: 'junit', name: 'junit', version: '4.11'
                }
                """.stripIndent()

        when:
        def result = runTasksSuccessfully(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        !result.wasSkipped(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
        result.wasExecuted(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
    }

    def "fail CheckDependenciesTask on checking conflicted versions between fixed versions in IO platform and project dependencies section in libraries"() {
        given:
        buildFile << """
                dependencyManagement {                
                    // Запрещаем переопределять версии библиотек в обычной секции Gradle dependency
                    overriddenByDependencies = false
                    
                    // Фиксируем версии библиотек из pom.xml файла
                    imports {
                        mavenBom 'io.spring.platform:platform-bom:2.0.6.RELEASE'
                    }
                }
                
                // Специально определяем версии библиотек, которые конфликтуют с зафиксированными
                dependencies {
                    // Ожидается 4.2.7.RELEASE
                    compile 'org.springframework:spring-core:4.2.5.RELEASE'
                    // Ожидается 1.3
                    compile 'org.hamcrest:hamcrest-core:1.2'
                
                    // Ожидается 4.12
                    // Использует org.hamcrest:hamcrest-core:1.3
                    testCompile group: 'junit', name: 'junit', version: '4.11'
                }
                """.stripIndent()

        when:
        def result = runTasksWithFailure(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        result.failure
    }

    def "success CheckDependenciesTask on checking project libraries and fixed versions, which are override in build script"() {
        given:
        buildFile << """
                dependencyManagement {                
                    // Запрещаем переопределять версии библиотек в обычной секции Gradle dependency
                    overriddenByDependencies = false
                    
                    // Фиксируем версии библиотек из pom.xml файла
                    imports {
                        mavenBom 'io.spring.platform:platform-bom:2.0.6.RELEASE'
                    }
                    
                    dependencies {
                        dependency 'org.springframework:spring-core:4.2.5.RELEASE'
                        dependency 'org.hamcrest:hamcrest-core:1.2'
                    }
                    
                    testCompile {
                        dependencies {
                            dependency 'junit:junit:4.11'
                            dependency 'org.hamcrest:hamcrest-core:1.3' // platform fixed 1.2
                        }
                    }                    
                }
                
                dependencies {
                    // Ожидается 4.2.5.RELEASE
                    compile 'org.springframework:spring-core:4.2.5.RELEASE'
                    // Ожидается 1.2
                    compile 'org.hamcrest:hamcrest-core:1.2'
                
                    // Ожидается 4.11
                    // Использует org.hamcrest:hamcrest-core:1.3
                    testCompile group: 'junit', name: 'junit', version: '4.11'
                }            
            """.stripIndent()
        when:
        def result = runTasksWithFailure(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        result.failure
    }

    def "success CheckDependenciesTask on project libraries and fixed versions and rules of changing libraries versions"() {
        given:
        File exclusionFile = new File(projectDir, 'exclusion.properties')
        exclusionFile<<"""
            org.hamcrest.hamcrest-core = 1.2 -> 1.3
        """.stripIndent()

        buildFile << """
                dependencyManagement {                
                    // Запрещаем переопределять версии библиотек в обычной секции Gradle dependency
                    overriddenByDependencies = false
                    
                    // Фиксируем версии библиотек из pom.xml файла
                    imports {
                        mavenBom 'io.spring.platform:platform-bom:2.0.6.RELEASE'
                    }
                    
                    dependencies {
                        dependency 'org.springframework:spring-core:4.2.5.RELEASE'
                        dependency 'org.hamcrest:hamcrest-core:1.2'
                    }
                    
                    testCompile {
                        dependencies {
                            dependency 'junit:junit:4.11'
                            dependency 'org.hamcrest:hamcrest-core:1.3' // platform fixed 1.2
                        }
                    }                    
                }
                
                // Указываем путь к файлу с разрешающими правилами изменения версий библиотек
                checkDependencies {
                    exclusionsFileName = '$exclusionFile.absolutePath'
                }
                
                dependencies {
                    // Ожидается 4.2.5.RELEASE
                    compile 'org.springframework:spring-core:4.2.5.RELEASE'
                    // Ожидается 1.2
                    compile 'org.hamcrest:hamcrest-core:1.2'
                
                    // Ожидается 4.11
                    // Использует org.hamcrest:hamcrest-core:1.3
                    testCompile group: 'junit', name: 'junit', version: '4.11'
                }            
            """.stripIndent()
        when:
        def result = runTasksSuccessfully(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        !result.wasSkipped(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
        result.wasExecuted(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
    }

    def "success CheckDependenciesTask with wrong exlusions rules file"() {
        given:
        buildFile << """
                buildscript {
                    repositories {
                        maven { url 'http://nexus.yamoney.ru/content/repositories/central/' }
                        maven { url 'http://nexus.yamoney.ru/content/repositories/releases/' }
                    }
                    dependencies {
                        classpath 'io.spring.gradle:dependency-management-plugin:0.6.1.RELEASE'
                    }
                }
                
                // Указываем путь к несуществующему файлу
                checkDependencies {
                    exclusionsFileName = "not_existed_file.properties"
                }
                """.stripIndent()

        when:
        def result = runTasksSuccessfully(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        !result.wasSkipped(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
        result.wasExecuted(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
    }
}
