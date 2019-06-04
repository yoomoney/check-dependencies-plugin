package ru.yandex.money.gradle.plugins.library.forbiddenartifacts

import org.gradle.internal.impldep.org.hamcrest.CoreMatchers.not
import org.gradle.internal.impldep.org.hamcrest.CoreMatchers.containsString
import org.gradle.internal.impldep.org.hamcrest.MatcherAssert.assertThat
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Тесты ForbiddenArifactsTask
 *
 * @author horyukova
 * @since 28.05.2019
 */

class ForbiddenArifactsSpec {

    private val testProjectDir = TemporaryFolder()
    private lateinit var setupBuildFile: String
    private lateinit var buildFile: File

    @Before
    fun setup() {
        testProjectDir.create()
        buildFile = testProjectDir.newFile("build.gradle")

        setupBuildFile = """

                buildscript {
                    repositories {
                        maven { url 'http://nexus.yamoney.ru/content/repositories/thirdparty/' }
                        maven { url 'http://nexus.yamoney.ru/content/repositories/central/' }
                        maven { url 'http://nexus.yamoney.ru/content/repositories/releases/' }
                        maven { url 'http://nexus.yamoney.ru/content/repositories/public/' }
                    }
                    dependencies {
                        classpath 'io.spring.gradle:dependency-management-plugin:1.0.1.RELEASE'
                    }
                }
                plugins {
                    id 'java'
                    id 'io.spring.dependency-management'
                    id 'yamoney-check-dependencies-plugin'
                }

        """
    }

    @Test
    fun `should found forbidden dependency`() {

        buildFile.writeText(setupBuildFile + """
                dependencies {
                    compile 'ru.yandex.money.common:yamoney-json-utils:2.0.2',
                    'ru.yandex.money.common:yamoney-xml-utils:3.0.1',
                    'ru.yandex.money.common:yamoney-xml-utils:4.0.1',
                    'ru.yandex.money.common:yamoney-json-utils:4.0.3'

                }
                forbiddenDependenciesChecker {
                    eq {
                        forbidden 'ru.yandex.money.common:yamoney-xml-utils:4.0.1'
                        recommended '4.+'
                        comment 'bla bla'
                    }
               }
            """.trimIndent())

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("checkForbiddenDependencies")
                .withPluginClasspath()
                .withDebug(true)
                .buildAndFail()

        assertThat(result.output, containsString("There is forbidden dependencies"))

        assertThat(result.output,
                containsString("Forbidden dependency: ru.yandex.money.common:yamoney-xml-utils:4.0.1"))
        assertThat(result.output,
                containsString("Recommended version: ru.yandex.money.common:yamoney-xml-utils 4.0.1 -> "))
    }

    @Test
    fun `should found forbidden dependencies`() {

        buildFile.writeText(setupBuildFile + """
                dependencies {
                    compile 'ru.yandex.money.common:yamoney-json-utils:2.0.2',
                    'ru.yandex.money.common:yamoney-xml-utils:3.0.1',
                    'ru.yandex.money.common:yamoney-xml-utils:4.0.1',
                    'ru.yandex.money.common:yamoney-json-utils:4.0.3'

                }
                forbiddenDependenciesChecker {
                    eq {
                        forbidden 'ru.yandex.money.common:yamoney-json-utils:4.0.3'
                        recommended '4.0.+'
                        comment 'bla bla'
                    }
                    eq {
                        forbidden 'ru.yandex.money.common:yamoney-xml-utils:4.0.1'
                        recommended '4.0.0'
                        comment 'bla bla'
                    }
               }
            """.trimIndent())

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("checkForbiddenDependencies")
                .withPluginClasspath()
                .withDebug(true)
                .buildAndFail()

        assertThat(result.output, containsString("There is forbidden dependencies"))

        assertThat(result.output,
                containsString("Forbidden dependency: ru.yandex.money.common:yamoney-xml-utils:4.0.1"))
        assertThat(result.output,
                containsString("Recommended version: ru.yandex.money.common:yamoney-xml-utils 4.0.1 -> "))

        assertThat(result.output,
                containsString("Forbidden dependency: ru.yandex.money.common:yamoney-json-utils:4.0.3"))
        assertThat(result.output,
                containsString("Recommended version: ru.yandex.money.common:yamoney-json-utils 4.0.3 -> "))
    }

    @Test
    fun `should found forbidden dependencies when checkLibraryDependencies`() {

        buildFile.writeText(setupBuildFile + """
                dependencies {
                    compile 'ru.yandex.money.common:yamoney-json-utils:2.0.2',
                    'ru.yandex.money.common:yamoney-xml-utils:3.0.1',
                    'ru.yandex.money.common:yamoney-xml-utils:4.0.1',
                    'ru.yandex.money.common:yamoney-json-utils:4.0.3'

                }
                forbiddenDependenciesChecker {
                    eq {
                        forbidden 'ru.yandex.money.common:yamoney-xml-utils:4.0.1'
                        recommended '4.0.0'
                        comment 'bla bla'
                    }
               }
            """.trimIndent())

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("checkLibraryDependencies")
                .withPluginClasspath()
                .withDebug(true)
                .buildAndFail()

        assertThat(result.output, containsString("There is forbidden dependencies"))

        assertThat(result.output,
                containsString("Forbidden dependency: ru.yandex.money.common:yamoney-xml-utils:4.0.1"))
        assertThat(result.output,
                containsString("Recommended version: ru.yandex.money.common:yamoney-xml-utils 4.0.1 -> "))
    }

    @Test
    fun `should not found forbidden dependencies`() {

        buildFile.writeText(setupBuildFile + """
                dependencies {
                    compile 'ru.yandex.money.common:yamoney-json-utils:2.0.2',
                    'ru.yandex.money.common:yamoney-xml-utils:3.0.1',
                    'ru.yandex.money.common:yamoney-xml-utils:4.0.1',
                    'ru.yandex.money.common:yamoney-json-utils:4.0.3'

                }
                forbiddenDependenciesChecker {
                    eq {
                        forbidden 'ru.yandex.money.common:yamoney-enum-utils:4.0.2'
                        recommended '4.0.0'
                        comment 'bla bla'
                    }
                }
            """.trimIndent())

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("checkForbiddenDependencies")
                .withPluginClasspath()
                .withDebug(true)
                .build()

        assertThat(result.output, not(containsString("There is forbidden dependencies")))
    }

    @Test
    fun `should not found forbidden dependencies when forbiddenArtifactsChecker absent`() {

        buildFile.writeText(setupBuildFile + """
                dependencies {
                    compile 'ru.yandex.money.common:yamoney-json-utils:2.0.2',
                    'ru.yandex.money.common:yamoney-xml-utils:3.0.1',
                    'ru.yandex.money.common:yamoney-xml-utils:4.0.1',
                    'ru.yandex.money.common:yamoney-json-utils:4.0.3'

                }
            """.trimIndent())

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("checkForbiddenDependencies")
                .withPluginClasspath()
                .withDebug(true)
                .build()

        assertThat(result.output, not(containsString("There is forbidden dependencies")))
    }

    @Test
    fun `should found forbidden dependencies less version`() {

        buildFile.writeText(setupBuildFile + """
                dependencies {
                    compile 'ru.yandex.money.common:yamoney-enum-utils:2.0.2',
                    'ru.yandex.money.common:yamoney-enum-utils:2.1.5',
                    'ru.yandex.money.common:yamoney-xml-utils:3.0.1',
                    'ru.yandex.money.common:yamoney-xml-utils:4.0.1',
                    'ru.yandex.money.common:yamoney-json-utils:4.0.3',
                    'ru.yandex.money.common:yamoney-json-utils:4.2.3'
                }
                forbiddenDependenciesChecker {
                    before {
                        forbidden 'ru.yandex.money.common:yamoney-xml-utils:4.0.0'
                        recommended '4.0.1'
                        comment 'bla bla'
                    }

                    before {
                        forbidden 'ru.yandex.money.common:yamoney-json-utils:4.2.0'
                        recommended '4.2.2'
                        comment 'bla bla'
                    }

                    before {
                        forbidden 'ru.yandex.money.common:yamoney-enum-utils:2.1.4'
                        recommended '2.2.0'
                        comment 'bla bla'
                    }
                }
            """.trimIndent())

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("checkForbiddenDependencies")
                .withPluginClasspath()
                .withDebug(true)
                .buildAndFail()

        assertThat(result.output, containsString("There is forbidden dependencies"))

        assertThat(result.output,
                containsString("Forbidden dependency: ru.yandex.money.common:yamoney-xml-utils:3.0.1"))
        assertThat(result.output,
                not(containsString("Forbidden dependency: ru.yandex.money.common:yamoney-xml-utils:4.0.1")))

        assertThat(result.output,
                containsString("Forbidden dependency: ru.yandex.money.common:yamoney-json-utils:4.0.3"))
        assertThat(result.output,
                not(containsString("Forbidden dependency: ru.yandex.money.common:yamoney-json-utils:4.2.3")))

        assertThat(result.output,
                containsString("Forbidden dependency: ru.yandex.money.common:yamoney-enum-utils:2.0.2"))
        assertThat(result.output,
                not(containsString("Forbidden dependency: ru.yandex.money.common:yamoney-enum-utils:2.1.5")))
    }

    @Test
    fun `should found forbidden dependencies more version`() {

        buildFile.writeText(setupBuildFile + """
                dependencies {
                    compile 'ru.yandex.money.common:yamoney-enum-utils:2.0.2',
                    'ru.yandex.money.common:yamoney-enum-utils:2.1.5',
                    'ru.yandex.money.common:yamoney-xml-utils:3.0.1',
                    'ru.yandex.money.common:yamoney-xml-utils:4.0.1',
                    'ru.yandex.money.common:yamoney-json-utils:4.0.3',
                    'ru.yandex.money.common:yamoney-json-utils:4.2.3'
                }
                forbiddenDependenciesChecker {
                    after {
                        forbidden 'ru.yandex.money.common:yamoney-xml-utils:4.0.0'
                        recommended '4.0.8'
                        comment 'bla bla'
                    }
                    after {
                        forbidden 'ru.yandex.money.common:yamoney-json-utils:4.2.0'
                        recommended '4.2.8'
                        comment 'bla bla'
                    }
                    after {
                        forbidden 'ru.yandex.money.common:yamoney-enum-utils:2.1.4'
                        recommended '2.1.9'
                        comment 'bla bla'
                    }
                }
            """.trimIndent())

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("checkForbiddenDependencies")
                .withPluginClasspath()
                .withDebug(true)
                .buildAndFail()

        assertThat(result.output, containsString("There is forbidden dependencies"))

        assertThat(result.output,
                not(containsString("Forbidden dependency: ru.yandex.money.common:yamoney-xml-utils:3.0.1")))
        assertThat(result.output,
                containsString("Forbidden dependency: ru.yandex.money.common:yamoney-xml-utils:4.0.1"))

        assertThat(result.output,
                not(containsString("Forbidden dependency: ru.yandex.money.common:yamoney-json-utils:4.0.3")))
        assertThat(result.output,
                containsString("Forbidden dependency: ru.yandex.money.common:yamoney-json-utils:4.2.3"))

        assertThat(result.output,
                not(containsString("Forbidden dependency: ru.yandex.money.common:yamoney-enum-utils:2.0.2")))
        assertThat(result.output,
                containsString("Forbidden dependency: ru.yandex.money.common:yamoney-enum-utils:2.1.5"))
    }

    @Test
    fun `should found forbidden dependencies before and after version`() {

        buildFile.writeText(setupBuildFile + """
                dependencies {
                    compile 'ru.yandex.money.common:yamoney-enum-utils:2.0.2',
                    'ru.yandex.money.common:yamoney-enum-utils:2.1.4',
                    'ru.yandex.money.common:yamoney-xml-utils:3.0.1',
                    'ru.yandex.money.common:yamoney-xml-utils:4.0.1',
                    'ru.yandex.money.common:yamoney-json-utils:4.0.3',
                    'ru.yandex.money.common:yamoney-json-utils:4.2.3'
                }
                forbiddenDependenciesChecker {
                    after {
                        forbidden 'ru.yandex.money.common:yamoney-xml-utils:4.0.0'
                        recommended '4.0.7'
                        comment 'bla bla'
                    }
                    before {
                        forbidden 'ru.yandex.money.common:yamoney-json-utils:4.2.0'
                        recommended '4.2.7'
                        comment 'bla bla'
                    }
                    eq {
                        forbidden 'ru.yandex.money.common:yamoney-enum-utils:2.1.4'
                        recommended '2.1.7'
                        comment 'bla bla'
                    }
                }
            """.trimIndent())

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("checkForbiddenDependencies")
                .withPluginClasspath()
                .withDebug(true)
                .buildAndFail()

        assertThat(result.output, containsString("There is forbidden dependencies"))

        assertThat(result.output,
                not(containsString("Forbidden dependency: ru.yandex.money.common:yamoney-xml-utils:3.0.1")))
        assertThat(result.output,
                containsString("Forbidden dependency: ru.yandex.money.common:yamoney-xml-utils:4.0.1"))

        assertThat(result.output,
                containsString("Forbidden dependency: ru.yandex.money.common:yamoney-json-utils:4.0.3"))
        assertThat(result.output,
                not(containsString("Forbidden dependency: ru.yandex.money.common:yamoney-json-utils:4.2.3")))

        assertThat(result.output,
                not(containsString("Forbidden dependency: ru.yandex.money.common:yamoney-enum-utils:2.0.2")))
        assertThat(result.output,
                containsString("Forbidden dependency: ru.yandex.money.common:yamoney-enum-utils:2.1.4"))
    }
}