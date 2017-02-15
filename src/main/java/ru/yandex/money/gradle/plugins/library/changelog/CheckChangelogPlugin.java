package ru.yandex.money.gradle.plugins.library.changelog;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import ru.yandex.money.gradle.plugins.library.helpers.GitRepositoryProperties;

/**
 * Плагин для контроля обновления changelog файла
 *
 * @author Konstantin Rashev (rashev@yamoney.ru)
 * @since 09.01.2017
 */
public class CheckChangelogPlugin implements Plugin<Project> {

    public static final String CHECK_CHANGELOG_TASK_NAME = "checkChangelog";
    private static final String CHECK_CHANGELOG_TASK_GROUP = "verification";
    private static final String JAVA_PLUGIN_ID = "java";

    private GitRepositoryProperties gitRepositoryProperties;

    @Override
    public void apply(Project target) {
        if (!target.getPluginManager().hasPlugin(JAVA_PLUGIN_ID)) {
            throw new GradleException("Java plugin must be applied before CheckChangelogPlugin");
        }

        gitRepositoryProperties = new GitRepositoryProperties(target.getProjectDir().getAbsolutePath());

        Task checkChangelogTask = addCheckChangelogTask(target);
        target.getTasks().getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(checkChangelogTask);
    }

    private Task addCheckChangelogTask(Project project) {
        CheckChangelogTask task = project.getTasks().create(CHECK_CHANGELOG_TASK_NAME, CheckChangelogTask.class);
        task.onlyIf(element ->
                !gitRepositoryProperties.isMasterBranch() &&
                !gitRepositoryProperties.isReleaseBranch() &&
                !gitRepositoryProperties.isDevBranch());
        task.setGroup(CHECK_CHANGELOG_TASK_GROUP);
        task.setDescription(String.format("Check description for current release version in file %s", CheckChangelogTask.CHANGELOG_FILE_NAME));
        return task;
    }
}
