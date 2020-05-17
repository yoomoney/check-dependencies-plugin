package ru.yandex.money.gradle.plugins.library.snapshot

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
 * Тесты CheckSnapshotsDependenciesTask
 *
 * @author horyukova
 * @since 01.03.2019
 */

class CheckSnapshotSpec {

    private val projectDir = TemporaryFolder()
    var originRepoFolder = TemporaryFolder()

    private lateinit var setupBuildFile: String
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
    fun `should found SNAPSHOT dependencies`() {

        buildFile.writeText(setupBuildFile + """
                dependencies {
                    compile 'ru.yandex.money.common:yamoney-json-utils:2.0.2-feature-SNAPSHOT',
                    'ru.yandex.money.common:yamoney-xml-utils:3.0.1'

                }
            """.trimIndent())

        val result = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments("checkSnapshotsDependencies")
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
                        'ru.yandex.money.common:yamoney-xml-utils:3.0.1'
                }
            """.trimIndent())

        val result = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments("checkSnapshotsDependencies")
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
                            'ru.yandex.money.common:yamoney-xml-utils:3.0.1'
                }
            """.trimIndent())

        val result = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments("build")
                .withPluginClasspath()
                .withDebug(true)
                .build()

        assertThat(result.output, not(containsString("You have the following SNAPSHOT dependencies")))
    }

    @Test
    fun `should checkSnapshotsDependencies successfully when there is 'allowSnapshot' flag`() {

        buildFile.writeText(setupBuildFile + """
                ext.allowSnapshot = "true"

                dependencies {
                    compile 'ru.yandex.money.common:yamoney-json-utils:2.0.2-feature-SNAPSHOT',
                            'ru.yandex.money.common:yamoney-xml-utils:3.0.1'
                }
            """.trimIndent())

        val result = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments("checkSnapshotsDependencies")
                .withPluginClasspath()
                .withDebug(true)
                .build()

        assertThat("checkSnapshotsDependencies failed when there is 'allowSnapshot' flag",
                result.output, containsString("Snapshot dependencies are allowed. SKIPPED"))
    }
}