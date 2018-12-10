package ru.yandex.money.gradle.plugins.library.dependencies.showdependencies;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;

import org.gradle.api.Project;

import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Выводит версии внутренних зависимостей
 *
 * @author horyukova
 * @since 10.12.2018
 */
public class PrintInnerDependenciesVersionsTask extends DefaultTask {
    private final Logger log = LoggerFactory.getLogger(PrintInnerDependenciesVersionsTask.class);

    /**
     * Выводит новые версии внутренних библиотек
     */
    @TaskAction
    public void printInnerVersion() {
        log.warn("===============New inner dependencies===============");

        Action<Project> printDependenciesAction = new PrintDependenciesAction(DependencyType.INNER);
        getProject().allprojects(printDependenciesAction);

        log.warn("====================================================");
    }
}
