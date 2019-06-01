package ru.yandex.money.gradle.plugins.library.dependencies.dsl;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Полное имя библиотеки. Состоит из идентификатора группы и имени библиотеки
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 13.03.2017
 */
public final class LibraryName {
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
     * Возвращает идентификатор группы, к которой принадлежит библиотека
     *
     * @return идентификатор группы, к которой принадлежит библиотека
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
        if (!(object instanceof LibraryName)) {
            return false;
        }

        LibraryName other = (LibraryName) object;

        return Objects.equals(group, other.group) && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return 7 * group.hashCode() + name.hashCode();
    }

    @Override
    public String toString() {
        return group + ":" + name;
    }
}
