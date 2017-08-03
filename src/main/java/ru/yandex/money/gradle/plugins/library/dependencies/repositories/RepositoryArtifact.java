package ru.yandex.money.gradle.plugins.library.dependencies.repositories;

import ru.yandex.money.gradle.plugins.library.dependencies.dsl.Artifact;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactName;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Предоставляет информацию об артефакте, хранящемся в данном репозитории
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 15.03.2017
 */
public class RepositoryArtifact implements Artifact<RepositoryArtifact> {
    private final Repository repository;
    private final ArtifactName name;

    public RepositoryArtifact(Repository repository, ArtifactName name) {
        this.repository = repository;
        this.name = name;
    }

    @Override
    public ArtifactName getName() {
        return name;
    }

    @Override
    public List<RepositoryArtifact> getDependencies() {
        List<ArtifactName> childArtifactNames = repository.findDirectDependencies(name);
        return childArtifactNames.stream()
                                 .map(childArtifactName -> new RepositoryArtifact(repository, childArtifactName))
                                 .collect(Collectors.toList());
    }
}
