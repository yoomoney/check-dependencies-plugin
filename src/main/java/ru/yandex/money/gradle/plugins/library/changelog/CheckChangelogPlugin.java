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

    private static final String CHECK_CHANGELOG_TASK_NAME = "checkChangelog";
    private static final String JAVA_PLUGIN_ID = "java";

    @Override
    public void apply(Project project) {
        if (!project.getPluginManager().hasPlugin(JAVA_PLUGIN_ID)) {
            throw new GradleException("Java plugin must be applied before CheckChangelogPlugin");
        }

        Task checkChangelogTask = addCheckChangelogTask(project);
        project.getTasks().getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(checkChangelogTask);
    }

    private static Task addCheckChangelogTask(Project project) {
        CheckChangelogTask task = project.getTasks().create(CHECK_CHANGELOG_TASK_NAME, CheckChangelogTask.class);
        task.onlyIf(element -> {
            GitRepositoryProperties gitRepositoryProperties = GitRepositoryProperties.getInstance();
            return !gitRepositoryProperties.isMasterBranch() &&
                    !gitRepositoryProperties.isReleaseBranch() &&
                    !gitRepositoryProperties.isDevBranch();
        });
        task.setGroup("verification");
        task.setDescription("Check description for current release version in file " + CheckChangelogTask.CHANGELOG_FILE_NAME);
        return task;
    }
}
