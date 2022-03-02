package ru.yoomoney.gradle.plugins.library.dependencies

import ru.yoomoney.gradle.plugins.library.AbstractPluginSpec
import ru.yoomoney.gradle.plugins.library.TestRepositories

/**
 * Функциональные тесты для CheckDependenciesPlugin, проверяющего корректность изменения версий используемых библиотек в проекте.
 *
 * @author Brovin Yaroslav
 * @since 30.01.2017
 */
class CheckDependenciesPluginSpec extends AbstractPluginSpec {

    def "check that all custom tasks exist"() {
        def expectedTasks = [CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME]

        when:
        def result = runTasksSuccessfully("tasks")

        then:
        expectedTasks.forEach({
            assert result.standardOutput.contains(it)
        })
    }


    def "success check without fixing library versions"() {
        when:
        def result = runTasksSuccessfully(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        !result.wasSkipped(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
        result.wasExecuted(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
    }

    def "success check on empty project libraries and fixed versions from Spring IO platform"() {
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

    def "success check on project libraries and empty fixed versions list"() {
        given:
        buildFile << """

                dependencyManagement {
                    // Запрещаем переопределять версии библиотек в обычной секции Gradle dependency
                    overriddenByDependencies = false
                }

                dependencies {
                    implementation 'org.springframework:spring-core:4.2.5.RELEASE'
                    implementation 'org.hamcrest:hamcrest-core:1.2'

                    testImplementation group: 'junit', name: 'junit', version: '4.11'
                }
                """.stripIndent()

        when:
        def result = runTasksSuccessfully(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        !result.wasSkipped(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
        result.wasExecuted(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
    }

    def "fail check on conflicted versions between fixed versions in IO platform and project dependencies section in libraries"() {
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
                    implementation 'org.springframework:spring-core:4.2.5.RELEASE'
                    // Ожидается 1.3
                    implementation 'org.hamcrest:hamcrest-core:1.2'

                    // Ожидается 4.12
                    // Использует org.hamcrest:hamcrest-core:1.3
                    testImplementation group: 'junit', name: 'junit', version: '4.11'
                }
                """.stripIndent()

        when:
        def result = runTasksSuccessfully(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        result.success
        result.standardOutput.contains("There are conflicts: [ConflictedLibraryInfo{library=org.springframework:spring-core," +
                " version='4.2.5.RELEASE', fixedVersion='4.2.7.RELEASE}, ConflictedLibraryInfo{library=org.hamcrest:hamcrest-core," +
                " version='1.2', fixedVersion='1.3}]")

        result.standardOutput.contains("There are conflicts: [ConflictedLibraryInfo{library=org.springframework:spring-core, " +
                "version='4.2.5.RELEASE', fixedVersion='4.2.7.RELEASE}, ConflictedLibraryInfo{library=junit:junit, version='4.11'," +
                " fixedVersion='4.12}, ConflictedLibraryInfo{library=org.hamcrest:hamcrest-core, version='1.2', fixedVersion='1.3}]")
    }

    def "fail check on project libraries and fixed versions, which are override in build script"() {
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

                    testImplementation {
                        dependencies {
                            dependency 'junit:junit:4.11'
                            dependency 'org.hamcrest:hamcrest-core:1.3' // platform fixed 1.2
                        }
                    }
                }

                dependencies {
                    // Ожидается 4.2.5.RELEASE
                    implementation 'org.springframework:spring-core:4.2.5.RELEASE'
                    // Ожидается 1.2
                    implementation 'org.hamcrest:hamcrest-core:1.2'
                    //Использует slf4j-api:1.20, ожидается slf4j-api:1.21
                    implementation 'ch.qos.logback:logback-classic:1.1.7'

                    // Ожидается 4.11
                    // Использует org.hamcrest:hamcrest-core:1.3
                    testImplementation group: 'junit', name: 'junit', version: '4.11'
                }
            """.stripIndent()
        when:
        def result = runTasksSuccessfully(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        result.success
        result.standardOutput.contains("There are conflicts: [ConflictedLibraryInfo{library=org.slf4j:slf4j-api, version='1.7.20', fixedVersion='1.7.21}]")

        result.standardOutput.contains("There are conflicts: [ConflictedLibraryInfo{library=org.hamcrest:hamcrest-core, version='1.2'," +
                " fixedVersion='1.3}, ConflictedLibraryInfo{library=org.slf4j:slf4j-api, version='1.7.20', fixedVersion='1.7.21}]")

    }

    def "success check on project libraries and fixed versions and rules of changing libraries versions"() {
        given:
        def exclusionFile = new File(projectDir, 'exclusion.properties')
        exclusionFile << """
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

                    testImplementation {
                        dependencies {
                            dependency 'junit:junit:4.11'
                            dependency 'org.hamcrest:hamcrest-core:1.3' // platform fixed 1.2
                        }
                    }
                }

                dependencies {
                    // Ожидается 4.2.5.RELEASE
                    implementation 'org.springframework:spring-core:4.2.5.RELEASE'
                    // Ожидается 1.2
                    implementation 'org.hamcrest:hamcrest-core:1.2'

                    // Ожидается 4.11
                    // Использует org.hamcrest:hamcrest-core:1.3
                    testImplementation group: 'junit', name: 'junit', version: '4.11'
                }
            """.stripIndent()
        when:
        def result = runTasksSuccessfully(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        !result.wasSkipped(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
        result.wasExecuted(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
    }

    def "success check on project libraries and excluded compile configuration"() {
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

                    testImplementation {
                        dependencies {
                            dependency 'junit:junit:4.11'
                            dependency 'org.hamcrest:hamcrest-core:1.3' // platform fixed 1.2
                        }
                    }
                }

                checkDependencies {
                    includedConfigurations = ["compileClasspath", "default"]
                }
                dependencies {
                    // Ожидается 4.2.5.RELEASE
                    implementation 'org.springframework:spring-core:4.2.5.RELEASE'

                    // Ожидается 1.2
                    implementation 'org.hamcrest:hamcrest-core:1.2'

                    // Ожидается 4.11
                    // Использует org.hamcrest:hamcrest-core:1.3
                    testImplementation group: 'junit', name: 'junit', version: '4.11'
                }
                """.stripIndent()

        when:
        def result = runTasksSuccessfully(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        !result.wasSkipped(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
        result.wasExecuted(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
    }

    def 'success check on project with unresolved dependency'() {
        given:
        buildFile << """

            dependencyManagement {
                overriddenByDependencies = false

                imports {
                    mavenBom 'io.spring.platform:platform-bom:2.0.6.RELEASE'
                }
            }

            // нет конфликтов версий с требуемыми в dependencyManagement секции
            dependencies {
                implementation 'unresolved:dependency'
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        !result.wasSkipped(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
        result.wasExecuted(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
    }

    def 'failed check on project with conflicts on unresolved dependency'() {
        given:
        buildFile << """

            dependencyManagement {
                overriddenByDependencies = false

                imports {
                    mavenBom 'io.spring.platform:platform-bom:2.0.6.RELEASE'
                }

                dependencies {
                    dependency 'unresolved:dependency:1.0.0'
                }
            }

            // нет конфликтов версий с требуемыми в dependencyManagement секции
            dependencies {
                implementation 'unresolved:dependency:2.0.0'
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        result.success
        result.standardOutput.contains("[ConflictedLibraryInfo{library=unresolved:dependency, version='2.0.0', fixedVersion='1.0.0}]")
    }

    def 'check that conflicts are not resolved when no version selectors are specified'() {
        given:
        buildFile << """
            repositories {
                maven { url '$TestRepositories.MAVEN_REPO_1' }
            }

            dependencyManagement {
                overriddenByDependencies = false

                dependencies {
                    dependency 'test:alpha:5.1.0'
                }
            }

            dependencies {
                implementation 'test:beta:4.1.0'
            }
        """

        when:
        def result = runTasksSuccessfully(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        result.success
        result.standardOutput.contains("[ConflictedLibraryInfo{library=test:alpha, version='4.1.0', fixedVersion='5.1.0}]")
    }
}
