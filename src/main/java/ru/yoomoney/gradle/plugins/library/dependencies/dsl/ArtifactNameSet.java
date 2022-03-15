package ru.yoomoney.gradle.plugins.library.dependencies.dsl;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Описывает набор имен артефактов
 *
 * @author Konstantin Novokreshchenov
 * @since 13.03.2017
 */
public class ArtifactNameSet implements Iterable<ArtifactName> {
    private final Map<LibraryName, Set<String>> libraryVersions;

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
     * Возврщает набор версий артефактов с указанным именем библиотеки
     *
     * @return набор версий артефактов
     */
    public Set<String> getVersions(LibraryName libraryName) {
        return Optional.ofNullable(libraryVersions.get(libraryName)).orElseGet(Collections::emptySet);
    }

    /**
     * Проверяет, есть ли фиксированная версия для библиотеки
     *
     * @return true, если есть
     */
    public boolean contains(@Nonnull LibraryName libraryName) {
        requireNonNull(libraryName, "libraryName");

        return libraryVersions != null
                && libraryName.getGroup() != null
                && libraryName.getName() != null
                && libraryVersions.containsKey(libraryName);
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
