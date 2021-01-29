package ru.yoomoney.gradle.plugins.library.dependencies.analysis.conflicts.resolvers;

import ru.yoomoney.gradle.plugins.library.dependencies.dsl.ArtifactDependency;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.ArtifactName;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.DependencyPath;

/**
 * Информация о конфликте версий.
 * Содержит путь зависимостей, приводящий к конфликту.
 *
 * @author Konstantin Novokreshchenov
 * @since 25.03.2017
 */
public class VersionConflictInfo {
    private final DependencyPath<ArtifactDependency> conflictPath;
    private final String targetDependencyFixedVersion;

    public VersionConflictInfo(DependencyPath<ArtifactDependency> conflictPath, String targetDependencyFixedVersion) {
        this.conflictPath = conflictPath;
        this.targetDependencyFixedVersion = targetDependencyFixedVersion;
    }

    ArtifactName getDirectDependency() {
        return conflictPath.getRoot().getName();
    }

    ArtifactName getTargetFixedDependency() {
        return ArtifactName.changeVersion(conflictPath.getTargetDependency().getName(), targetDependencyFixedVersion);
    }

    DependencyPath<ArtifactDependency> getConflictPath() {
        return conflictPath;
    }
}
