package ru.yandex.money.gradle.plugins.library.dependencies.dsl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Описывает артефакт с указанием диапазона версий
 *
 * @author horyukova
 * @since 31.05.2019
 */
public class ArtifactWithVersionRange {
    /**
     * Полное имя библиотеки
     */
    private final LibraryName libraryName;
    /**
     * Начальная версия диапазона
     */
    private final String startVersion;
    /**
     * Последняя версия диапазона
     */
    private final String endVersion;

    /**
     * Конструктор с заданием начальной и последней версией
     *
     * @param libraryName  полное имя бибилотеки
     * @param startVersion начальная версия диапазона, null для полуинтервала
     * @param endVersion   последняя версия диапазона, null для полуинтервала
     */
    public ArtifactWithVersionRange(@Nonnull LibraryName libraryName,
                                    @Nullable String startVersion,
                                    @Nullable String endVersion) {
        if (startVersion == null && endVersion == null) {
            throw new RuntimeException(String.format("StartVersion and endVersion are null for library: %s",
                    libraryName));
        }
        this.libraryName = libraryName;
        this.startVersion = startVersion;
        this.endVersion = endVersion;
    }

    /**
     * Конструктор для случая совпадения начальной и последней версии
     *
     * @param libraryName полное имя бибилотеки
     * @param version     версия библиотеки
     */
    public ArtifactWithVersionRange(LibraryName libraryName, @Nonnull String version) {
        this.libraryName = libraryName;
        this.startVersion = version;
        this.endVersion = version;
    }

    /**
     * Определяет, входит ли переденная версия в диапозон версий артефакта
     *
     * @param version версия, вхождение которой необходимо проверить
     * @return true, если версия входит в диапазон
     */
    public boolean isVersionIncludedInRange(String version) {
        if (startVersion == null) {
            return compareVersions(endVersion, version) >= 0;
        }
        if (endVersion == null) {
            return compareVersions(startVersion, version) <= 0;
        }
        return compareVersions(startVersion, version) <= 0
                && compareVersions(endVersion, version) >= 0;
    }

    public LibraryName getLibraryName() {
        return libraryName;
    }

    private int compareVersions(String version1, String version2) {
        String[] versionAsArray1 = version1.split("\\.");
        String[] versionAsArray2 = version2.split("\\.");

        for (int i = 0; i < Math.min(versionAsArray1.length, versionAsArray2.length); i++) {
            Integer num1 = Integer.valueOf(versionAsArray1[i]);
            Integer num2 = Integer.valueOf(versionAsArray2[i]);

            if (!num1.equals(num2)) {
                return num1.compareTo(num2);
            }
        }
        return 0;
    }
}
