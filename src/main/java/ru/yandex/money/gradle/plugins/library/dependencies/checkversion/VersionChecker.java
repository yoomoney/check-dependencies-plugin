package ru.yandex.money.gradle.plugins.library.dependencies.checkversion;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.DependencyResolveDetails;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.LibraryName;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


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
                                       Set<LibraryName> excludedLibraries,
                                       Set<String> includePrefixLibraries) {
        ConfigurationContainer allConfigurations = project.getConfigurations();

        allConfigurations.stream()
                .filter(VersionChecker::isValidConfiguration)
                .forEach(conf -> {
                    Map<LibraryName, Set<String>> conflictModules = new HashMap<>();

                    conf.getResolutionStrategy()
                            .eachDependency(new FindAllVersionConflictAction(excludedLibraries, includePrefixLibraries, conflictModules))
                            .eachDependency(new CheckVersionAction(project, conflictModules));
                });
    }

    private static boolean isValidConfiguration(Configuration configuration) {
        String configurationLowerName = configuration.getName().toLowerCase();

        return configurationLowerName.endsWith("compile")
                || configurationLowerName.endsWith("runtime")
                || Objects.equals(configurationLowerName, "compileclasspath");
    }
}

