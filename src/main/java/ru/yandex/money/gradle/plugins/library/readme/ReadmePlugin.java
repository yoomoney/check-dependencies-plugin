package ru.yandex.money.gradle.plugins.library.readme;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import ru.yandex.money.gradle.plugins.library.helpers.GitRepositoryProperties;

/**
 * Плагин для работы с readme файлами.
 *
 * @author Kirill Bulatov (mail4score@gmail.com)
 * @since 20.12.2016
 */
public class ReadmePlugin implements Plugin<Project> {

    private GitRepositoryProperties gitRepositoryProperties;

    @Override
    public void apply(Project project) {
        gitRepositoryProperties = new GitRepositoryProperties(project.getProjectDir().getAbsolutePath());

        project.getExtensions().create(ReadmePluginExtension.EXTENSION_NAME, ReadmePluginExtension.class, project);

        PublishReadmeTask task = project.getTasks().create(PublishReadmeTask.TASK_NAME, PublishReadmeTask.class);
        task.onlyIf(element -> gitRepositoryProperties.isMasterBranch());
        task.setGroup("publishing");
        task.setDescription("Publishes README.md to confluence");
    }
}
