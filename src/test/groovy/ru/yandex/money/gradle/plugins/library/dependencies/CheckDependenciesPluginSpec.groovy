package ru.yandex.money.gradle.plugins.library.dependencies

import ru.yandex.money.gradle.plugins.library.AbstractPluginSpec
import ru.yandex.money.gradle.plugins.library.TestRepositories

/**
 * Функциональные тесты для CheckDependenciesPlugin, проверяющего корректность изменения версий используемых библиотек в проекте.
 *
 * @author Brovin Yaroslav (brovin@yamoney.ru)
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
                repositories {
                    maven { url 'https://nexus.yamoney.ru/content/repositories/central/' }
                }

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

    def "Not found conflict for excludedVersionConflictLibraries"() {

        given:
        buildFile << """
                dependencies {
                implementation 'ru.yandex.money.common:yamoney-xml-utils:3.0.1',
                        'ru.yandex.money.common:yamoney-xml-utils:4.0.1',
                        'ru.yandex.money.common:yamoney-enum-utils:2.0.2',
                        'ru.yandex.money.common:yamoney-enum-utils:4.0.3'
               } 
               
               majorVersionChecker {
                    excludeDependencies = ['ru.yandex.money.common:yamoney-enum-utils']
               }
                        
               
                """.stripIndent()
        when:
        def result = runTasksSuccessfully("dependencies")

        then:
        !(result.standardOutput.contains("There is major version conflict for dependency=ru.yandex.money.common:yamoney-enum-utils"))
        result.standardOutput.contains("There is major version conflict for dependency=ru.yandex.money.common:yamoney-xml-utils")
    }

    def "success check on project libraries and empty fixed versions list"() {
        given:
        buildFile << """
                repositories {
                    maven { url 'https://nexus.yamoney.ru/content/repositories/central/' }
                }

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
                repositories {
                    maven { url 'https://nexus.yamoney.ru/content/repositories/central/' }
                }

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
        def result = runTasksWithFailure(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        result.failure
    }

    def "fail check on project libraries and fixed versions, which are override in build script"() {
        given:
        buildFile << """
                repositories {
                    maven { url 'https://nexus.yamoney.ru/content/repositories/central/' }
                }

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
        def result = runTasksWithFailure(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        result.failure
        println result.standardError
    }

    def "success check on project libraries and fixed versions and rules of changing libraries versions"() {
        given:
        def exclusionFile = new File(projectDir, 'exclusion.properties')
        exclusionFile << """
            org.hamcrest.hamcrest-core = 1.2 -> 1.3
        """.stripIndent()

        buildFile << """
                repositories {
                    maven { url 'https://nexus.yamoney.ru/content/repositories/central/' }
                }

                buildscript {
                    dependencies {
                        classpath group: 'ru.yandex.money.platform', name: 'yamoney-libraries-dependencies', version: '2.+', ext: 'zip'
                    }
                }

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

                // Указываем путь к файлу с разрешающими правилами изменения версий библиотек
                checkDependencies {
                    exclusionsRulesSources = ['$exclusionFile.absolutePath',
                                              "ru.yandex.money.platform:yamoney-libraries-dependencies:"]
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

    def "success check on empty project libraries and wrong exclusions: empty param"() {
        given:
        buildFile << """
                // Не указываем файлы
                checkDependencies {
                    exclusionsRulesSources = []
                }
                """.stripIndent()

        when:
        def result = runTasksSuccessfully(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        !result.wasSkipped(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
        result.wasExecuted(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
    }

    def "success check on empty project libraries and wrong exclusions: null param"() {
        given:
        buildFile << """
                // Указываем null список
                checkDependencies {
                    exclusionsRulesSources = null
                }
                """.stripIndent()

        when:
        def result = runTasksSuccessfully(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        !result.wasSkipped(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
        result.wasExecuted(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
    }

    def "success check on empty project libraries and wrong exclusions: wrong file param"() {
        given:
        buildFile << """
                // Указываем путь к несуществующему файлу
                checkDependencies {
                    exclusionsRulesSources = ["not_existed_file.properties"]
                }
                """.stripIndent()

        when:
        def result = runTasksSuccessfully(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        !result.wasSkipped(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
        result.wasExecuted(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
    }

    def "success check on empty project libraries and wrong exclusions: wrong artifact param"() {
        given:
        buildFile << """
                // Указываем путь к несуществующему файлу
                checkDependencies {
                    exclusionsRulesSources = ["ru.yandex.fakegroup:fakeartifact:"]
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
                repositories {
                    maven { url 'https://nexus.yamoney.ru/content/repositories/central/' }
                }

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

                // Указываем путь к несуществующему файлу
                checkDependencies {
                    excludedConfigurations = ["testImplementation", "testRuntime", 
                    "testCompileClasspath", "testRuntimeClasspath"]
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

    def 'fail check on project with stale local exclusion rules'() {
        given:
        def exclusionFile = new File(projectDir, 'exclusion.properties')
        exclusionFile << """
            org.hamcrest.hamcrest-core = 1.0, 1.2 -> 1.3
        """.stripIndent()

        buildFile << """
            repositories {
                maven { url 'https://nexus.yamoney.ru/content/repositories/central/' }
            }

            // ожидается org.hamcrest:hamcrest-core:1.3
            dependencyManagement {
                // Запрещаем переопределять версии библиотек в обычной секции Gradle dependencies
                overriddenByDependencies = false

                // Наследуем правила фиксации версии от Spring-Boot 1.3.3
                imports {
                    mavenBom 'org.springframework.boot:spring-boot-dependencies:1.5.2.RELEASE'
                }
            }

            // используется только правило org.hamcrest.hamcrest-core:1.2 -> 1.3
            // правило org.hamcrest.hamcrest-core:1.0 -> 1.3 'просрочено'
            dependencies {
                implementation 'org.hamcrest:hamcrest-core:1.2'
            }

            checkDependencies.exclusionsRulesSources = ['$exclusionFile.absolutePath']
        """.stripIndent()

        when:
        def result = runTasksWithFailure(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        result.failure
        result.standardOutput.contains('stale exclusions')
    }

    def 'success check on project with stale imported exclusion rules'() {
        given:
        buildFile << """
            repositories {
                maven { url 'https://nexus.yamoney.ru/content/repositories/public/' }
                maven { url 'https://nexus.yamoney.ru/content/repositories/releases/' }
            }

            dependencyManagement {
                overriddenByDependencies = false

                imports {
                    mavenBom 'ru.yandex.money.platform:yamoney-libraries-dependencies:1.+'
                }
            }

            // нет конфликтов версий с требуемыми в dependencyManagement секции
            dependencies {
                implementation 'junit:junit:4.12'
            }

            // загружаем правила исключений из yamoney-libraries-dependencies
            checkDependencies.exclusionsRulesSources = ['ru.yandex.money.platform:yamoney-libraries-dependencies']
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
            repositories {
                maven { url 'https://nexus.yamoney.ru/content/repositories/public/' }
                maven { url 'https://nexus.yamoney.ru/content/repositories/releases/' }
            }

            dependencyManagement {
                overriddenByDependencies = false

                imports {
                    mavenBom 'ru.yandex.money.platform:yamoney-libraries-dependencies:1.+'
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

    def 'success read exclusion rules with library name in different formats'() {
        given:
        def exclusionFile = new File(projectDir, 'exclusion.properties')
        exclusionFile << """
            ## new library name format
            org.hamcrest:hamcrest-core = 1.0 -> 1.3

            ## deprecated library name format
            junit.junit = 4.11 -> 4.12
        """.stripIndent()

        buildFile << """
            repositories {
                maven { url 'https://nexus.yamoney.ru/content/repositories/central/' }
            }

            dependencyManagement {
                // Запрещаем переопределять версии библиотек в обычной секции Gradle dependencies
                overriddenByDependencies = false

                dependencies {
                    dependency 'org.hamcrest:hamcrest-core:1.3'
                    dependency 'junit:junit:4.12'
                }
            }

            dependencies {
                implementation 'org.hamcrest:hamcrest-core:1.0'
                implementation 'junit:junit:4.11'
            }

            checkDependencies.exclusionsRulesSources = ['$exclusionFile.absolutePath']
        """.stripIndent()

        when:
        def result = runTasksSuccessfully(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        result.success
    }

    def 'success check on project with exclusion rule in incorrect format'() {
        given:
        def exclusionFile = new File(projectDir, 'exclusion.properties')
        exclusionFile << """
            ## incorrect library name format
            junit-junit = 4.11 -> 4.12
        """.stripIndent()

        buildFile << """
            repositories {
                maven { url 'https://nexus.yamoney.ru/content/repositories/central/' }
            }

            dependencyManagement {
                // Запрещаем переопределять версии библиотек в обычной секции Gradle dependencies
                overriddenByDependencies = false

                dependencies {
                    dependency 'junit:junit:4.12'
                                    implementation 'junit:junit:4.11'
            }

            checkDependencies.exclusionsRulesSources = ['$exclusionFile.absolutePath']
        """.stripIndent()

        when:
        def result = runTasksWithFailure(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then: 'rule was ignored'
        result.failure
    }

    def 'failed check on project with conflicts on unresolved dependency'() {
        given:
        buildFile << """
            repositories {
                maven { url 'https://nexus.yamoney.ru/content/repositories/public/' }
                maven { url 'https://nexus.yamoney.ru/content/repositories/releases/' }
            }

            dependencyManagement {
                overriddenByDependencies = false

                imports {
                    mavenBom 'ru.yandex.money.platform:yamoney-libraries-dependencies:1.+'
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
        def result = runTasksWithFailure(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        !result.wasSkipped(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
        result.wasExecuted(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
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
        def result = runTasksWithFailure(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        result.wasExecuted(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
        result.standardOutput.contains("NO SOLUTIONS FOUND")
    }

    def 'check that conflicts for given dependency are resolved when version selector is specified'() {
        given:
        buildFile << """
            repositories {
                maven { url '$TestRepositories.MAVEN_REPO_1' }
            }

            dependencyManagement {
                overriddenByDependencies = false

                dependencies {
                    dependency 'test:alpha:5.1.0'
                    dependency 'test:zeta:6.2.0'
                }
            }

            dependencies {
                // depends on test:alpha:4.1.0
                implementation 'test:beta:4.1.0'

                implementation 'test:zeta:5.2.0'
            }

            checkDependencies.versionSelectors = [
                'test:beta': { it.tokenize(".")[0].toInteger() > 5 },
            ]
        """

        when:
        def result = runTasksWithFailure(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        result.wasExecuted(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
        result.standardOutput.contains("[6.2.0]")
        !result.standardOutput.contains("[5.1.0]")
    }

    def 'check that conflicts for given dependency are resolved when version selector for given dependency is not specified'() {
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

            checkDependencies.versionSelectors = [
                'test:zeta': { it.tokenize(".")[0].toInteger() > 5 },
            ]
        """

        when:
        def result = runTasksWithFailure(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)

        then:
        result.wasExecuted(CheckDependenciesPlugin.CHECK_DEPENDENCIES_TASK_NAME)
        result.standardOutput.contains("NO SOLUTIONS FOUND")
    }
}
