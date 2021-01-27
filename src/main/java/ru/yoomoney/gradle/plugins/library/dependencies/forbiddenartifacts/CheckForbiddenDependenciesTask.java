package ru.yoomoney.gradle.plugins.library.dependencies.forbiddenartifacts;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.ArtifactName;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.ForbiddenArtifactInfo;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Задача, проверяющая наличие запрещенных артефактов
 *
 * @author horyukova
 * @since 26.05.2019
 */
public class CheckForbiddenDependenciesTask extends DefaultTask {
    private Set<ForbiddenArtifactInfo> forbiddenArtifacts;
    private Logger log = getProject().getLogger();

    public void setForbiddenArtifacts(Set<ForbiddenArtifactInfo> forbiddenArtifacts) {
        this.forbiddenArtifacts = forbiddenArtifacts;
    }

    /**
     * Проверяем наличие запрещенных артефактов
     */
    @TaskAction
    public void forbiddenArtifacts() {
        Set<Dependency> foundForbiddenArtifacts = getProject().getConfigurations().stream()
                .flatMap(configuration -> configuration.getAllDependencies().stream())
                .filter(this::isForbiddenArtifacts)
                .collect(Collectors.toSet()).stream()
                .peek(this::printForbiddenVersion)
                .peek(this::printRecommendedVersion)
                .collect(Collectors.toSet());

        if (!foundForbiddenArtifacts.isEmpty()) {
            throw new GradleException("There is forbidden dependencies");
        }
    }

    private boolean isForbiddenArtifacts(Dependency dependency) {
        ArtifactName artifactName =
                new ArtifactName(dependency.getGroup(), dependency.getName(), dependency.getVersion());

        return forbiddenArtifacts.stream()
                .filter(forbiddenArtifact -> forbiddenArtifact.getForbiddenArtifact().getLibraryName()
                        .equals(artifactName.getLibraryName()))
                .anyMatch(forbiddenArtifact ->
                        forbiddenArtifact.getForbiddenArtifact().isVersionIncludedInRange(artifactName.getVersion()));
    }

    private void printForbiddenVersion(Dependency dependency) {
        ArtifactName artifactName =
                new ArtifactName(dependency.getGroup(), dependency.getName(), dependency.getVersion());

        forbiddenArtifacts.stream()
                .filter(artifact -> artifact.getForbiddenArtifact().getLibraryName()
                        .equals(artifactName.getLibraryName()))
                .forEach(artifact -> log.lifecycle("Forbidden dependency: {}:{}, cause: {}",
                        artifactName.getLibraryName(), artifactName.getVersion(), artifact.getComment()));
    }

    private void printRecommendedVersion(Dependency dependency) {
        ArtifactName artifactName =
                new ArtifactName(dependency.getGroup(), dependency.getName(), dependency.getVersion());

        forbiddenArtifacts.stream()
                .map(ForbiddenArtifactInfo::getRecommendedArtifact)
                .filter(artifact -> artifact.getLibraryName().equals(artifactName.getLibraryName()))
                .forEach(recommendedArtifact -> log.lifecycle("Recommended version: {} {} -> {}",
                        artifactName.getLibraryName(), artifactName.getVersion(), recommendedArtifact.getVersion()));
    }
}
