package ru.yoomoney.gradle.plugins.library.dependencies.showdependencies;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import ru.yoomoney.gradle.plugins.library.dependencies.ArtifactVersionResolver;

import javax.annotation.Nonnull;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Выводит версии зависимостей по списку префиксов
 *
 * @author horyukova
 * @since 10.12.2018
 */
public class PrintDependenciesByGroupTask extends DefaultTask {
    private Set<String> includeGroupIdPrefixes;
    private ArtifactVersionResolver artifactVersionResolver;

    /**
     * Выводит новые версии библиотек по списку префиксов
     */
    @TaskAction
    public void printInnerVersion() {
        getProject().getLogger().lifecycle("===============New dependencies by group===============");

        Action<Project> printDependenciesAction = new PrintNewDependenciesAction(includeGroupIdPrefixes, artifactVersionResolver);
        getProject().allprojects(printDependenciesAction);

        getProject().getLogger().lifecycle("====================================================");
    }

    public void setIncludeGroupIdPrefixes(@Nonnull Set<String> includeGroupIdPrefixes) {
        this.includeGroupIdPrefixes = requireNonNull(includeGroupIdPrefixes, "includeGroupIdPrefixes");
    }

    public void setArtifactVersionResolver(ArtifactVersionResolver artifactVersionResolver) {
        this.artifactVersionResolver = artifactVersionResolver;
    }
}
