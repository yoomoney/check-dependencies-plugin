package ru.yandex.money.plugins.library.readme;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import ru.yandex.money.plugins.library.GitUtils;

public class PublishReadmePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getExtensions().create(PublishReadmeTask.TASK_NAME, ReadmePluginConfiguration.class, project);

        PublishReadmeTask task = project.getTasks().create(PublishReadmeTask.TASK_NAME, PublishReadmeTask.class);
        task.onlyIf(element -> GitUtils.isMasterBranch());
        task.setGroup("publishing");
        task.setDescription("Publishes README.md to confluence");
    }
}
