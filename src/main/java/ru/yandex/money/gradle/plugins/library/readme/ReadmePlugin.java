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
    @Override
    public void apply(Project project) {
        project.getExtensions().create(ReadmePluginExtension.EXTENSION_NAME, ReadmePluginExtension.class, project);

        PublishReadmeTask task = project.getTasks().create(PublishReadmeTask.TASK_NAME, PublishReadmeTask.class);
        task.onlyIf(element -> GitRepositoryProperties.isMasterBranch());
        task.setGroup("publishing");
        task.setDescription("Publishes README.md to confluence");
    }
}
