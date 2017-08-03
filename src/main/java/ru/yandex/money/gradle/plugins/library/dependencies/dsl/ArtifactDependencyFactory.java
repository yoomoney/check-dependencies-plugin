package ru.yandex.money.gradle.plugins.library.dependencies.dsl;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ComponentSelector;
import org.gradle.api.artifacts.component.ModuleComponentSelector;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
import org.gradle.api.artifacts.result.UnresolvedDependencyResult;

import javax.annotation.Nullable;

/**
 * Фабрика для создания объектов, реализующих интерфейс {@link ArtifactDependency}
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 13.04.2017
 */
public class ArtifactDependencyFactory {

    private ArtifactDependencyFactory() {}

    /**
     * Фабричный метод для создания объекта, реализующуего данный интерфейс,
     * на основе переданной конфигурации проекта и результата резолва зависимости
     *
     * @param configuration конфигурация проекта
     * @param dependency зависимость (прямая или транзитивная) конфигурации проекта
     * @return новый объект, реализующий данный интерфейс
     */
    @Nullable
    public static ArtifactDependency create(Configuration configuration, DependencyResult dependency) {
        ComponentSelector selector = dependency.getRequested();
        if (selector instanceof ModuleComponentSelector) {
            ModuleComponentSelector moduleSelector = (ModuleComponentSelector) selector;

            if (dependency instanceof ResolvedDependencyResult) {
                ResolvedDependencyResult resolvedDependency = (ResolvedDependencyResult) dependency;
                return new ResolvedArtifactDependency(configuration, resolvedDependency, moduleSelector);
            }

            if (dependency instanceof UnresolvedDependencyResult) {
                return new UnresolvedArtifactDependency(moduleSelector);
            }
        }
        return null;
    }
}
