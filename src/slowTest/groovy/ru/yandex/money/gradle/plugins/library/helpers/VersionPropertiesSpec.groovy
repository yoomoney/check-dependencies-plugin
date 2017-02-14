package ru.yandex.money.gradle.plugins.library.helpers

import spock.lang.Specification

/**
 *
 * @author Kirill Bulatov (mail4score@gmail.com)
 * @since 23.12.2016
 */
class VersionPropertiesSpec extends Specification {

    def "open correct version file"() {
        when:
        def versionFile = new File("./gradle.properties")
        def result = VersionProperties.loadPluginVersion(versionFile)

        then:
        result != null
        result.length() > 0
    }

    def "throw IllegalStateException on incorrect version file"() {
        when:
        VersionProperties.loadPluginVersion(new File("./build.gradle"))

        then:
        thrown(IllegalStateException)
    }

    def "throw IllegalStateException on not existent version file"() {
        when:
        VersionProperties.loadPluginVersion(new File("./zzzz"))

        then:
        thrown(IllegalStateException)
    }
}
