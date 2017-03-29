package ru.yandex.money.gradle.plugins.library.dependencies.reporters;

import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactDependency;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactName;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.LibraryName;

/**
 * Предназначен для получения имени зависимого артефакта в удобном читаемом формате после разрешения зависимостей.
 * <ul>
 *     <li>
 *         Если в результате резолва поменялась только версия артефакта,
 *         то имя артефакта представляется в формате: <i>group:name:oldVersion -> newVersion</i>
 *     </li>
 *     <li>
 *         Если в результате резолва поменялось имя артефакта,
 *         то имя артефакта представляется в формате: <i>oldGroup:oldName:oldVersion -> newGroup:newName:newVersion</i>
 *     </li>
 * </ul>
 *
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 25.03.2017
 */
class DependencyFormatter {
    private final ArtifactDependency dependency;

    private final LibraryName requestedLibraryName;
    private final String requestedVersion;

    private final LibraryName selectedLibraryName;
    private final String selectedVersion;

    /**
     * Возвращает форматированное имя артефакта после разрешения зависимостей
     *
     * @param dependency зависимый разрезолвленный артефакт
     * @return форматированное имя артефакта
     */
    static String format(ArtifactDependency dependency) {
        return new DependencyFormatter(dependency).format();
    }

    private DependencyFormatter(ArtifactDependency dependency) {
        this.dependency = dependency;

        this.requestedLibraryName = dependency.getRequestedLibraryName();
        this.requestedVersion = dependency.getRequestedVersion();

        this.selectedLibraryName = dependency.getSelectedLibraryName();
        this.selectedVersion = dependency.getSelectedVersion();
    }

    private String format() {
        if (hasSameLibraryNames()) {
            if (hasSameVersions()) {
                return new ArtifactName(requestedLibraryName, requestedVersion).toString();
            }

            return String.format("%s:%s -> %s", format(requestedLibraryName), requestedVersion, selectedVersion);
        }

        return String.format("%s -> %s", format(dependency.getRequestedArtifactName()),
                                         format(dependency.getSelectedArtifactName()));
    }

    private String format(LibraryName libraryName) {
        return NameFormatter.format(libraryName);
    }

    private String format(ArtifactName artifactName) {
        return NameFormatter.format(artifactName);
    }

    private boolean hasSameLibraryNames() {
        return requestedLibraryName.equals(selectedLibraryName);
    }

    private boolean hasSameVersions() {
        return requestedVersion.equals(selectedVersion);
    }
}
