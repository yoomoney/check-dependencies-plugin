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
                    forbiddenArtifacts = ['ru.yandex.money.common:yamoney-xml-utils:4.0.1']
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
                    forbiddenArtifacts = ['ru.yandex.money.common:yamoney-json-utils:4.0.3',
                    'ru.yandex.money.common:yamoney-xml-utils:4.0.1']

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
    fun `should not found forbidden dependencies`() {

        buildFile.writeText(setupBuildFile + """
                dependencies {
                    compile 'ru.yandex.money.common:yamoney-json-utils:2.0.2',
                    'ru.yandex.money.common:yamoney-xml-utils:3.0.1',
                    'ru.yandex.money.common:yamoney-xml-utils:4.0.1',
                    'ru.yandex.money.common:yamoney-json-utils:4.0.3'

                }
                forbiddenDependenciesChecker {
                    forbiddenArtifacts = ['ru.yandex.money.common:yamoney-enum-utils:4.0.2']
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
                    forbiddenArtifacts = ['ru.yandex.money.common:yamoney-xml-utils:<4.0.0',
                    'ru.yandex.money.common:yamoney-json-utils:<4.2.0',
                    'ru.yandex.money.common:yamoney-enum-utils:<2.1.4']
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
                    forbiddenArtifacts = ['ru.yandex.money.common:yamoney-xml-utils:>4.0.0',
                    'ru.yandex.money.common:yamoney-json-utils:>4.2.0',
                    'ru.yandex.money.common:yamoney-enum-utils:>2.1.4']
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
}