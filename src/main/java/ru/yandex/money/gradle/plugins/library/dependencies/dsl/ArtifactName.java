package ru.yandex.money.gradle.plugins.library.dependencies.dsl;

import java.util.Objects;

/**
 * Имя артефакта. Состоит из полного имени библиотеки, описываемой {@link LibraryName}, и версии библиотеки
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 13.03.2017
 */
public final class ArtifactName {
    private final LibraryName libraryName;
    private final String version;

    /**
     * Конструктор класса
     *
     * @param group имя группы
     * @param name  имя бибилотеки
     * @param version версия библиотеки
     */
    public ArtifactName(String group, String name, String version) {
        this(new LibraryName(group, name), version);
    }

    /**
     * Конструктор класса
     *
     * @param libraryName полное имя бибилотеки
     * @param version версия библиотеки
     */
    public ArtifactName(LibraryName libraryName, String version) {
        this.libraryName = libraryName;
        this.version = version;
    }

    /**
     * Возвращает имя библиотеки
     *
     * @return имя библиотеки
     */
    public LibraryName getLibraryName() {
        return libraryName;
    }

    /**
     * Вовращает версию библиотеки
     *
     * @return версия библиотеки
     */
    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ArtifactName)) {
            return false;
        }

        ArtifactName other = (ArtifactName)object;

        return Objects.equals(libraryName, other.libraryName) && Objects.equals(version, other.version);
    }

    @Override
    public int hashCode() {
        return 7 * libraryName.hashCode() + version.hashCode();
    }
}