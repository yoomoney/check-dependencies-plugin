package ru.yandex.money.gradle.plugins.library.dependencies.checkversion;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Класс, позволяющий настраивать проверку конфликта мажорных версий
 *
 * @author horyukova
 * @since 12.12.2018
 */
public class MajorVersionCheckerExtension {

    /**
     * Установка необходимости производить проверку конфликта версий библиотек
     */
    public boolean enabled = true;

    /**
     * Список префиксов groupId библиотек, для которых требуется выполнять проверку конфликта версий библиотек (например, ru.yamoney)
     */
    public Set<String> includeGroupIdPrefixes = new HashSet<>();

    /**
     * Список библиотек, для которых не требуется выполнять проверку конфликта версий библиотек
     */
    public Set<String> excludeDependencies = new HashSet<>();
}
