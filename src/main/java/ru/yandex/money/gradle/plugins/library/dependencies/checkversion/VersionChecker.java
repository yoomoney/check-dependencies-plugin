package ru.yandex.money.gradle.plugins.library.dependencies.checkversion;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.LibraryName;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Класс для проверки конфликтов мажорных версий подключенных библиотек yamoney
 *
 * @author horyukova
 * @since 07.12.2018
 */
public class VersionChecker {

    /**
     * Запуск проверки
     *
     * @param project проект
     */
    public static void runCheckVersion(Project project,
                                       MajorVersionCheckerExtension majorVersionCheckerExtension) {
        ConfigurationContainer allConfigurations = project.getConfigurations();
        Set<LibraryName> excludedLibraries = majorVersionCheckerExtension.excludeDependencies.stream()
                .map(LibraryName::parse)
                .collect(Collectors.toSet());

        MetricsSender metricsSender = new MetricsSender(project);

        allConfigurations.stream()
                .filter(VersionChecker::isValidConfiguration)
                .forEach(conf -> {
                    Map<LibraryName, Set<String>> conflictModules = new HashMap<>();

                    conf.getResolutionStrategy()
                            .eachDependency(new FindAllVersionConflictAction(excludedLibraries,
                                    majorVersionCheckerExtension.includeGroupIdPrefixes, conflictModules))
                            .eachDependency(new CheckVersionAction(project, conflictModules, majorVersionCheckerExtension,
                                    metricsSender));
                });
    }

    private static boolean isValidConfiguration(Configuration configuration) {
        String configurationLowerName = configuration.getName().toLowerCase();

        return configurationLowerName.endsWith("compile")
                || configurationLowerName.endsWith("runtime")
                || Objects.equals(configurationLowerName, "compileclasspath");
    }
}

