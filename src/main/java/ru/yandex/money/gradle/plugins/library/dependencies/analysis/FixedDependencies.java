package ru.yandex.money.gradle.plugins.library.dependencies.analysis;

import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactNameSet;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.LibraryName;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Позволяет получить набор имен артефактов, указанных в секции dependencyManagement, для каждой из конфигураций проекта
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 13.03.2017
 */
public class FixedDependencies {
    /**
     * Хранит отображение имени конфигурации в набор зависимостей, указанных в секции dependencyManagement
     */
    private final Map<String, ArtifactNameSet> configurationManagedDependencies;

    /**
     * Жадно загружает все зависимости, указанные в секции dependencyManagement, для каждой из конфигураций проекта.
     * Использует результат работы стороннего плагина <i>io.spring.dependency-management</i>
     *
     * ВАЖНО: необходимо загрузить все managed-зависимости до первого резолва зависимостей,
     * который происходит при первом обращении к resolution result любой конфигурации.
     * Иначе результат вызова {@link DependencyManagementExtension#getManagedVersionsForConfigurationHierarchy(Configuration)}
     * для каждой следующей конфигурации содержит также прямые зависимости конфигурации.
     *
     * @param project текущий проект
     * @return объект класса
     */
    public static FixedDependencies from(Project project) {
        ManagedDependenciesLoader loader = new ManagedDependenciesLoader(project);
        Map<String, ArtifactNameSet> configurationManagedDependencies = project.getConfigurations().stream()
                                                                               .collect(Collectors.toMap(Configuration::getName,
                                                                                                         loader::loadManagedDependencies));
        return new FixedDependencies(configurationManagedDependencies);
    }

    private FixedDependencies(Map<String, ArtifactNameSet> configurationManagedDependencies) {
        this.configurationManagedDependencies = configurationManagedDependencies;
    }

    /**
     * Возвращает набор имен артефактов, указанных в секции dependencyManagement, для данной конфигурации
     *
     * @param configuration конфигурация, для которой необходимо получить набор имен артефактов
     * @return набор имен артефактов, указанных в секции dependencyManagement
     */
    public ArtifactNameSet forConfiguration(@Nonnull Configuration configuration) {
        return configurationManagedDependencies.get(configuration.getName());
    }

    private static class ManagedDependenciesLoader {
        private final Project project;

        private ManagedDependenciesLoader(Project project) {
            this.project = project;
        }

        ArtifactNameSet loadManagedDependencies(@Nonnull Configuration configuration) {
            return ArtifactNameSet.fromLibraryVersions(getManagedLibraries(configuration));
        }

        private Map<LibraryName, Set<String>> getManagedLibraries(@Nonnull Configuration configuration) {
            return getManagedLibraryVersions(configuration)
                    .entrySet().stream()
                    .collect(Collectors.toMap(entry -> LibraryName.parse(entry.getKey()),
                            entry -> Collections.singleton(entry.getValue())));
        }

        @SuppressWarnings("unchecked")
        private Map<String, String> getManagedLibraryVersions(@Nonnull Configuration configuration) {
            return getDependencyManagementExtension().getManagedVersionsForConfigurationHierarchy(configuration);
        }

        private DependencyManagementExtension getDependencyManagementExtension() {
            return project.getExtensions().getByType(DependencyManagementExtension.class);
        }
    }
}
