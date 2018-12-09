package ru.yandex.money.gradle.plugins.library.dependencies.checkversion;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.DependencyResolveDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * Выполняет проверку по всем найденным конфликтам мажорных версий
 *
 * @author horyukova
 * @since 09.12.2018
 */
class CheckVersionAction implements Action<DependencyResolveDetails> {
    private static final Logger log = LoggerFactory.getLogger(CheckVersionAction.class);

    private final Project project;

    private final Map<String, Set<String>> conflictModules;

    CheckVersionAction(Project project, Map<String, Set<String>> conflictModules) {
        this.project = project;
        this.conflictModules = conflictModules;

    }

    @Override
    public void execute(DependencyResolveDetails dependency) {

        String moduleGroupAndName = dependency.getRequested().getGroup() + ':' + dependency.getRequested().getName();

        if (conflictModules.containsKey(moduleGroupAndName)) {
            String errorMsg = "There is major vesion conflict for dependepcy=" + moduleGroupAndName + ", versions=" +
                    conflictModules.get(moduleGroupAndName);

            if (project.getGradle().getStartParameter().getTaskNames().get(0).endsWith("dependencies")) {
                log.error(errorMsg);
            } else {
                throw new GradleException(errorMsg);
            }
        }
    }
}