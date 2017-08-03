package ru.yandex.money.gradle.plugins.library.dependencies.dsl;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ComponentSelector;
import org.gradle.api.artifacts.component.ModuleComponentSelector;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
import org.gradle.api.artifacts.result.UnresolvedDependencyResult;

import javax.annotation.Nullable;

/**
 * <p>Предоставляет информацию о зависимости от артефакта, которая описывается:</p>
 *
 * <ul>
 *     <li>конфигурацией, в которой объявлена зависимость</li>
 *     <li>запрошенным именем библиотеки и версией артефакта</li>
 *     <li>разрешенным именем библиотеки и версией артефакта</li>
 * </ul>
 *
 * <p>Реализуется в следующих классах:</p>
 *
 * <ul>
 *     <li>{@link ru.yandex.money.gradle.plugins.library.dependencies.dsl.ResolvedArtifactDependency}</li>
 *     <li>{@link ru.yandex.money.gradle.plugins.library.dependencies.dsl.UnresolvedArtifactDependency}</li>
 * </ul>
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 14.03.2017
 */
public interface ArtifactDependency extends Artifact<ArtifactDependency> {

    LibraryName getRequestedLibraryName();

    String getRequestedVersion();

    LibraryName getSelectedLibraryName();

    String getSelectedVersion();

    @Override
    default ArtifactName getName() {
        return getRequestedArtifactName();
    }

    default ArtifactName getRequestedArtifactName() {
        return new ArtifactName(getRequestedLibraryName(), getRequestedVersion());
    }

    default ArtifactName getSelectedArtifactName() {
        return new ArtifactName(getSelectedLibraryName(), getSelectedVersion());
    }
}
