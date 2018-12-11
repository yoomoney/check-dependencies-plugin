package ru.yandex.money.gradle.plugins.library.dependencies.checkversion;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.DependencyResolveDetails;
import org.gradle.api.Project;
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
    private static final Map<LibraryName, Set<String>> conflictModules = new HashMap<>();

    /**
     * Запуск проверки
     *
     * @param project проект
     */
    public static void runCheckVersion(Project project,
                                       Set<String> excludedLibraries,
                                       Set<String> includePrefixLibraries) {
        ConfigurationContainer allConfigurations = project.getConfigurations();

        Action<DependencyResolveDetails> findAllVersionConflictAction =
                new FindAllVersionConflictAction(excludedLibraries, includePrefixLibraries, conflictModules);

        Action<DependencyResolveDetails> checkVersionAction =
                new CheckVersionAction(project, conflictModules);

        allConfigurations.stream()
                .filter(VersionChecker::isValidConfiguration)
                .forEach(conf -> conf.getResolutionStrategy()
                        .eachDependency(findAllVersionConflictAction)
                        .eachDependency(checkVersionAction));
    }

    private static boolean isValidConfiguration(Configuration configuration) {
        String configurationLowerName = configuration.getName().toLowerCase();

        return configurationLowerName.endsWith("compile")
                || configurationLowerName.endsWith("runtime")
                || Objects.equals(configurationLowerName, "compileclasspath");
    }
}

