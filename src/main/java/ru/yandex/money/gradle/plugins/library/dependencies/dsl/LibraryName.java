package ru.yandex.money.gradle.plugins.library.dependencies.dsl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Полное имя библиотеки. Состоит из идентификатора группы и имени библиотеки
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 13.03.2017
 */
public final class LibraryName implements Comparable<LibraryName> {
    private static final Pattern LIBRARY_NAME_PATTERN = Pattern.compile("(?<group>.+):(?<name>.+)");

    private final String group;
    private final String name;

    /**
     * Преобразует переданную строку в объект, представляющий полное имя библиотеки
     *
     * @param libraryName строка, содержащая полное имя библиотеки в формате 'идентификатор группы':'имя библиотеки'
     */
    public static LibraryName parse(String libraryName) {
        Matcher matcher = LIBRARY_NAME_PATTERN.matcher(libraryName);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Passed library name has incorrect format: expected '<group>:<name>'");
        }
        return new LibraryName(matcher.group("group"), matcher.group("name"));
    }

    public LibraryName(String group, String name) {
        this.group = group;
        this.name = name;
    }

    /**
     * Возвращает идентификтор группы, к которой принадлежит библиотека
     *
     * @return
     */
    public String getGroup() {
        return group;
    }

    /**
     * Возвращает имя библиотеки в группе
     *
     * @return имя библиотеки
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if ((object == null) || !(object instanceof LibraryName)) return false;

        LibraryName other = (LibraryName) object;

        return other.group.equals(group) && other.name.equals(name);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s:%s", group, name);
    }

    @Override
    public int compareTo(LibraryName other) {
        return toString().compareTo(other.toString());
    }
}
