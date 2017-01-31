package ru.yandex.money.gradle.plugins.library.dependencies;

/**
 * Информация о конфликте: название библиотеки (Группа + имя артефакта), первоначальная запрашиваемая версия и конечная версии
 * после разрешения конфликта
 *
 * @author Brovin Yaroslav (brovin@yamoney.ru)
 * @since 30.01.2017
 */
class ConflictedLibraryInfo {

    private final String library;
    private final String version;
    private final String fixedVersion;

    ConflictedLibraryInfo(String library, String version, String fixedVersion) {
        this.library = library;
        this.version = version;
        this.fixedVersion = fixedVersion;
    }

    String getLibrary() {
        return library;
    }

    String getVersion() {
        return version;
    }

    String getFixedVersion() {
        return fixedVersion;
    }
}
