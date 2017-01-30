package ru.yandex.money.gradle.plugins.library

import nebula.test.IntegrationSpec
import org.ajoberstar.grgit.Grgit

/**
 *
 * @author Kirill Bulatov (mail4score@gmail.com)
 * @since 22.12.2016
 */
abstract class AbstractPluginSpec extends IntegrationSpec {

    protected static final String COMMON_BUILD_FILE_CONTENTS = """
    buildscript {
        repositories {
            maven { url 'http://nexus.yamoney.ru/content/repositories/releases/' }
            maven { url 'http://nexus.yamoney.ru/content/repositories/jcenter.bintray.com/' }
            maven { url 'http://nexus.yamoney.ru/content/repositories/central/' }
        }
        dependencies {
            classpath 'org.ajoberstar:gradle-git:1.5.0'
            classpath 'ru.yandex.money.common:yamoney-doc-publishing:1.0.1'
            classpath 'io.spring.gradle:dependency-management-plugin:0.6.1.RELEASE'
        }
    }
    apply plugin: 'java'
    apply plugin: 'io.spring.dependency-management'
    apply plugin: 'yamoney-library-project-plugin'
    """.stripIndent()

    protected Grgit grgit

    def setup() {
        grgit = Grgit.init(dir: projectDir.absolutePath)

        buildFile << COMMON_BUILD_FILE_CONTENTS

        grgit.add(patterns: ['build.gradle'])
        grgit.commit(message: 'build.gradle commit', all: true)
    }

    def cleanup() {
        grgit.close()
    }
}
