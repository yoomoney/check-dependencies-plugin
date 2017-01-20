package ru.yandex.money.gradle.plugins.library;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import ru.yandex.money.gradle.plugins.library.changelog.CheckChangelogPlugin;
import ru.yandex.money.gradle.plugins.library.readme.ReadmePlugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Входная точка library-plugin'а, подключает все необходимые плагины-зависимости.
 *
 * @author Kirill Bulatov (mail4score@gmail.com)
 * @since 22.12.2016
 */
public class LibraryProjectPlugin implements Plugin<Project> {

    /**
     * Для подключения новой функциональности, достаточно добавить плагин в этот список.
     * Все остальные настройки должны делаться в самом добавляемом плагине.
     */
    private static final Collection<Class<?>> PLUGINS_TO_APPLY = Arrays.asList(
            ReadmePlugin.class,
            CheckChangelogPlugin.class
    );

    @Override
    public void apply(Project project) {
        PLUGINS_TO_APPLY.forEach(pluginClass -> project.getPluginManager().apply(pluginClass));
        configureRepositories(project);
    }

    /**
     * Добавляем в проект все репозитории, нужные для получения зависимостей.
     */
    private void configureRepositories(Project project) {
        RepositoryHandler repositories = project.getRepositories();
        Stream.of(
                "http://nexus.yamoney.ru/content/repositories/central/",
                "http://nexus.yamoney.ru/content/repositories/releases/",
                "http://nexus.yamoney.ru/content/repositories/snapshots/",
                "http://nexus.yamoney.ru/content/repositories/thirdparty/",
                "http://nexus.yamoney.ru/content/repositories/spp-snapshots/",
                "http://nexus.yamoney.ru/content/repositories/spp-releases/"
        )
                .map(repoUrl -> repositories.maven(mavenArtifactRepository -> mavenArtifactRepository.setUrl(repoUrl)))
                .forEach(repositories::add);
    }
}
