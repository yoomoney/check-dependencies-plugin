package ru.yoomoney.gradle.plugins.library.dependencies.utils

import ru.yoomoney.gradle.plugins.library.dependencies.dsl.Artifact
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.ArtifactName

/**
 * @author Konstantin Novokreshchenov
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