package ru.yandex.money.gradle.plugins.library.dependencies;

/**
 * Класс, позволяющий настраивать CheckDependenciesPlugin.
 *
 * @author Brovin Yaroslav (brovin@yamoney.ru)
 * @since 05.02.2017
 */
@SuppressWarnings("WeakerAccess")
public class CheckDependenciesPluginExtension {
    /** Название файла с правилами допускающими изменение версий библиотек. */
    public String exclusionsFileName  = "library_versions_exclusions.properties";
}
