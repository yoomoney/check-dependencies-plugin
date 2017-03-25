package ru.yandex.money.gradle.plugins.library.dependencies.reporters;

import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactDependency;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactName;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.LibraryName;

/**
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 25.03.2017
 */
class DependencyFormatter {
    private final ArtifactDependency dependency;

    private final LibraryName requestedLibraryName;
    private final String requestedVersion;

    private final LibraryName selectedLibraryName;
    private final String selectedVersion;

    public static String format(ArtifactDependency dependency) {
        return new DependencyFormatter(dependency).format();
    }

    private DependencyFormatter(ArtifactDependency dependency) {
        this.dependency = dependency;

        this.requestedLibraryName = dependency.getRequestedLibraryName();
        this.requestedVersion = dependency.getRequestedVersion();

        this.selectedLibraryName = dependency.getSelectedLibraryName();
        this.selectedVersion = dependency.getSelectedVersion();
    }

    public String format() {
        if (hasSameLibraryNames()) {
            if (hasSameVersions()) {
                return new ArtifactName(requestedLibraryName, requestedVersion).toString();
            }

            return String.format("%s:%s -> %s", requestedLibraryName, requestedVersion, selectedVersion);
        }

        return String.format("%s -> %s", dependency.getRequestedArtifactName(),
                dependency.getSelectedArtifactName());
    }

    private boolean hasSameLibraryNames() {
        return requestedLibraryName.equals(selectedLibraryName);
    }

    private boolean hasSameVersions() {
        return requestedVersion.equals(selectedVersion);
    }
}
