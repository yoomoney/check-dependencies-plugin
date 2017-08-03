package ru.yandex.money.gradle.plugins.library.dependencies.analysis;

import ru.yandex.money.gradle.plugins.library.dependencies.dsl.Artifact;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactDependent;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactName;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.DependencyPath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Ищет в переданном дереве артефактов все пути до артефактов, удовлетворяющих данному условию
 *
 * @param <ArtifactT> тип артефакта, реализующий интерфейс {@link Artifact}
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 14.03.2017
 */
public class ArtifactDependentPathsFinder<ArtifactT extends Artifact<ArtifactT>> {
    private final ArtifactDependent<ArtifactT> root;
    private final Predicate<? super ArtifactName> isRequestedArtifact;

    private Set<ArtifactName> visitedArtifacts;
    private List<DependencyPath<ArtifactT>> foundPaths;

    public ArtifactDependentPathsFinder(ArtifactDependent<ArtifactT> root,
                                        Predicate<? super ArtifactName> isRequestedArtifact) {
        this.root = root;
        this.isRequestedArtifact = isRequestedArtifact;
    }

    /**
     * Начинает поиск всех путей до артефактов, имя которых удовлетворяет условию
     *
     * @return список найденных путей
     */
    public List<DependencyPath<ArtifactT>> findPaths() {
        initialize();

        traverse(root, DependencyPathBuilder.create());

        return foundPaths;
    }

    private void initialize() {
        visitedArtifacts = new HashSet<>();
        foundPaths = new ArrayList<>();
    }

    private void traverse(ArtifactDependent<ArtifactT> root, DependencyPathBuilder<ArtifactT> dependencyPathBuilder) {
        for (ArtifactT artifact: root.getDependencies()) {
            visitDependency(artifact, dependencyPathBuilder);
        }
    }

    private void visitDependency(ArtifactT dependency, DependencyPathBuilder<ArtifactT> dependencyPathBuilder) {
        dependencyPathBuilder = dependencyPathBuilder.copy();
        dependencyPathBuilder.add(dependency);

        if (isRequested(dependency)) {
            foundPaths.add(dependencyPathBuilder.build());
            return;
        }

        if (visitedArtifacts.contains(dependency.getName())) {
            return;
        }

        visitedArtifacts.add(dependency.getName());

        for (ArtifactT childDependency: dependency.getDependencies()) {
            visitDependency(childDependency, dependencyPathBuilder);
        }
    }

    private boolean isRequested(ArtifactT foundArtifact) {
        ArtifactName foundArtifactName = foundArtifact.getName();
        return isRequestedArtifact.test(foundArtifactName);
    }
}

