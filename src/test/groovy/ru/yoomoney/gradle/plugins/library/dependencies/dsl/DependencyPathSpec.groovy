package ru.yoomoney.gradle.plugins.library.dependencies.dsl

import spock.lang.Specification

/**
 * @author Konstantin Novokreshchenov
 * @since 29.03.2017
 */
class DependencyPathSpec extends Specification {

    def 'fail to create dependency path with empty list of dependencies'() {
        given:
        LinkedList<TestArtifact> dependencies = []

        when:
        new DependencyPath<TestArtifact>(dependencies)

        then:
        thrown IllegalArgumentException
    }

    def 'check that target equals root for path constructed from single dependency'() {
        given:
        LinkedList<TestArtifact> dependencies = [new TestArtifact(new ArtifactName('test', 'a', '1.0'), [])]
        def dependencyPath = new DependencyPath(dependencies)

        when:
        def root = dependencyPath.root
        def target = dependencyPath.targetDependency

        then:
        root == target
    }

    private class TestArtifact implements Artifact<TestArtifact> {
        ArtifactName name
        List<TestArtifact> dependencies

        TestArtifact(name, dependencies) {
            this.name = name
            this.dependencies = dependencies
        }
    }
}
