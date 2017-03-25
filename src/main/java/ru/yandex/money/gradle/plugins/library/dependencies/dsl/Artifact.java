package ru.yandex.money.gradle.plugins.library.dependencies.dsl;

/**
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 15.03.2017
 */
public interface Artifact<T extends Artifact<T>> extends ArtifactDependent<T> {
    ArtifactName getName();
}
