package ru.yandex.money.gradle.plugins.library.dependencies.extensions;

import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactName;

/**
 * Расширение класса {@link ArtifactName}
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 26.03.2017
 */
public class ArtifactNameEx {
    private ArtifactNameEx() { }

    public static ArtifactName changeVersion(ArtifactName artifactName, String version) {
        return new ArtifactName(artifactName.getLibraryName(), version);
    }
}
