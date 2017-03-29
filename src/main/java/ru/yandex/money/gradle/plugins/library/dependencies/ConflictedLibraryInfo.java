package ru.yandex.money.gradle.plugins.library.dependencies;

/**
 * Информация о конфликте: название библиотеки (Группа + имя артефакта), первоначальная запрашиваемая версия и конечная версии
 * после разрешения конфликта
 *
 * @author Brovin Yaroslav (brovin@yamoney.ru)
 * @since 30.01.2017
 */
public class ConflictedLibraryInfo {

    private final String library;
    private final String version;
    private final String fixedVersion;

    /**
     * Конструктор класс
     *
     * @param library имя библиотеки
     * @param version первоначально запрашиваемая версия библиотеки
     * @param fixedVersion конечная версия библиотеки
     */
    ConflictedLibraryInfo(String library, String version, String fixedVersion) {
        this.library = library;
        this.version = version;
        this.fixedVersion = fixedVersion;
    }

    public String getLibrary() {
        return library;
    }

    public String getVersion() {
        return version;
    }

    public String getFixedVersion() {
        return fixedVersion;
    }
}
