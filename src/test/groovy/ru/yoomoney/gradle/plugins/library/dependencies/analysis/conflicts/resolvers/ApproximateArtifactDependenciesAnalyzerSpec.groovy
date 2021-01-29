package ru.yoomoney.gradle.plugins.library.dependencies.analysis.conflicts.resolvers

import ru.yoomoney.gradle.plugins.library.TestRepositories
import ru.yoomoney.gradle.plugins.library.dependencies.repositories.Repository
import ru.yoomoney.gradle.plugins.library.dependencies.repositories.aether.AetherRepository
import ru.yoomoney.gradle.plugins.library.dependencies.utils.TestArtifact
import ru.yoomoney.gradle.plugins.library.dependencies.analysis.DependencyPathBuilder
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.ArtifactName
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.DependencyPath
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.LibraryName
import spock.lang.Specification

/**
 * @author Konstantin Novokreshchenov
 * @since 26.03.2017
 */
class ApproximateArtifactDependenciesAnalyzerSpec extends Specification {
    /**
     * repository:
     *    beta:1.1.0 --> alpha:1.1.0
     *    beta:1.2.0 --> alpha:1.2.0
     *
     * hint:
     *    beta:1.1.0 --> alpha:1.1.0
     *
     * check:
     *    beta:1.2.0  ?  alpha with version != 1.2.0
     *
     */
    def 'test-1'() {
        given: 'real hint'
        def dependencyPathHint = createDependencyPath(['test:beta:1.1.0', 'test:alpha:1.1.0'])

        when: 'check for another versions of dependency'
        def result = hasAnotherDependencyVersion(dependencyPathHint, 'test:beta:1.2.0', 'test:alpha', '1.2.0')

        then: 'does not have dependency with another version'
        !result
    }

    /**
     * repository:
     *    beta:2.1.0 --> alpha:2.1.0
     *    beta:2.2.0 --> alpha:2.2.0
     *
     * hint:
     *    beta:2.1.0 --> alpha:2.1.0
     *
     * check:
     *    beta:2.2.0  ?  alpha with version != 2.3.0
     *
     */
    def 'test-2'() {
        given: 'real hint'
        def dependencyPathHint = createDependencyPath(['test:beta:2.1.0', 'test:alpha:2.1.0'])

        when: 'check for another versions of dependency'
        def result = hasAnotherDependencyVersion(dependencyPathHint, 'test:beta:2.2.0', 'test:alpha', '2.3.0')

        then: 'has dependency with another version'
        result
    }

    /**
     * repository:
     *    omega:3.1.0 --> beta:3.1.0 --> alpha:3.1.0
     *    omega:3.2.0 --> beta:3.2.0 --> alpha:3.2.0
     *
     * hint:
     *    omega:3.1.0 --> beta:3.1.0 --> alpha:3.1.0
     *
     * check:
     *    omega:3.2.0  ?  alpha with version != 3.2.0
     *
     */
    def 'test-3'() {
        given: 'real hint'
        def dependencyPathHint = createDependencyPath(['test:omega:3.1.0', 'test:beta:3.1.0', 'test:alpha:3.1.0'])

        when: 'check for another versions of dependency'
        def result = hasAnotherDependencyVersion(dependencyPathHint, 'test:omega:3.2.0', 'test:alpha', '3.2.0')

        then: 'does not have dependency with another version'
        !result
    }

    /**
     * repository:
     *    omega:4.1.0 --> beta:4.1.0 --> alpha:4.1.0
     *    omega:4.2.0 --> beta:4.2.0 --> alpha:4.2.0
     *
     * hint:
     *    omega:4.1.0 --> beta:4.1.0 --> alpha:4.1.0
     *
     * check:
     *    omega:4.2.0  ?  alpha with version != 4.3.0
     *
     */
    def 'test-4'() {
        given: 'real hint'
        def dependencyPathHint = createDependencyPath(['test:omega:4.1.0', 'test:beta:4.1.0', 'test:alpha:4.1.0'])

        when: 'check for another versions of dependency'
        def result = hasAnotherDependencyVersion(dependencyPathHint, 'test:omega:4.2.0', 'test:alpha', '4.3.0')

        then: 'has dependency with another version'
        result
    }

    /**
     * repository:
     *    beta:5.1.0 --> alpha:5.1.0
     *    beta:5.2.0 --> zeta:5.2.0
     *
     * hint:
     *    beta:5.1.0 --> alpha:5.1.0
     *
     * check:
     *    beta:5.2.0  ?  alpha with version != 5.2.0
     *
     */
    def 'test-5'() {
        given: 'real hint'
        def dependencyPathHint = createDependencyPath(['test:beta:5.1.0', 'test:alpha:5.1.0'])

        when: 'check for another versions of dependency'
        def result = hasAnotherDependencyVersion(dependencyPathHint, 'test:beta:5.2.0', 'test:alpha', '5.2.0')

        then: 'does not have dependency with another version'
        !result
    }

    /**
     * repository:
     *    omega:6.1.0 --> beta:6.1.0 --> alpha:6.1.0
     *    omega:6.2.0 --> beta:6.2.0 --> zeta:6.2.0
     *
     * hint:
     *    omega:6.1.0 --> beta:6.1.0 --> alpha:6.1.0
     *
     * check:
     *    omega:6.2.0  ?  alpha with version != 6.2.0
     *
     */
    def 'test-6'() {
        given: 'real hint'
        def dependencyPathHint = createDependencyPath(['test:omega:6.1.0', 'test:beta:6.1.0', 'test:alpha:6.1.0'])

        when: 'check for another versions of dependency'
        def result = hasAnotherDependencyVersion(dependencyPathHint, 'test:omega:6.2.0', 'test:alpha', '6.2.0')

        then: 'does not have dependency with another version'
        !result
    }

    /**
     * repository:
     *    omega:7.1.0 --> beta:7.1.0 --> alpha:7.1.0
     *    omega:7.2.0 --> zeta:7.2.0 --> alpha:6.2.0
     *
     * hint:
     *    omega:7.1.0 --> beta:7.1.0 --> alpha:7.1.0
     *
     * check:
     *    omega:7.2.0  ?  alpha with version != 7.2.0
     *
     */
    def 'test-7'() {
        given: 'real hint'
        def dependencyPathHint = createDependencyPath(['test:omega:7.1.0', 'test:beta:7.1.0', 'test:alpha:7.1.0'])

        when: 'check for another versions of dependency'
        def result = hasAnotherDependencyVersion(dependencyPathHint, 'test:omega:7.2.0', 'test:alpha', '7.2.0')

        then: 'does not have dependency with another version'
        !result
    }

    /**
     * repository:
     *    omega:8.1.0 --> beta:8.1.0 --> alpha:8.1.0
     *    omega:8.2.0 --> zeta:8.2.0 --> alpha:8.3.0
     *
     * hint:
     *    omega:8.1.0 --> beta:8.1.0 --> alpha:8.1.0
     *
     * check:
     *    omega:8.2.0  ?  alpha with version != 8.2.0
     *
     */
    def 'test-8'() {
        given: 'real hint'
        def dependencyPathHint = createDependencyPath(['test:omega:8.1.0', 'test:beta:8.1.0', 'test:alpha:8.1.0'])

        when: 'check for another versions of dependency'
        def result = hasAnotherDependencyVersion(dependencyPathHint, 'test:omega:8.2.0', 'test:alpha', '8.3.0')

        then: 'does not have dependency with another version'
        !result
    }

    /**
     * repository:
     *    omega:8.1.0 --> beta:8.1.0 --> alpha:8.1.0
     *    omega:8.2.0 --> zeta:8.2.0 --> alpha:8.3.0
     *
     * hint:
     *    beta:8.1.0 --> alpha:8.1.0
     *
     * check:
     *    omega:8.2.0  ?  alpha with version != 8.2.0
     *
     */
    def 'test-9'() {
        given: 'bad hint'
        def dependencyPathHint = createDependencyPath(['test:beta:8.1.0', 'test:alpha:8.1.0'])

        when: 'check for another versions of dependency'
        def result = hasAnotherDependencyVersion(dependencyPathHint, 'test:omega:8.2.0', 'test:alpha', '8.3.0')

        then: 'does not have dependency with another version'
        !result
    }


    def createDependencyPath(List<String> artifactNames) {
        DependencyPathBuilder<TestArtifact> pathBuilder = DependencyPathBuilder.create();
        artifactNames.each { pathBuilder.add(new TestArtifact(it)) }
        return pathBuilder.build()
    }

    def hasAnotherDependencyVersion(DependencyPath dependencyPathHint,
                                    String artifact, String dependencyLibrary, String dependencyVersion) {
        Repository repository = AetherRepository.create([TestRepositories.MAVEN_REPO_1])
        def analyzer = new ApproximateArtifactDependenciesAnalyzer<>(repository, dependencyPathHint)
        return analyzer.hasAnotherDependencyVersions(ArtifactName.parse(artifact), LibraryName.parse(dependencyLibrary), dependencyVersion)
    }
}
