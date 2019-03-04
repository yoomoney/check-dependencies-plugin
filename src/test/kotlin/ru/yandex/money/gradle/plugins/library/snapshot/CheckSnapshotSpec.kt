package ru.yandex.money.gradle.plugins.library.snapshot

import org.gradle.internal.impldep.org.hamcrest.CoreMatchers.containsString
import org.gradle.internal.impldep.org.hamcrest.CoreMatchers.not
import org.gradle.internal.impldep.org.hamcrest.MatcherAssert.assertThat
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Тесты CheckSnapshotDependenciesTask
 *
 * @author horyukova
 * @since 01.03.2019
 */

class CheckSnapshotSpec {

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
    fun `should found SNAPSHOT dependencies`() {

        buildFile.writeText(setupBuildFile + """
                dependencies {
                    compile 'ru.yandex.money.common:yamoney-json-utils:2.0.2-feature-SNAPSHOT',
                    'ru.yandex.money.common:yamoney-xml-utils:3.0.1',
                    'ru.yandex.money.common:yamoney-xml-utils:4.0.1',
                    'ru.yandex.money.common:yamoney-json-utils:4.0.3'

                }
            """.trimIndent())

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("checkSnapshotDependencies")
                .withPluginClasspath()
                .withDebug(true)
                .buildAndFail()

        assertThat(result.output, containsString("You have the following SNAPSHOT dependencies:"))
        assertThat(result.output, containsString("[ru.yandex.money.common:yamoney-json-utils:2.0.2-feature-SNAPSHOT]"))
    }

    @Test
    fun `should not found SNAPSHOT dependencies`() {

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
                .withArguments("checkSnapshotDependencies")
                .withPluginClasspath()
                .withDebug(true)
                .build()

        assertThat(result.output, not(containsString("You have the following SNAPSHOT dependencies:")))
        assertThat(result.output,
                not(containsString("[ru.yandex.money.common:yamoney-json-utils:2.0.2-feature-SNAPSHOT]")))
    }

    @Test
    fun `should build successfully when there is SNAPSHOT dependency`() {

        buildFile.writeText(setupBuildFile + """
                dependencies {
                    compile 'ru.yandex.money.common:yamoney-json-utils:2.0.2-feature-SNAPSHOT',
                            'ru.yandex.money.common:yamoney-xml-utils:3.0.1',
                            'ru.yandex.money.common:yamoney-xml-utils:4.0.1',
                            'ru.yandex.money.common:yamoney-json-utils:4.0.3'
                }
            """.trimIndent())

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("build")
                .withPluginClasspath()
                .withDebug(true)
                .build()

        assertThat(result.output, not(containsString("You have the following SNAPSHOT dependencies")))
    }

    @Test
    fun `should checkSnapshotDependencies successfully when there is 'allowSnapshot' flag`() {

        buildFile.writeText(setupBuildFile + """
                ext.allowSnapshot = "true"

                dependencies {
                    compile 'ru.yandex.money.common:yamoney-json-utils:2.0.2-feature-SNAPSHOT',
                            'ru.yandex.money.common:yamoney-xml-utils:3.0.1',
                            'ru.yandex.money.common:yamoney-xml-utils:4.0.1',
                            'ru.yandex.money.common:yamoney-json-utils:4.0.3'
                }
            """.trimIndent())

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("checkSnapshotDependencies")
                .withPluginClasspath()
                .withDebug(true)
                .build()

        assertThat("checkSnapshotDependencies failed when there is 'allowSnapshot' flag",
                result.output, containsString("Snapshot dependencies are allowed. SKIPPED"))
    }
}