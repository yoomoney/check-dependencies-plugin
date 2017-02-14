package ru.yandex.money.gradle.plugins.library.readme

import org.joda.time.DateTime
import ru.yandex.money.gradle.plugins.library.AbstractPluginSpec

/**
 *
 * @author Kirill Bulatov (mail4score@gmail.com)
 * @since 22.12.2016
 */
class ReadmePluginSpec extends AbstractPluginSpec {
    private static final String NON_EXISTENT_FILE_PATH = "non_existent_path_${DateTime.now().millis}"

    def setup() {
        buildFile << """
        readme {
            pathToDocument = '$NON_EXISTENT_FILE_PATH'
        }
        """.stripIndent()
    }

    def "fail publishReadme task on master branch cause non existent file"() {
        when:
        def result = runTasksWithFailure("publishReadme")

        then:
        grgit.branch.current.name == "master"
        result.wasExecuted(":publishReadme")
        result.standardError.contains("java.nio.file.NoSuchFileException: $NON_EXISTENT_FILE_PATH")
    }

    def "skip publishReadme task on non-master branch"() {
        grgit.checkout(branch: 'dev', createBranch: true)

        when:
        def result = runTasksSuccessfully("publishReadme")

        then:
        grgit.branch.current.name == "dev"
        result.wasSkipped(":publishReadme")
    }
}
