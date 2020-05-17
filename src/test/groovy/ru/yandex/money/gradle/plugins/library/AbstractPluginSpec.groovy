package ru.yandex.money.gradle.plugins.library

import nebula.test.IntegrationSpec
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.transport.URIish

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 *
 * @author Kirill Bulatov (mail4score@gmail.com)
 * @since 22.12.2016
 */
abstract class AbstractPluginSpec extends IntegrationSpec {

    protected static final String COMMON_BUILD_FILE_CONTENTS = """
    buildscript {
        repositories {
                    maven { url 'http://nexus.yamoney.ru/repository/thirdparty/' }
                    maven { url 'http://nexus.yamoney.ru/repository/central/' }
                    maven { url 'http://nexus.yamoney.ru/repository/releases/' }
                    maven { url 'http://nexus.yamoney.ru/repository/snapshots/' }
            }
        dependencies {
            classpath 'io.spring.gradle:dependency-management-plugin:1.0.1.RELEASE'
        }
    }
    repositories {
                    maven { url 'http://nexus.yamoney.ru/repository/thirdparty/' }
                    maven { url 'http://nexus.yamoney.ru/repository/central/' }
                    maven { url 'http://nexus.yamoney.ru/repository/releases/' }
                    maven { url 'http://nexus.yamoney.ru/repository/snapshots/' }
            }
    apply plugin: 'java'
    apply plugin: 'io.spring.dependency-management'
    apply plugin: 'yamoney-check-dependencies-plugin'
    System.setProperty("ignoreDeprecations", "true")
    """.stripIndent()

    private Git git

    def setup() {
        buildFile << COMMON_BUILD_FILE_CONTENTS
        initProjectRepoWithMasterBranch();
    }

    private initProjectRepoWithMasterBranch() throws GitAPIException, IOException, URISyntaxException {
        File projectDir = projectDir;
        git = Git.init().setDirectory(projectDir).call();

        git.add().addFilepattern("build.gradle")
                .call();

        git.commit().setMessage("build.gradle commit").call();

        Path originRepoFolder = Files.createTempDirectory("origin");
        Git.init().setDirectory(originRepoFolder.toFile())
                .setBare(true)
                .call();

        git.remoteAdd().setUri(new URIish("file://" + originRepoFolder.toAbsolutePath() + "/"))
                .setName("origin")
                .call();
        git.push()
                .setPushAll()
                .setRemote("origin")
                .setPushTags()
                .call();
        return git;
    }

}
