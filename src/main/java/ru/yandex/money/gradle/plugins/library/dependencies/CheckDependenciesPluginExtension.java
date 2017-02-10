package ru.yandex.money.gradle.plugins.library.dependencies;

import java.util.Collections;
import java.util.List;

/**
 * Класс, позволяющий настраивать CheckDependenciesPlugin.
 *
 * @author Brovin Yaroslav (brovin@yamoney.ru)
 * @since 05.02.2017
 */
@SuppressWarnings("WeakerAccess")
public class CheckDependenciesPluginExtension {
    /**
     * Источники файлов правил исключений, допускающих изменение версий библиотек.
     * <p>
     * Возможны следующие значения:
     * <ul>
     *     <li>Путь к файлу настроек в локальной файловой системе</li>
     *     <li>Название артефакта с настройками: <b>"Название группы" : "Название артефакта"</b>. Будет искаться файл с именем
     *     <b>"libraries_versions_exclusions.properties"</b></li>
     * </ul>
     */
    public List<String> exclusionsRulesSources = Collections.singletonList("libraries_versions_exclusions.properties");


}
