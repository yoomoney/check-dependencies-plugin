package ru.yandex.money.gradle.plugins.library.dependencies.showdependencies;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


import static ru.yandex.money.gradle.plugins.library.dependencies.NexusUtils.getArtifactLatestVersion;

/**
 * Выводит новые версии подключаемых библиотек
 *
 * @author horyukova
 * @since 10.12.2018
 */
public class PrintDependenciesAction implements Action<Project> {
    private static final Logger log = LoggerFactory.getLogger(PrintDependenciesAction.class);

    private final Map<Dependency, Boolean> checked = new HashMap<>();

    private final DependencyType dependencyType;

    PrintDependenciesAction(DependencyType dependencyType) {
        this.dependencyType = dependencyType;
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
                        .collect(Collectors.toMap(mvId -> mvId.getGroup() + ":" + mvId.getName(),
                                ModuleVersionIdentifier::getVersion));

                configuration.getAllDependencies()
                        .forEach(dependency -> {
                                    if (checked.containsKey(dependency)) {
                                        return;
                                    }
                                    checked.put(dependency, true);

                                    if (dependencyType.isCorresponds(dependency)) {
                                        String realVersion = resolvedVersionMap.get(dependency.getGroup() + ":" + dependency.getName());
                                        try {
                                            printLatestDependencyVersion(dependency, realVersion);
                                        } catch (Exception e) {
                                            log.warn("Can't get latest dependency version: {}:{} {}",
                                                    dependency.getGroup(),
                                                    dependency.getName(),
                                                    realVersion);
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
                .map(dep -> getArtifactLatestVersion(dependency.getGroup(), dependency.getName()))
                .filter(newVersion -> !Objects.equals(realVersion, newVersion))
                .ifPresent(newVersion -> printNewVersion(dependency, realVersion, newVersion));
    }

    private void printNewVersion(Dependency dependency, String realVersion, String newVersion) {
        log.warn("New available version: {}:{} {} -> {}", dependency.getGroup(), dependency.getName(), realVersion, newVersion);
    }
}
