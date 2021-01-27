package ru.yoomoney.gradle.plugins.library.dependencies.analysis


import ru.yoomoney.gradle.plugins.library.dependencies.utils.TestArtifact
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.ArtifactName
import spock.lang.Specification
/**
 * @author Konstantin Novokreshchenov
 * @since 17.03.2017
 */
class ArtifactDependentPathsFinderSpec extends Specification {

    def 'check that traverse looping dependencies successfully'() {
        given: 'dependency tree with loop'
            def a = new TestArtifact(new ArtifactName('test', 'a', '1.1'))
            def b = new TestArtifact(new ArtifactName('test', 'b', '1.1'), Arrays.asList(a))
            a.dependencies.add(b)

        when: 'run full traversing'
            def finder = new ArtifactDependentPathsFinder<>(a, { false })
            finder.findPaths()

        then:
            true
    }
}
