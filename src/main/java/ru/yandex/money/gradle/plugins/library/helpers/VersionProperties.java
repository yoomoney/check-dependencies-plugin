package ru.yandex.money.gradle.plugins.library.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Утилиты для получения версии библиотеки из свойств.
 *
 * @author Kirill Bulatov (mail4score@gmail.com)
 * @since 23.12.2016
 */
public class VersionProperties {
    private static final String VERSION_PROPERTY_KEY = "version";

    @SuppressWarnings("unused")
    public VersionProperties() {}

    /**
     * Читает версию библиотеки из указанного файла.
     *
     * @param propertiesFile путь до файла с версией
     * @return значение property с ключом version
     * @throws IllegalStateException в случае проблем с открытием/чтением файла или когда version оказывается пустой
     */
    public static String loadPluginVersion(File propertiesFile) {
        Properties gradleProperties = new Properties();
        try {
            gradleProperties.load(new FileInputStream(propertiesFile));
            String version = (String) gradleProperties.get(VERSION_PROPERTY_KEY);
            if (version == null || version.length() == 0) {
                throw new IllegalStateException("Version is either null or empty, version=" + version);
            }
            return version;
        } catch (IOException e) {
            throw new IllegalStateException("Could not load file=" + propertiesFile, e);
        }
    }
}
