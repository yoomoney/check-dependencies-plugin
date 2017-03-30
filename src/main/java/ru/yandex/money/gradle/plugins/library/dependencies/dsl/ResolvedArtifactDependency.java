package ru.yandex.money.gradle.plugins.library.dependencies.dsl;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ModuleComponentSelector;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Содержит информацию о разрешенной зависимости от артефакта
 *
 * @see ArtifactDependency
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 14.03.2017
 */
class ResolvedArtifactDependency implements ArtifactDependency {
    private final Configuration configuration;
    private final ResolvedComponentResult component;
    private final ModuleComponentSelector selector;

    /**
     * Конструктор класса
     *
     * @param configuration проверяемая конфигурация проекта
     * @param dependency успешно разрезолвленная зависимость
     * @param selector селектор для получения первоначально запрашиваемого имени зависимости
     */
    ResolvedArtifactDependency(Configuration configuration,
                               ResolvedDependencyResult dependency,
                               ModuleComponentSelector selector) {
        this.configuration = configuration;
        this.component = dependency.getSelected();
        this.selector = selector;
    }

    @Override
    public LibraryName getRequestedLibraryName() {
        return new LibraryName(selector.getGroup(), selector.getModule());
    }

    @Override
    public String getRequestedVersion() {
        return selector.getVersion();
    }

    @Override
    public LibraryName getSelectedLibraryName() {
        return new LibraryName(component.getModuleVersion().getGroup(), component.getModuleVersion().getName());
    }

    @Override
    public String getSelectedVersion() {
        return component.getModuleVersion().getVersion();
    }

    @Override
    public List<ArtifactDependency> getDependencies() {
        return component.getDependencies()
                        .stream()
                        .map(dependency -> ArtifactDependency.create(configuration, dependency))
                        .filter(dependency -> dependency != null).collect(Collectors.toList());
    }
}
