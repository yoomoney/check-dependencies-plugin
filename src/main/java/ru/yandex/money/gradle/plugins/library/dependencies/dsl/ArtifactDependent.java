package ru.yandex.money.gradle.plugins.library.dependencies.dsl;

import java.util.List;

/**
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 15.03.2017
 */
public interface ArtifactDependent<TArtifact extends Artifact<TArtifact>> {
    List<TArtifact> getDependencies();
}
