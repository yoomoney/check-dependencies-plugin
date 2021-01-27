package ru.yoomoney.gradle.plugins.library.dependencies.dsl;

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
 *     <li>{@link ResolvedArtifactDependency}</li>
 *     <li>{@link UnresolvedArtifactDependency}</li>
 * </ul>
 *
 * @author Konstantin Novokreshchenov
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
