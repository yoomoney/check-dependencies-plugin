package ru.yandex.money.gradle.plugins.library.dependencies.analysis.conflicts.resolvers;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactDependency;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactName;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.VersionSelectors;
import ru.yandex.money.gradle.plugins.library.dependencies.repositories.Repository;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Резолвер конфликтов версий, использующий информацию об артефактах, хранящихся в переданном репозитории
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 25.03.2017
 */
public class RepositoryVersionConflictResolver implements VersionConflictResolver {
    private final Logger log = Logging.getLogger(RepositoryVersionConflictResolver.class);

    private final Repository repository;
    private final VersionSelectors versionSelectors;

    public RepositoryVersionConflictResolver(Repository repository, VersionSelectors versionSelectors) {
        this.repository = repository;
        this.versionSelectors = versionSelectors;
    }

    @Override
    public ConflictPathResolutionResult resolveConflict(VersionConflictInfo conflictInfo) {
        ArtifactName directDependency = conflictInfo.getDirectDependency();
        Set<String> alternativeVersions = findAlternativeVersions(directDependency);
        Set<String> suggestedVersions = findAppropriateVersions(conflictInfo, alternativeVersions);
        return new ConflictPathResolutionResult(directDependency, alternativeVersions, suggestedVersions);
    }

    private Set<String> findAlternativeVersions(ArtifactName artifactName) {
        Predicate<String> versionSelector = versionSelectors.forLibrary(artifactName.getLibraryName());
        return repository.findVersions(artifactName.getLibraryName())
                         .stream()
                         .filter(versionSelector)
                         .collect(Collectors.toSet());
    }

    private Set<String> findAppropriateVersions(VersionConflictInfo conflictInfo, Set<String> directDependencyAlternativeVersions) {
        ArtifactName directDependency = conflictInfo.getDirectDependency();
        ArtifactName fixedDependency = conflictInfo.getTargetFixedDependency();

        if (directDependency.getLibraryName().equals(fixedDependency.getLibraryName())) {
            return directDependencyAlternativeVersions.contains(fixedDependency.getVersion())
                    ? Collections.singleton(fixedDependency.getVersion())
                    : Collections.emptySet();
        }

        ApproximateArtifactDependenciesAnalyzer<ArtifactDependency> dependenciesAnalyzer =
                new ApproximateArtifactDependenciesAnalyzer<>(repository, conflictInfo.getConflictPath());

        return directDependencyAlternativeVersions.stream()
                .filter(version -> {
                    ArtifactName alternativeDirectDependency = new ArtifactName(directDependency.getLibraryName(), version);
                    return !dependenciesAnalyzer.hasAnotherDependencyVersions(alternativeDirectDependency,
                                                                              fixedDependency.getLibraryName(),
                                                                              fixedDependency.getVersion());
                })
                .collect(Collectors.toSet());
    }
}
