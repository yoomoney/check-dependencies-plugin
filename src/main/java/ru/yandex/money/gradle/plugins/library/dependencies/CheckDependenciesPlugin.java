package ru.yandex.money.gradle.plugins.library.dependencies;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginAware;

import javax.annotation.Nonnull;

/**
 * Плагин для контроля версий используемых библиотек. Проект может использовать библиотеку, которая жестко фиксирует
 * версии, используемых внутри, библиотек (Например, так делает Spring-Boot). В случае, если проект использует
 * такую же библиотеку, но другой версии, может возникнуть конфликт версий, приводящий к ошибкам в runtime.
 * <p>
 * Этот плагин проверяет все зависимости проекта:
 * <ol>
 * <li>Если изменение версии библиотеки связано с жесткой фиксации версии, плагин остановит билд с ошибкой.</li>
 * <li>Если изменение версии библиотеки не связано с жесткой фиксацией версии, то билд допускается к выполнению.</li>
 * </ol>
 *
 * @author Brovin Yaroslav (brovin@yamoney.ru)
 * @since 09.01.2017
 */
public class CheckDependenciesPlugin implements Plugin<Project> {

    public static final String CHECK_DEPENDENCIES_TASK_NAME = "checkLibraryDependencies";
    private static final String CHECK_DEPENDENCIES_TASK_GROUP = "verification";
    private static final String CHECK_DEPENDENCIES_TASK_DESCRIPTION = "Checks current used libraries versions on " +
                                                                      "conflict with platform libraries version.";
    private static final String SPRING_DEPENDENCY_MANAGEMENT_PLUGIN_ID = "io.spring.dependency-management";
    private static final String ERROR_APPLYING_PLUGIN_REQUIRED = "\"%s\" plugin is required for correct working of check " +
                                                                  "dependencies plugin.\n Apply this plugin in build script.";

    @Override
    public void apply(Project target) {
        checkApplyingExternalPlugin(target, SPRING_DEPENDENCY_MANAGEMENT_PLUGIN_ID);

        Task task = createCheckDependenciesTask(target);
        target.getTasks().getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(task);
    }

    /**
     * Проверяет, что плагин подключен к проекту.
     *
     * @param project проект
     * @param pluginID идентификатор плагина
     * @throws GradleException указанный плагин не подключен
     */
    private static void checkApplyingExternalPlugin(@Nonnull PluginAware project, @Nonnull String pluginID) {
        if (!project.getPluginManager().hasPlugin(pluginID)) {
            throw new GradleException(String.format(ERROR_APPLYING_PLUGIN_REQUIRED, pluginID));
        }
    }

    /**
     * Создае задачу проверки версий библиотек
     *
     * @param project проект
     * @return задача проверки зависимостей
     */
    private static Task createCheckDependenciesTask(@Nonnull Project project) {
        CheckDependenciesTask task = project.getTasks().create(CHECK_DEPENDENCIES_TASK_NAME, CheckDependenciesTask.class);
        task.setGroup(CHECK_DEPENDENCIES_TASK_GROUP);
        task.setDescription(CHECK_DEPENDENCIES_TASK_DESCRIPTION);

        return task;
    }
}