package ru.yandex.money.gradle.plugins.library

import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import ru.yandex.money.gradle.plugins.library.helpers.VersionProperties

/**
 *
 * @author Kirill Bulatov (mail4score@gmail.com)
 * @since 22.12.2016
 */
abstract class AbstractGradleTest {
    private File temporaryFolder
    private File buildFile

    protected static final String COMMON_BUILD_FILE_CONTENTS = """
    buildscript {
                repositories {
                    flatDir { dirs '${new File("./build/libs").absolutePath.replace("\\", "\\\\")}' }
                    maven { url 'http://nexus.yamoney.ru/content/repositories/releases/' }
                    maven { url 'http://nexus.yamoney.ru/content/repositories/jcenter.bintray.com/' }
                }
                dependencies {
                    classpath 'ru.yandex.money.gradle.plugins:yamoney-library-project-plugin:${
                        VersionProperties.loadPluginVersion(new File("./gradle.properties"))}'
                    classpath 'org.ajoberstar:gradle-git:1.5.0'
                    classpath 'ru.yandex.money.common:yamoney-doc-publishing:1.0.1'
                }
            }
            apply plugin: 'yamoney-library-project-plugin'
            \n
    """.stripIndent()

    @BeforeMethod
    void beforeMethod() {
        temporaryFolder = File.createTempFile("yamoney_gradle_libraries_plugin_test_", "")
        recursiveDelete(temporaryFolder)
        temporaryFolder.mkdir()
        buildFile = createFileInTemporaryFolder("build.gradle")
    }

    @AfterMethod
    void afterMethod() {
        recursiveDelete(temporaryFolder)
    }

    private void recursiveDelete(File file) {
        File[] files = file.listFiles()
        if (files != null) {
            for (File each : files) {
                recursiveDelete(each)
            }
        }
        file.delete()
    }

    protected File createFileInTemporaryFolder(String fileName) {
        if (temporaryFolder == null) {
            throw new IllegalStateException("temporary directory has not been yet created")
        }

        def file = new File(temporaryFolder, fileName)
        if (!file.createNewFile()) {
            throw new IllegalStateException("File with name $fileName already exists")
        }
        return file
    }

    protected File getTemporaryFolder() {
        if (temporaryFolder == null) {
            throw new IllegalStateException("temporary directory has not been yet created")
        }
        return temporaryFolder
    }

    protected File getBuildFile() {
        if (buildFile == null) {
            throw new IllegalStateException("build file has not been yet created")
        }
        return buildFile
    }
}
