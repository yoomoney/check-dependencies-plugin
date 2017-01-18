package ru.yandex.money.gradle.plugins.library.changelog

import nebula.test.IntegrationSpec
import org.ajoberstar.grgit.Grgit

/**
 * Функциональные тесты для CheckChangelogPlugin, проверяющего корректность changelog файла
 * при его наличии.
 *
 * @author Konstantin Rashev (rashev@yamoney.ru)
 * @since 17.01.2017
 */
class CheckChangelogPluginTest extends IntegrationSpec {

    private Grgit grgit;

    def setup() {
        grgit = Grgit.init(dir: projectDir.absolutePath)

        buildFile << '''
            buildscript {
                repositories {
                    maven { url 'http://nexus.yamoney.ru/content/repositories/releases/' }
                    maven { url 'http://nexus.yamoney.ru/content/repositories/jcenter.bintray.com/' }
                }
                dependencies {
                    classpath 'org.ajoberstar:gradle-git:1.5.0'
                    classpath 'ru.yandex.money.common:yamoney-doc-publishing:1.0.1'
                }
            }
            apply plugin: 'java'
            apply plugin: 'yamoney-library-project-plugin'
        '''.stripIndent()

        grgit.add(patterns: ['build.gradle'])
        grgit.commit(message: 'build.gradle commit', all: true)

        file("gradle.properties") << "version=1.0.1-SNAPSHOT"

    }

    def cleanup() {
        grgit.close()
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
