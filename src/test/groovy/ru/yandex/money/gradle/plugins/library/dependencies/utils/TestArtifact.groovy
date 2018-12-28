package ru.yandex.money.gradle.plugins.library.dependencies.utils

import ru.yandex.money.gradle.plugins.library.dependencies.dsl.Artifact
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactName

/**
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru) 
 * @since 26.03.2017
 */
public class TestArtifact implements Artifact<TestArtifact> {
    List<TestArtifact> dependencies = new ArrayList<>()
    ArtifactName name

    TestArtifact(String name) {
        this(ArtifactName.parse(name))
    }

    TestArtifact(ArtifactName name) {
        this(name, Collections.emptyList())
    }

    TestArtifact(ArtifactName name, List<TestArtifact> dependencies) {
        this.name = name
        this.dependencies.addAll(dependencies)
    }
}