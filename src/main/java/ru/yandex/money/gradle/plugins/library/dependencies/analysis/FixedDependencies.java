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
    private final Project project;

    public FixedDependencies(Project project) {
        this.project = project;
    }

    /**
     * Возвращает набор имен артефактов, указанных в секции dependencyManagement, для данной конфигурации
     * <p>
     * Использует результат работы стороннего плагина <i>io.spring.dependency-management</i>
     *
     * @param configuration конфигурация, для которой необходимо получить набор имен артефактов
     * @return набор имен артефактов, указанных в секции dependencyManagement
     */
    ArtifactNameSet forConfiguration(@Nonnull Configuration configuration) {
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
