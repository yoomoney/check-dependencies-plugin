package ru.yandex.money.gradle.plugins.library.dependencies.analysis;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactDependency;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactDependent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Хранит информацию о графе зависимостей для конкретной конфигурации
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 13.03.2017
 */
class ConfigurationDependencies {
    private final Configuration configuration;

    ConfigurationDependencies(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Возвращает корень дерева зависимостей для конфигурации проекта
     *
     * @return корень дерева зависимостей
     */
    ArtifactDependent<ArtifactDependency> root() {
        return getDependencyRoot();
    }

    /**
     * Возвращает прямые зависимости для конфигурации проекта
     *
     * @return список прямых зависимостей
     */
    public List<ArtifactDependency> direct() {
        return getDependencyRoot().getDependencies();
    }

    /**
     * Возвращает все зависимости для конфигурации проекта
     *
     * @return список всех зависимостей
     */
    List<ArtifactDependency> all() {
        return findAllDependencies(configuration).stream()
                    .map(dependency -> ArtifactDependency.create(configuration, dependency))
                    .filter(dependency -> dependency != null)
                    .collect(Collectors.toList());
    }

    private Set<? extends DependencyResult> findAllDependencies(@Nonnull Configuration configuration) {
        return configuration.getIncoming().getResolutionResult().getAllDependencies();
    }

    private ArtifactDependent<ArtifactDependency> getDependencyRoot() {
        ResolvedComponentResult root = configuration.getIncoming().getResolutionResult().getRoot();
        return () -> root.getDependencies().stream()
                         .map(dependency -> ArtifactDependency.create(configuration, dependency))
                         .filter(artifact -> artifact != null)
                         .collect(Collectors.toList());
    }
}
