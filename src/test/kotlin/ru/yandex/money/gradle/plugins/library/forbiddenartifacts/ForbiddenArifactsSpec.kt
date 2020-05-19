package ru.yandex.money.gradle.plugins.library.forbiddenartifacts

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.URIish
import org.gradle.internal.impldep.org.hamcrest.CoreMatchers.containsString
import org.gradle.internal.impldep.org.hamcrest.CoreMatchers.not
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

    private val projectDir = TemporaryFolder()
    private lateinit var setupBuildFile: String
    var originRepoFolder = TemporaryFolder()

    lateinit var buildFile: File

    lateinit var git: Git
    lateinit var gitOrigin: Git
    lateinit var gradleProperties: File
    @Before
    fun setup() {
        projectDir.create()
        buildFile = projectDir.newFile("build.gradle")

        setupBuildFile = """

                buildscript {
                    repositories {
                        maven { url 'https://nexus.yamoney.ru/content/repositories/thirdparty/' }
                        maven { url 'https://nexus.yamoney.ru/content/repositories/central/' }
                        maven { url 'https://nexus.yamoney.ru/content/repositories/releases/' }
                        maven { url 'https://nexus.yamoney.ru/content/repositories/public/' }
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

        originRepoFolder.create()

        git = Git.init().setDirectory(File(projectDir.root.absolutePath))
                .setBare(false)
                .call()

        gradleProperties = projectDir.newFile("gradle.properties")
        gradleProperties.writeText("version=1.0.1-SNAPSHOT")
        git.add().addFilepattern("gradle.properties")
                .addFilepattern("build.gradle")
                .call()
        git.commit().setMessage("build.gradle commit").call()
        git.tag().setName("1.0.0").call()
        gitOrigin = Git.init().setDirectory(originRepoFolder.root)
                .setBare(true)
                .call()
        val remoteSetUrl = git.remoteSetUrl()
        remoteSetUrl.setRemoteUri(URIish("file://${originRepoFolder.root.absolutePath}/"))
        remoteSetUrl.setRemoteName("origin")
        remoteSetUrl.call()
        git.push()
                .setPushAll()
                .setPushTags()
                .call()
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
                .withProjectDir(projectDir.root)
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
                .withProjectDir(projectDir.root)
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
                .withProjectDir(projectDir.root)
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
                .withProjectDir(projectDir.root)
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
                .withProjectDir(projectDir.root)
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
                .withProjectDir(projectDir.root)
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
                .withProjectDir(projectDir.root)
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
                .withProjectDir(projectDir.root)
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

    @Test
    fun `should found forbidden dependencies range versions`() {

        buildFile.writeText(setupBuildFile + """
                dependencies {
                    compile 'ru.yandex.money.common:yamoney-http-client:3.0.1',
                            'ru.yandex.money.common:yamoney-http-client:5.0.1'
                }
                forbiddenDependenciesChecker {
                    range {
                        forbidden 'ru.yandex.money.common:yamoney-http-client'
                        startVersion '5.0.0'
                        endVersion '5.0.1'
                        recommended '5.1.+'
                        comment 'В версии есть ошибка конфигурирования socketTimeout и connectionTimeout'
                    }
                }
            """.trimIndent())

        val result = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments("checkForbiddenDependencies")
                .withPluginClasspath()
                .withDebug(true)
                .buildAndFail()

        assertThat(result.output, containsString("There is forbidden dependencies"))

        assertThat(result.output,
                not(containsString("Forbidden dependency: ru.yandex.money.common:yamoney-http-client:3.0.1")))
        assertThat(result.output,
                containsString("Forbidden dependency: ru.yandex.money.common:yamoney-http-client:5.0.1"))
    }
}