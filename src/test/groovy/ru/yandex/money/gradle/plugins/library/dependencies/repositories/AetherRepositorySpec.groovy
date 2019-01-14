package ru.yandex.money.gradle.plugins.library.dependencies.repositories

import ru.yandex.money.gradle.plugins.library.TestRepositories
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactName
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.LibraryName
import ru.yandex.money.gradle.plugins.library.dependencies.repositories.aether.AetherRepository
import spock.lang.Ignore
import spock.lang.Specification

/**
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 16.03.2017
 */
class AetherRepositorySpec extends Specification {
    def 'check that library versions are found'() {
        given: 'local test repository'
            def urls = [TestRepositories.MAVEN_REPO_2]
            def repository = AetherRepository.create(urls.toList())

        when: 'requesting library versions'
            def versions = repository.findVersions(new LibraryName('test', 'alpha'))

        then:
            versions.size() == 3
            versions.containsAll(['1.1', '1.2', '1.3'])
    }

    def 'check that direct dependencies of given artifact are found'() {
        given: 'local test repository'
            def urls = [TestRepositories.MAVEN_REPO_2]
            def repository = AetherRepository.create(urls.toList())

        when: 'requesting direct dependencies of artifact'
            def artifactName = new ArtifactName('test', 'omega', '1.1')
            def dependencies = repository.findDirectDependencies(artifactName)

        then: 'all dependencies are found'
            dependencies.size() == 4
            dependencies.containsAll([
                    new ArtifactName('test', 'alpha', '1.1'),
                    new ArtifactName('test', 'beta', '1.1'),
                    new ArtifactName('test', 'theta', '1.1'),
                    new ArtifactName('test', 'zeta', '1.1')
            ])
    }

    @Ignore
    def 'check that library versions are found on remote repository'() {
        given: 'remote repositories'
        def urls = ['https://nexus.yamoney.ru/content/repositories/central']
            def repository = AetherRepository.create(urls.toList())

        when: 'requesting versions'
            def versions = repository.findVersions(new LibraryName('junit', 'junit'))

        then: 'versions are found'
            versions.size() > 3
            versions.containsAll(['4.9', '4.10', '4.11', '4.12'])
    }

    @Ignore
    def 'check that library versions are found on multiple repositories'() {
        given: 'local and remote repositories'
        def urls = [TestRepositories.MAVEN_REPO_2, 'https://nexus.yamoney.ru/content/repositories/central/']
            def repository = AetherRepository.create(urls.toList())

        when: 'requesting versions'
            def localArtifactVersions = repository.findVersions(new LibraryName('test', 'alpha'))
            def remoteArtifactVersions = repository.findVersions(new LibraryName('junit', 'junit'))

        then: 'all versions are found'
            localArtifactVersions.size() == 3
            localArtifactVersions.containsAll(['1.1', '1.2', '1.3'])

            remoteArtifactVersions.size() > 3
            remoteArtifactVersions.containsAll(['4.9', '4.10', '4.11', '4.12'])
    }
}
