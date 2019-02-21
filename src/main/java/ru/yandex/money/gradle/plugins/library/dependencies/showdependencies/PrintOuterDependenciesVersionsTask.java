package ru.yandex.money.gradle.plugins.library.dependencies.showdependencies;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

/**
 * Выводит версии внешних зависимостей
 *
 * @author horyukova
 * @since 10.12.2018
 */
public class PrintOuterDependenciesVersionsTask extends DefaultTask {

    /**
     * Выводит новые версии внешних библиотек
     */
    @TaskAction
    public void printOuterVersion() {
        getProject().getLogger().lifecycle("===============New outer dependencies===============");

        Action<Project> printDependenciesAction = new PrintDependenciesAction(DependencyType.OUTER);
        getProject().allprojects(printDependenciesAction);

        getProject().getLogger().lifecycle("====================================================");
    }

}
