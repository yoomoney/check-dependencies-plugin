package ru.yandex.money.gradle.plugins.library.dependencies.analysis;

import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactDependency;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactName;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.DependencyPath;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.LibraryName;

import java.util.List;

/**
 * Информация о конфликте:
 * <ul>
 *     <li>имя библиотеки библиотеки {@link LibraryName}</li>
 *     <li>первоначальная запрашиваемая версия</li>
 *     <li>конечная версии после разрешения конфликта</li>
 *     <li>список путей зависимостей, в результате которых произошел конфликт</li>
 * </ul>
 * , ,
 *
 * @author Brovin Yaroslav (brovin@yamoney.ru)
 * @since 30.01.2017
 */
public class ConflictedLibraryInfo {
    private final LibraryName library;
    private final String version;
    private final String fixedVersion;
    private List<DependencyPath<ArtifactDependency>> dependentPaths;

    ConflictedLibraryInfo(ArtifactName artifact, String fixedVersion,
                          List<DependencyPath<ArtifactDependency>> dependentPaths) {
        this(artifact.getLibraryName(), artifact.getVersion(), fixedVersion, dependentPaths);
    }

    ConflictedLibraryInfo(LibraryName library, String version, String fixedVersion,
                          List<DependencyPath<ArtifactDependency>> dependentPaths) {
        this.library = library;
        this.version = version;
        this.fixedVersion = fixedVersion;
        this.dependentPaths = dependentPaths;
    }

    public LibraryName getLibrary() {
        return library;
    }

    public String getVersion() {
        return version;
    }

    public String getFixedVersion() {
        return fixedVersion;
    }

    public List<DependencyPath<ArtifactDependency>> getDependentPaths() {
        return dependentPaths;
    }
}
