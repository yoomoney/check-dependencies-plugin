package ru.yandex.money.gradle.plugins.library.dependencies;

import org.gradle.api.Project;

/**
 * Класс, позволяющий настраивать CheckDependenciesPlugin.
 *
 * @author Brovin Yaroslav (brovin@yamoney.ru)
 * @since 05.02.2017
 */
public class CheckDependenciesPluginExtension {
    /** Имя extension'а, под которым он регистрируется в проекте. */
    static final String EXTENSION_NAME = "checkDependencies";

    /** Название файла с правилами допускающими изменение версий библиотек. */
    public String fileName;

    public CheckDependenciesPluginExtension(Project project) {
        fileName = "library_versions_exclusions.properties";
    }
}
