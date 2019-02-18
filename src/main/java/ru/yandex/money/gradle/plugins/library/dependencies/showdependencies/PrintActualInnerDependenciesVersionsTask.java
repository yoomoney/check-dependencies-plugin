package ru.yandex.money.gradle.plugins.library.dependencies.showdependencies;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

/**
 * Выводит актуальные версии внутренних зависимостей
 */
public class PrintActualInnerDependenciesVersionsTask extends DefaultTask {

    /**
     * Выводит актуальные версии внутренних библиотек
     */
    @TaskAction
    public void printActualInnerVersion() {
        getProject().getLogger().lifecycle("===============Actual inner dependencies===============");

        Action<Project> printDependenciesAction = new PrintActualDependenciesAction(DependencyType.INNER);
        getProject().allprojects(printDependenciesAction);

        getProject().getLogger().lifecycle("====================================================");
    }
}
