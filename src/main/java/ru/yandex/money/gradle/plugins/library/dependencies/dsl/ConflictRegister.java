package ru.yandex.money.gradle.plugins.library.dependencies.dsl;

/**
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 25.03.2017
 */
public interface ConflictRegister {
    void registerConflict(ArtifactName requestedArtifact, String fixedVersion);
}
