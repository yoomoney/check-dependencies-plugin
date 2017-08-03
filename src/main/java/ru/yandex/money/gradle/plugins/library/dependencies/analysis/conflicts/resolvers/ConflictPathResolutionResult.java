package ru.yandex.money.gradle.plugins.library.dependencies.analysis.conflicts.resolvers;

import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactName;

import java.util.Set;

/**
 * Результат анализа версий прямой зависимости, не приводящих к возникновению конфликтов
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 25.03.2017
 */
public class ConflictPathResolutionResult {
    private final ArtifactName directDependency;
    private final Set<String> checkedVersions;
    private final Set<String> suggestedVersions;

    ConflictPathResolutionResult(ArtifactName directDependency,
                                 Set<String> checkedVersions,
                                 Set<String> suggestedVersions) {

        this.directDependency = directDependency;
        this.checkedVersions = checkedVersions;
        this.suggestedVersions = suggestedVersions;
    }

    public ArtifactName getDirectDependency() {
        return directDependency;
    }

    public Set<String> getCheckedVersions() {
        return checkedVersions;
    }

    public Set<String> getSuggestedVersions() {
        return suggestedVersions;
    }
}
