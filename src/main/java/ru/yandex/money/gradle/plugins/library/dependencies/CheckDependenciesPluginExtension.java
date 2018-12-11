package ru.yandex.money.gradle.plugins.library.dependencies;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * <li>Путь к файлу настроек в локальной файловой системе</li>
     * <li>Название артефакта с настройками: <b>"Название группы" : "Название артефакта"</b>. Будет искаться файл с именем
     * <b>"libraries-versions-exclusions.properties"</b></li>
     * </ul>
     */
    public List<String> exclusionsRulesSources = Collections.singletonList("libraries-versions-exclusions.properties");

    /**
     * Список конфигурация для которых не требуется выполнять проверку версий библиотек.
     */
    public List<String> excludedConfigurations = new ArrayList<>();

    /**
     * Зарегистрированные селекторы версий для библиотек, для которых требуется разрешить конфликты.
     * Разрешение конфликтов происходит путем анализа других версий данной библиоки с целью найти версию, не приводящую к конфликтам.
     * Селектор версий представляет собой функцию, принимающую на вход версию библиотеки в виде строки,
     * и возвращающую true, если данную версию библиотеки нужно проанализировать на предмет возникновения конфликтов,
     * и возвращающую false, если данную версию бибилиотеки нужно проигнорировать
     *
     * Представляет собой отображение имени библиотеки в формате group:name на селектор версий.
     */
    public Map<String, Closure<Boolean>> versionSelectors = Collections.emptyMap();

    /**
     * Установка необходимости производить проверку конфликта версий библиотек
     */
    public boolean enableMajorVersionCheck = true;

    /**
     * Список библиотек, для которых не требуется выполнять проверку конфликта версий библиотек
     */
    public Set<String> excludedMajorVersionCheckLibraries = new HashSet<>();
}
