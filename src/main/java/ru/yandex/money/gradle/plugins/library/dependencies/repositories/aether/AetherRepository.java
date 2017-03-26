package ru.yandex.money.gradle.plugins.library.dependencies.repositories.aether;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.aether.resolution.ArtifactDescriptorRequest;
import org.sonatype.aether.resolution.ArtifactDescriptorResult;
import org.sonatype.aether.resolution.VersionRangeRequest;
import org.sonatype.aether.resolution.VersionRangeResolutionException;
import org.sonatype.aether.resolution.VersionRangeResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.version.Version;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactName;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.LibraryName;
import ru.yandex.money.gradle.plugins.library.dependencies.repositories.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Предоставляет информацию о всех артефактах (версиях и зависимостях),
 * хранящихся на одном или нескольких репозиториях
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 16.03.2017
 */
public class AetherRepository implements Repository {
    private static final String ALL_LIBRARY_VERSIONS_REQUEST_FORMAT = "%s:%s:[0,)";
    private static final String ARTIFACT_DIRECT_DEPENDENCIES_REQUEST_FORMAT = "%s:%s:%s";

    private final Logger log = Logging.getLogger(AetherRepository.class);
    private final RepositorySystem repositorySystem;
    private final RepositorySystemSession session;
    private final List<RemoteRepository> repositories;

    /**
     * Создает объект класса {@link Repository} для получения информации об артефактах в репозиториях,
     * указанных в переданных URL-адреса
     *
     * @param repositoryUrls URL-адреса репозиториев
     * @return новый экземпляр класса {@link Repository}
     */

    public static Repository create(List<String> repositoryUrls) {
        RepositorySystem repositorySystem = AetherService.newRepositorySystem();
        RepositorySystemSession session = AetherService.newRepositorySystemSession(repositorySystem);
        List<RemoteRepository> repositories = AetherService.createRemoteRepositories(repositoryUrls);
        return new AetherRepository(repositorySystem, session, repositories);
    }

    private AetherRepository(RepositorySystem repositorySystem,
                             RepositorySystemSession session,
                             List<RemoteRepository> repositories) {
        this.repositorySystem = repositorySystem;
        this.session = session;
        this.repositories = repositories;
    }

    @Override
    public List<String> findVersions(LibraryName libraryName) {
        VersionRangeRequest request = createVersionRangeRequest(libraryName);

        VersionRangeResult result = null;
        try {
            result = repositorySystem.resolveVersionRange(session, request);
        } catch (VersionRangeResolutionException e) {
            log.warn("Version request failed: {}, {}", e.getResult(), e.getMessage());
        }

        return result != null
               ? result.getVersions().stream().map(Version::toString).collect(Collectors.toList())
               : Collections.emptyList();
    }

    private VersionRangeRequest createVersionRangeRequest(LibraryName libraryName) {
        Artifact artifact = new DefaultArtifact(getVersionsRequestString(libraryName));
        VersionRangeRequest request = new VersionRangeRequest();

        request.setArtifact(artifact);
        request.setRepositories(repositories);

        return request;
    }

    private String getVersionsRequestString(LibraryName libraryName) {
        return String.format(ALL_LIBRARY_VERSIONS_REQUEST_FORMAT, libraryName.getGroup(), libraryName.getName());
    }

    @Override
    public List<ArtifactName> findDirectDependencies(ArtifactName artifactName) {
        ArtifactDescriptorRequest request = createDirectDependenciesRequest(artifactName);

        ArtifactDescriptorResult result = null;
        try {
            result = repositorySystem.readArtifactDescriptor(session, request);
        } catch (ArtifactDescriptorException e) {
            e.printStackTrace();
        }

        if (result != null) {
          return result.getDependencies()
                       .stream()
                       .map(dependency -> {
                           Artifact artifactDependency = dependency.getArtifact();
                           return new ArtifactName(artifactDependency.getGroupId(),
                                                   artifactDependency.getArtifactId(),
                                                   artifactDependency.getVersion());
                       })
                       .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private ArtifactDescriptorRequest createDirectDependenciesRequest(ArtifactName artifactName) {
        Artifact artifact = new DefaultArtifact(getDirectDependenciesRequestString(artifactName));
        ArtifactDescriptorRequest request = new ArtifactDescriptorRequest();

        request.setArtifact(artifact);
        request.setRepositories(repositories);

        return request;
    }

    private String getDirectDependenciesRequestString(ArtifactName artifactName) {
        LibraryName libraryName = artifactName.getLibraryName();
        return String.format(ARTIFACT_DIRECT_DEPENDENCIES_REQUEST_FORMAT,
                             libraryName.getGroup(), libraryName.getName(), artifactName.getVersion());
    }
}
