package ru.yandex.money.gradle.plugins.library.changelog

import ru.yandex.money.gradle.plugins.library.AbstractPluginSpec

/**
 * Функциональные тесты для CheckChangelogPlugin, проверяющего корректность changelog файла
 * при его наличии.
 *
 * @author Konstantin Rashev (rashev@yamoney.ru)
 * @since 17.01.2017
 */
class CheckChangelogPluginSpec extends AbstractPluginSpec {

    def setup() {
        file("gradle.properties") << "version=1.0.1-SNAPSHOT"
    }

    def 'skip checkChangelog task on master branch'() {
        when:
        def result = runTasksSuccessfully("checkChangelog")

        then:
        grgit.branch.current.name == "master"
        result.wasSkipped(":checkChangelog")
    }

    def 'skip checkChangelog task on dev branch'() {
        grgit.checkout(branch: 'dev', createBranch: true)

        when:
        def result = runTasksSuccessfully("checkChangelog")

        then:
        grgit.branch.current.name == "dev"
        result.wasSkipped(":checkChangelog")
    }

    def 'skip checkChangelog task on release branch'() {
        grgit.checkout(branch: 'release/2.0', createBranch: true)

        when:
        def result = runTasksSuccessfully("checkChangelog")

        then:
        grgit.branch.current.name == "release/2.0"
        result.wasSkipped(":checkChangelog")
    }

    def 'run and pass checkChangelog task on feature branch'() {
        grgit.checkout(branch: 'feature/my-example-feature', createBranch: true)

        when:
        def result = runTasksSuccessfully("checkChangelog")

        then:
        grgit.branch.current.name == "feature/my-example-feature"
        !result.wasSkipped(":checkChangelog")
        result.standardOutput.contains("Changelog file doesn't exist")
    }

    def 'skip checkChangelog task before compileJava task'() {
        when:
        def result = runTasksSuccessfully("compileJava")

        then:
        grgit.branch.current.name == "master"
        result.wasSkipped(":checkChangelog")
        result.wasExecuted("compileJava")
    }

    def 'pass checkChangelog task on correct CHANGELOG.md on feature branch'() {
        file('CHANGELOG.md') << '''
            # sample-library

            ## NEXT_VERSION

            ## [1.0.1]() (22-12-2016)

            Описание деталей релиза.
            
            ## [1.0.0]() (21-12-2016)

            Описание деталей предыдущего релиза.
        '''.stripIndent()
        grgit.checkout(branch: 'feature/my-example-feature', createBranch: true)

        when:
        def result = runTasksSuccessfully("checkChangelog")

        then:
        grgit.branch.current.name == "feature/my-example-feature"
        !result.wasSkipped(":checkChangelog")
        !result.standardOutput.contains("Changelog file doesn't exist")
    }

    def 'fail checkChangelog task on incorrect CHANGELOG.md on feature branch'() {
        file('CHANGELOG.md') << '''
            # sample-library

            ## NEXT_VERSION

            ## [1.0.0]() (21-12-2016)

            Описание деталей предыдущего релиза.
        '''.stripIndent()
        grgit.checkout(branch: 'feature/my-example-feature', createBranch: true)

        when:
        def result = runTasksWithFailure("checkChangelog")

        then:
        grgit.branch.current.name == "feature/my-example-feature"
        !result.wasSkipped(":checkChangelog")
        result.standardError.contains("Changelog entry not found.")
    }
}
