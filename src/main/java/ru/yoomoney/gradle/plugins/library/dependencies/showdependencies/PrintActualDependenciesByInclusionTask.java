package ru.yoomoney.gradle.plugins.library.dependencies.showdependencies;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import javax.annotation.Nonnull;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Выводит актуальные версии зависимостей по списку префиксов
 */
public class PrintActualDependenciesByInclusionTask extends DefaultTask {
    private Set<String> includeGroupIdPrefixes;

    /**
     * Выводит актуальные версии библиотек по списку префиксов
     */
    @TaskAction
    public void printActualInnerVersion() {
        getProject().getLogger().lifecycle("===============Actual dependencies by inclusion===============");

        Action<Project> printDependenciesAction = new PrintActualDependenciesAction(includeGroupIdPrefixes);
        getProject().allprojects(printDependenciesAction);

        getProject().getLogger().lifecycle("====================================================");
    }

    public void setIncludeGroupIdPrefixes(@Nonnull Set<String> includeGroupIdPrefixes) {
        this.includeGroupIdPrefixes = requireNonNull(includeGroupIdPrefixes, "includeGroupIdPrefixes");
    }
}
