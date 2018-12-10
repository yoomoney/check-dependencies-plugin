package ru.yandex.money.gradle.plugins.library.dependencies.showdependencies;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;

import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Выводит версии внешних зависимостей
 *
 * @author horyukova
 * @since 10.12.2018
 */
public class PrintOuterDependenciesVersionsTask extends DefaultTask {
    private final Logger log = LoggerFactory.getLogger(PrintOuterDependenciesVersionsTask.class);

    /**
     * Выводит новые версии внешних библиотек
     */
    @TaskAction
    public void printOuterVersion() {
        log.warn("===============New outer dependencies===============");

        Action<Project> printDependenciesAction = new PrintDependenciesAction(DependencyType.OUTER);
        getProject().allprojects(printDependenciesAction);

        log.warn("====================================================");
    }

}
