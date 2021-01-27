package ru.yoomoney.gradle.plugins.library.dependencies.showdependencies;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

/**
 * Выводит актуальные версии внешних зависимостей
 */
public class PrintAllActualDependenciesTask extends DefaultTask {

    /**
     * Выводит актуальные версии внешних библиотек
     */
    @TaskAction
    public void printActualOuterVersion() {
        getProject().getLogger().lifecycle("===============Actual dependencies===============");

        Action<Project> printDependenciesAction = new PrintActualDependenciesAction();
        getProject().allprojects(printDependenciesAction);

        getProject().getLogger().lifecycle("====================================================");
    }

}
