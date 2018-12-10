package ru.yandex.money.gradle.plugins.library.dependencies.showdependencies;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.yandex.money.gradle.plugins.library.dependencies.NexusUtils.getArtifactLatestVersion;
import static ru.yandex.money.gradle.plugins.library.dependencies.showdependencies.DependencyType.INNER;
import static ru.yandex.money.gradle.plugins.library.dependencies.showdependencies.DependencyType.OUTER;

/**
 * Выводит новые версии подключаемых библиотек
 *
 * @author horyukova
 * @since 10.12.2018
 */
public class PrintDependenciesAction implements Action<Project> {
    private final Logger log = LoggerFactory.getLogger(PrintDependenciesAction.class);

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
                Map<String, String> resolvedVersionMap = configuration
                        .getResolvedConfiguration()
                        .getResolvedArtifacts().stream()
                        .collect(Collectors.toMap(ResolvedArtifact::getName,
                                resolvedArtifact -> resolvedArtifact.getModuleVersion().getId().getVersion()));

                configuration.getAllDependencies()
                        .forEach(dependency -> {
                                    if (checked.containsKey(dependency)) {
                                        return;
                                    }
                                    checked.put(dependency, true);

                                    if ((dependencyType == INNER && isInnerDependencies(dependency))
                                            || (dependencyType == OUTER && !isInnerDependencies(dependency))) {
                                        printLatestDependencyVersion(dependency, resolvedVersionMap.get(dependency.getName()));
                                    }
                                }
                        );
            } catch (IllegalStateException e) {
                log.info(String.format("The trouble with resolve configuration: configuration=%s", configuration.getName()), e);
            }
        }
    }

    private static boolean isInnerDependencies(Dependency dependency) {
        return dependency.getGroup().startsWith("ru.yandex.money")
                || dependency.getGroup().startsWith("ru.yamoney");
    }

    private void printLatestDependencyVersion(Dependency dependency, String realVersion) {
        String newest = getArtifactLatestVersion(dependency.getGroup(), dependency.getName());

        if (!Objects.equals(realVersion, newest)) {
            log.warn("{}:{} {} -> {}", dependency.getGroup(), dependency.getName(), realVersion, newest);
        }
    }
}
