package ru.yandex.money.plugins.library;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import ru.yandex.money.plugins.library.readme.PublishReadmePlugin;

import java.util.Arrays;
import java.util.Collection;

/**
 * Входная точка library-plugin'а, подключает все необходимые плагины-зависимости.
 *
 * @author Kirill Bulatov (mail4score@gmail.com)
 * @since 22.12.2016
 */
public class LibraryPlugin implements Plugin<Project> {

    /**
     * Для подключения новой функциональности, достаточно добавить плагин в этот список.
     * Все остальные настройки должны делаться в самом добавляемом плагине.
     */
    private static final Collection<Plugin<Project>> PLUGINS_TO_APPLY = Arrays.asList(
            new PublishReadmePlugin()
    );

    @Override
    public void apply(Project project) {
        PLUGINS_TO_APPLY.forEach(plugin -> plugin.apply(project));
        configureRepositories(project);
    }

    private void configureRepositories(Project project) {
        RepositoryHandler repositories = project.getRepositories();
        repositories.add(repositories.maven(mavenArtifactRepository ->
                mavenArtifactRepository.setUrl("http://nexus.yamoney.ru/content/repositories/central/")));
        repositories.add(repositories.maven(mavenArtifactRepository ->
                mavenArtifactRepository.setUrl("http://nexus.yamoney.ru/content/repositories/releases/")));
        repositories.add(repositories.maven(mavenArtifactRepository ->
                mavenArtifactRepository.setUrl("http://nexus.yamoney.ru/content/repositories/snapshots/")));
        repositories.add(repositories.maven(mavenArtifactRepository ->
                mavenArtifactRepository.setUrl("http://nexus.yamoney.ru/content/repositories/thirdparty/")));
        repositories.add(repositories.maven(mavenArtifactRepository ->
                mavenArtifactRepository.setUrl("http://nexus.yamoney.ru/content/repositories/spp-snapshots/")));
        repositories.add(repositories.maven(mavenArtifactRepository ->
                mavenArtifactRepository.setUrl("http://nexus.yamoney.ru/content/repositories/spp-releases/")));
    }
}
