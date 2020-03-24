package ru.yandex.money.gradle.plugins.library

import nebula.test.IntegrationSpec

/**
 *
 * @author Kirill Bulatov (mail4score@gmail.com)
 * @since 22.12.2016
 */
abstract class AbstractPluginSpec extends IntegrationSpec {

    protected static final String COMMON_BUILD_FILE_CONTENTS = """
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
    apply plugin: 'java'
    apply plugin: 'io.spring.dependency-management'
    apply plugin: 'yamoney-check-dependencies-plugin'
    System.setProperty("ignoreDeprecations", "true")
    """.stripIndent()


    def setup() {
        buildFile << COMMON_BUILD_FILE_CONTENTS
    }
}
