package ru.yoomoney.gradle.plugins.library.dependencies.analysis;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.ArtifactDependency;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.ArtifactDependencyFactory;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.ArtifactDependent;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Хранит информацию о графе зависимостей для конкретной конфигурации
 *
 * @author Konstantin Novokreshchenov
 * @since 13.03.2017
 */
public class ConfigurationDependencies {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationDependencies.class);

    private final Configuration configuration;

    public ConfigurationDependencies(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Возвращает корень дерева зависимостей для конфигурации проекта
     *
     * @return корень дерева зависимостей
     */
    public ArtifactDependent<ArtifactDependency> root() {
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
    public List<ArtifactDependency> all() {
        return findAllDependencies(configuration).stream()
                    .map(dependency -> ArtifactDependencyFactory.create(configuration, dependency))
                    .filter(dependency -> dependency != null)
                    .collect(Collectors.toList());
    }

    private Set<? extends DependencyResult> findAllDependencies(@Nonnull Configuration configuration) {
        try {
            return configuration.getIncoming().getResolutionResult().getAllDependencies();
        } catch (Exception ex) {
            log.info("Failed to resolve dependencies of configuration {} with message: {}",
                    configuration.getName(), ex.getMessage());
            return Collections.emptySet();
        }
    }

    private ArtifactDependent<ArtifactDependency> getDependencyRoot() {
        ResolvedComponentResult root = configuration.getIncoming().getResolutionResult().getRoot();
        return () -> root.getDependencies().stream()
                         .map(dependency -> ArtifactDependencyFactory.create(configuration, dependency))
                         .filter(artifact -> artifact != null)
                         .collect(Collectors.toList());
    }
}
