package ru.yandex.money.gradle.plugins.library.dependencies.dsl;

import java.util.*;

/**
 * Описывает набор имен артефактов
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 13.03.2017
 */
public class ArtifactNameSet implements Iterable<ArtifactName> {
    private final Map<LibraryName, Set<String>> libraryVersions;

    public static ArtifactNameSet fromLibraryVersions(LibraryName library, Set<String> versions) {
        Map<LibraryName, Set<String>> libraryVersions = new HashMap<>();
        libraryVersions.put(library, versions);
        return ArtifactNameSet.fromLibraryVersions(libraryVersions);
    }

    /**
     * Создает набор имен артефактов на основании переданного отображения из имени библиотеки в набор версий
     *
     * @param libraryVersions отображение из имени библиотеки в набор версий библиотеки
     * @return набор имен артефактов
     */
    public static ArtifactNameSet fromLibraryVersions(Map<LibraryName, Set<String>> libraryVersions) {
        return new ArtifactNameSet(libraryVersions);
    }

    private ArtifactNameSet(Map<LibraryName, Set<String>> libraryVersions) {
        this.libraryVersions = libraryVersions;
    }

    /**
     * Проверяет, содержатся ли имена артефактов с указанным именем библиотеки
     *
     * @param libraryName имя библиотеки
     * @return true, если содержатся, и false - в противном случае
     */
    public boolean contains(LibraryName libraryName) {
        return libraryVersions.containsKey(libraryName);
    }

    /**
     * Возвращает набор всех имен библиотек, для каждого из которых содержится как минимум один артефакт
     *
     * @return набор имен библиотек
     *
     * */
    public Set<LibraryName> getLibraryNames() {
        return libraryVersions.keySet();
    }

    /**
     * Возврщает набор версий артефактов с указанным именем библиотеки
     *
     * @return набор версий артефактов
     */
    public Set<String> getVersions(LibraryName libraryName) {
        return Optional.ofNullable(libraryVersions.get(libraryName)).orElse(Collections.emptySet()) ;
    }

    @Override
    public Iterator<ArtifactName> iterator() {
        return libraryVersions.entrySet()
                              .stream()
                              .flatMap(entry -> entry.getValue()
                                                     .stream()
                                                     .map(version -> new ArtifactName(entry.getKey(), version)))
                              .iterator();
    }
}
