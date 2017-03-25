package ru.yandex.money.gradle.plugins.library.dependencies.dsl;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ModuleComponentSelector;

import java.util.Collections;
import java.util.List;

/**
 * Содержит информацию о неразрешенной зависимости от артефакта
 *
 * @see ArtifactDependency
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 14.03.2017
 */
public class UnresolvedArtifactDependency implements ArtifactDependency {
    private final Configuration configuration;
    private final ModuleComponentSelector selector;

    public UnresolvedArtifactDependency(Configuration configuration,
                                        ModuleComponentSelector selector) {
        this.configuration = configuration;
        this.selector = selector;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
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
        return getRequestedLibraryName();
    }

    @Override
    public String getSelectedVersion() {
        return getRequestedVersion();
    }

    @Override
    public List<ArtifactDependency> getDependencies() {
        return Collections.emptyList();
    }
}
