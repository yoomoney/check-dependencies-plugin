package ru.yandex.money.gradle.plugins.library.snapshot

import ru.yandex.money.gradle.plugins.library.AbstractPluginSpec

/**
 * Тесты CheckSnapshotTask
 *
 * @author horyukova
 * @since 27.02.2019
 */
class CheckSnapshotSpec extends AbstractPluginSpec {

    def "Found SNAPSHOT dependencies"() {

        given:
        buildFile << """
                   
            dependencies {
                compile 'ru.yandex.money.common:yamoney-json-utils:2.0.2-feature-SNAPSHOT',
                        'ru.yandex.money.common:yamoney-xml-utils:3.0.1',
                        'ru.yandex.money.common:yamoney-xml-utils:4.0.1',
                        'ru.yandex.money.common:yamoney-json-utils:4.0.3'
                       
            } 
            
                """.stripIndent()

        when:
        def result = runTasksWithFailure("checkSnapshot")

        then:
        result.standardOutput.contains("You have the following SNAPSHOT dependencies:\n" +
                "[ru.yandex.money.common:yamoney-json-utils:2.0.2-feature-SNAPSHOT]")
    }

    def "Not found SNAPSHOT dependencies"() {

        given:
        buildFile << """
                   
            dependencies {
                compile 'ru.yandex.money.common:yamoney-json-utils:2.0.2',
                        'ru.yandex.money.common:yamoney-xml-utils:3.0.1',
                        'ru.yandex.money.common:yamoney-xml-utils:4.0.1',
                        'ru.yandex.money.common:yamoney-json-utils:4.0.3'
                       
            } 
            
                """.stripIndent()

        when:
        def result = runTasksSuccessfully("checkSnapshot")

        then:
        !(result.standardOutput.contains("You have the following SNAPSHOT dependencies"))
    }

    def "Build successfully when there is SNAPSHOT dependency"() {

        given:
        buildFile << """
                   
            dependencies {
                compile 'ru.yandex.money.common:yamoney-json-utils:2.0.2-feature-SNAPSHOT',
                        'ru.yandex.money.common:yamoney-xml-utils:3.0.1',
                        'ru.yandex.money.common:yamoney-xml-utils:4.0.1',
                        'ru.yandex.money.common:yamoney-json-utils:4.0.3'
                       
            } 
            
                """.stripIndent()

        when:
        def result = runTasksSuccessfully("build")

        then:
        !(result.standardOutput.contains("You have the following SNAPSHOT dependencies"))
    }

    def "checkSnapshot successfully when there is 'forceRelease' flag"() {

        given:
        buildFile << """
                   
            ext.forceRelease = "true"
            
            dependencies {
                compile 'ru.yandex.money.common:yamoney-json-utils:2.0.2-feature-SNAPSHOT',
                        'ru.yandex.money.common:yamoney-xml-utils:3.0.1',
                        'ru.yandex.money.common:yamoney-xml-utils:4.0.1',
                        'ru.yandex.money.common:yamoney-json-utils:4.0.3'
                       
            } 
            
                """.stripIndent()

        when:
        def result = runTasksSuccessfully("checkSnapshot")

        then:
        result.standardOutput.contains("Force release action. SKIPPED")
    }
}
