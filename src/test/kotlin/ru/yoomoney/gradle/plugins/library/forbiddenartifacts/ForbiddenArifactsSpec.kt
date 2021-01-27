package ru.yoomoney.gradle.plugins.library.forbiddenartifacts

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
                       jcenter()
                       mavenCentral()
                    }
                    dependencies {
                        classpath 'io.spring.gradle:dependency-management-plugin:1.0.1.RELEASE'
                    }
                }
                plugins {
                    id 'java'
                    id 'io.spring.dependency-management'
                    id 'ru.yoomoney.gradle.plugins.check-dependencies-plugin'
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
                    compile 'joda-time:joda-time:2.10.9'

                }
                forbiddenDependenciesChecker {
                    eq {
                        forbidden 'joda-time:joda-time:2.10.9'
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
                containsString("Forbidden dependency: joda-time:joda-time:2.10.9"))
        assertThat(result.output,
                containsString("Recommended version: joda-time:joda-time 2.10.9 -> "))
    }

    @Test
    fun `should found forbidden dependencies`() {

        buildFile.writeText(setupBuildFile + """
                dependencies {
                    compile 'joda-time:joda-time:2.10.9',
                           'org.apache.tomcat.embed:tomcat-embed-core:9.0.10'

                }
                forbiddenDependenciesChecker {
                    eq {
                        forbidden 'joda-time:joda-time:2.10.9'
                        recommended '4.0.+'
                        comment 'bla bla'
                    }
                    eq {
                        forbidden 'org.apache.tomcat.embed:tomcat-embed-core:9.0.10'
                        recommended '10.0.0'
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
                containsString("Forbidden dependency: joda-time:joda-time:2.10.9"))
        assertThat(result.output,
                containsString("Recommended version: joda-time:joda-time 2.10.9 -> "))

        assertThat(result.output,
                containsString("Forbidden dependency: org.apache.tomcat.embed:tomcat-embed-core:9.0.10"))
        assertThat(result.output,
                containsString("Recommended version: org.apache.tomcat.embed:tomcat-embed-core 9.0.10 -> "))
    }

    @Test
    fun `should found forbidden dependencies when checkLibraryDependencies`() {

        buildFile.writeText(setupBuildFile + """
                dependencies {
                    compile 'joda-time:joda-time:2.10.9',
                           'org.apache.tomcat.embed:tomcat-embed-core:9.0.10'

                }
                forbiddenDependenciesChecker {
                    eq {
                        forbidden 'joda-time:joda-time:2.10.9'
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
                containsString("Forbidden dependency: joda-time:joda-time:2.10.9"))
        assertThat(result.output,
                containsString("Recommended version: joda-time:joda-time 2.10.9 -> "))
    }

    @Test
    fun `should not found forbidden dependencies`() {

        buildFile.writeText(setupBuildFile + """
                dependencies {
                    compile 'joda-time:joda-time:2.10.8',
                           'org.apache.tomcat.embed:tomcat-embed-core:9.0.10'

                }
                forbiddenDependenciesChecker {
                    eq {
                        forbidden 'joda-time:joda-time:2.10.9'
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
                    compile 'joda-time:joda-time:2.10.9'

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
                    compile 'joda-time:joda-time:2.10.9'
                }
                forbiddenDependenciesChecker {
                    before {
                        forbidden 'joda-time:joda-time:2.11.0'
                        recommended '4.0.1'
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
                containsString("Forbidden dependency: joda-time:joda-time:2.10.9"))
        assertThat(result.output,
                not(containsString("Forbidden dependency: joda-time:joda-time:2.11.0")))
    }

    @Test
    fun `should found forbidden dependencies more version`() {

        buildFile.writeText(setupBuildFile + """
                dependencies {
                    compile 'joda-time:joda-time:1.6.2',
                        'joda-time:joda-time:2.10.9'
                }
                forbiddenDependenciesChecker {
                    after {
                        forbidden 'joda-time:joda-time:2.0.0'
                        recommended '4.0.8'
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

        assertThat(
            result.output,
            containsString("Forbidden dependency: joda-time:joda-time:2.10.9")
        )

        assertThat(
            result.output,
            not(containsString("Forbidden dependency: joda-time:joda-time:1.6.2"))
        )
    }

    @Test
    fun `should found forbidden dependencies before and after version`() {

        buildFile.writeText(setupBuildFile + """
                dependencies {
                    compile 'joda-time:joda-time:1.6.2',
                        'joda-time:joda-time:2.10.9',
                        'org.apache.tomcat.embed:tomcat-embed-core:10.0.0',
                        'org.apache.tomcat.embed:tomcat-embed-core:9.0.10'
                }
                forbiddenDependenciesChecker {
                    after {
                        forbidden 'joda-time:joda-time:2.0.0'
                        recommended '4.0.7'
                        comment 'bla bla'
                    }
                    before {
                        forbidden 'org.apache.tomcat.embed:tomcat-embed-core:9.1.0'
                        recommended '4.2.7'
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

        assertThat(
            result.output,
            containsString("Forbidden dependency: joda-time:joda-time:2.10.9")
        )

        assertThat(
            result.output,
            not(containsString("Forbidden dependency: joda-time:joda-time:1.6.2"))
        )

        assertThat(
            result.output,
            containsString("Forbidden dependency: org.apache.tomcat.embed:tomcat-embed-core:9.0.10")
        )

        assertThat(
            result.output,
            not(containsString("Forbidden dependency: org.apache.tomcat.embed:tomcat-embed-core:10.0.0"))
        )
    }

    @Test
    fun `should found forbidden dependencies range versions`() {

        buildFile.writeText(setupBuildFile + """
                dependencies {
                    compile 'joda-time:joda-time:1.6.2',
                        'joda-time:joda-time:2.10.9'
                }
                forbiddenDependenciesChecker {
                    range {
                        forbidden 'joda-time:joda-time'
                        startVersion '1.7.0'
                        endVersion '2.11.0'
                        recommended '5.1.+'
                        comment 'В версии есть ошибка конфигурирования'
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

        assertThat(
            result.output,
            containsString("Forbidden dependency: joda-time:joda-time:2.10.9")
        )

        assertThat(
            result.output,
            not(containsString("Forbidden dependency: joda-time:joda-time:1.6.2"))
        )
    }
}