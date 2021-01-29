package ru.yoomoney.gradle.plugins.library.dependencies.showdependencies;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import ru.yoomoney.gradle.plugins.library.dependencies.ArtifactVersionResolver;

/**
 * Выводит версии всех зависимостей
 *
 * @author horyukova
 * @since 10.12.2018
 */
public class PrintNewDependenciesVersionsTask extends DefaultTask {
    private ArtifactVersionResolver artifactVersionResolver;

    /**
     * Выводит новые версии всех библиотек
     */
    @TaskAction
    public void printNewVersion() {
        getProject().getLogger().lifecycle("===============New dependencies===============");

        Action<Project> printDependenciesAction = new PrintNewDependenciesAction(artifactVersionResolver);
        getProject().allprojects(printDependenciesAction);

        getProject().getLogger().lifecycle("====================================================");
    }

    public void setArtifactVersionResolver(ArtifactVersionResolver artifactVersionResolver) {
        this.artifactVersionResolver = artifactVersionResolver;
    }
}
