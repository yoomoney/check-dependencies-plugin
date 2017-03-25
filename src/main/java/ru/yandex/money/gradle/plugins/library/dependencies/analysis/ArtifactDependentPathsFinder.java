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
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 14.03.2017
 */
class ArtifactDependentPathsFinder<TArtifact extends Artifact<TArtifact>> {
    private final ArtifactDependent<TArtifact> root;
    private final Predicate<? super ArtifactName> isRequestedArtifact;

    private Set<ArtifactName> visitedArtifacts;
    private List<DependencyPath<TArtifact>> foundPaths;

    ArtifactDependentPathsFinder(ArtifactDependent<TArtifact> root,
                                 Predicate<? super ArtifactName> isRequestedArtifact) {
        this.root = root;
        this.isRequestedArtifact = isRequestedArtifact;
    }

    /**
     * Начинает поиск всех путей до артефактов, имя которых удовлетворяет условию
     *
     * @return список найденных путей
     */
    List<DependencyPath<TArtifact>> findPaths() {
        initialize();

        traverse(root, DependencyPathBuilder.create());

        return foundPaths;
    }

    private void initialize() {
        visitedArtifacts = new HashSet<>();
        foundPaths = new ArrayList<>();
    }

    private void traverse(ArtifactDependent<TArtifact> root, DependencyPathBuilder<TArtifact> dependencyPathBuilder) {
        for (TArtifact artifact: root.getDependencies()) {
            visitDependency(artifact, dependencyPathBuilder);
        }
    }

    private void visitDependency(TArtifact dependency, DependencyPathBuilder<TArtifact> dependencyPathBuilder) {
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

        for(TArtifact childDependency: dependency.getDependencies()) {
            visitDependency(childDependency, dependencyPathBuilder);
        }
    }

    private boolean isRequested(TArtifact foundArtifact) {
        ArtifactName foundArtifactName = foundArtifact.getName();
        return isRequestedArtifact.test(foundArtifactName);
    }
}

