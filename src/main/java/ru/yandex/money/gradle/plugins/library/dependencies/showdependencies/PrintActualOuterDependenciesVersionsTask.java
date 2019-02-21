package ru.yandex.money.gradle.plugins.library.dependencies.showdependencies;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

/**
 * Выводит актуальные версии внешних зависимостей
 */
public class PrintActualOuterDependenciesVersionsTask extends DefaultTask {

    /**
     * Выводит актуальные версии внешних библиотек
     */
    @TaskAction
    public void printActualOuterVersion() {
        getProject().getLogger().lifecycle("===============Actual outer dependencies===============");

        Action<Project> printDependenciesAction = new PrintActualDependenciesAction(DependencyType.OUTER);
        getProject().allprojects(printDependenciesAction);

        getProject().getLogger().lifecycle("====================================================");
    }

}
