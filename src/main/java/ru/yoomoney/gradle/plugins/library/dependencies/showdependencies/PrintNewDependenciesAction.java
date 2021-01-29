package ru.yoomoney.gradle.plugins.library.dependencies.showdependencies;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yoomoney.gradle.plugins.library.dependencies.ArtifactVersionResolver;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Выводит новые версии подключаемых библиотек
 *
 * @author horyukova
 * @since 10.12.2018
 */
public class PrintNewDependenciesAction implements Action<Project> {
    private static final Logger log = LoggerFactory.getLogger(PrintNewDependenciesAction.class);

    private final Map<Dependency, Boolean> checked = new HashMap<>();

    /**
     * Список префиксов, для которых нужно выводить зависимости.
     * Если список пуст - выводим все зависимости.
     */
    @Nonnull
    private final Set<String> includeGroupIdPrefixes;
    @Nonnull
    private final ArtifactVersionResolver artifactVersionResolver;

    PrintNewDependenciesAction(@Nonnull ArtifactVersionResolver artifactVersionResolver) {
        this(Collections.emptySet(), artifactVersionResolver);
    }

    PrintNewDependenciesAction(@Nonnull Set<String> includeGroupIdPrefixes,
                               @Nonnull ArtifactVersionResolver artifactVersionResolver) {
        this.includeGroupIdPrefixes = requireNonNull(includeGroupIdPrefixes, "includeGroupIdPrefixes");
        this.artifactVersionResolver = requireNonNull(artifactVersionResolver, "artifactVersionResolver");
    }

    @Override
    public void execute(Project project) {
        ConfigurationContainer configurationContainer = project.getConfigurations();
        for (Configuration configuration : configurationContainer) {
            try {
                Set<ResolvedArtifact> resolvedArtifacts = configuration
                        .getResolvedConfiguration()
                        .getResolvedArtifacts();

                List<ModuleVersionIdentifier> moduleVersionIdentifiers = resolvedArtifacts.stream().map(ra -> ra.getModuleVersion().getId())
                        .collect(Collectors.toList());

                Map<String, String> resolvedVersionMap = moduleVersionIdentifiers.stream()
                        .collect(Collectors.toMap(
                                mvId -> mvId.getGroup() + ":" + mvId.getName(),
                                ModuleVersionIdentifier::getVersion
                        ));

                configuration.getAllDependencies()
                        .forEach(dependency -> {
                                     if (checked.containsKey(dependency)) {
                                         return;
                                     }
                                     checked.put(dependency, true);

                                     if (doNeedPrint(dependency)) {
                                         String realVersion = resolvedVersionMap.get(dependency.getGroup() + ":" + dependency.getName());
                                         try {
                                             printLatestDependencyVersion(dependency, realVersion);
                                         } catch (Exception e) {
                                             log.warn(
                                                     "Can't get latest dependency version: {}:{} {}",
                                                     dependency.getGroup(),
                                                     dependency.getName(),
                                                     realVersion
                                             );
                                         }

                                     }
                                 }
                        );
            } catch (IllegalStateException e) {
                log.info(String.format("The trouble with resolve configuration: configuration=%s", configuration.getName()), e);
            }
        }
    }

    private void printLatestDependencyVersion(Dependency dependency, String realVersion) {
        Optional.of(dependency)
                .filter(dep -> dep.getGroup() != null)
                .map(dep -> artifactVersionResolver.getArtifactLatestVersion(dependency.getGroup(), dependency.getName()))
                .filter(newVersion -> !Objects.equals(realVersion, newVersion))
                .ifPresent(newVersion -> printNewVersion(dependency, realVersion, newVersion));
    }

    private void printNewVersion(Dependency dependency, String realVersion, String newVersion) {
        log.warn("New available version: {}:{} {} -> {}", dependency.getGroup(), dependency.getName(), realVersion, newVersion);
    }

    private boolean doNeedPrint(@Nonnull Dependency dependency) {
        requireNonNull(dependency, "dependency");

        if (includeGroupIdPrefixes.isEmpty()) {
            return true;
        }

        return includeGroupIdPrefixes.stream()
                .anyMatch(prefix -> dependency.getGroup() != null && dependency.getGroup().startsWith(prefix));
    }
}
