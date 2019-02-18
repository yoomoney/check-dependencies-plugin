package ru.yandex.money.gradle.plugins.library.dependencies.showdependencies;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

/**
 * Выводит версии внутренних зависимостей
 *
 * @author horyukova
 * @since 10.12.2018
 */
public class PrintInnerDependenciesVersionsTask extends DefaultTask {

    /**
     * Выводит новые версии внутренних библиотек
     */
    @TaskAction
    public void printInnerVersion() {
        getProject().getLogger().lifecycle("===============New inner dependencies===============");

        Action<Project> printDependenciesAction = new PrintDependenciesAction(DependencyType.INNER);
        getProject().allprojects(printDependenciesAction);

        getProject().getLogger().lifecycle("====================================================");
    }
}
