package ru.yoomoney.gradle.plugins.library.dependencies.analysis.conflicts;

import ru.yoomoney.gradle.plugins.library.dependencies.analysis.conflicts.resolvers.ConflictPathResolutionResult;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.ArtifactDependency;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.ArtifactName;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.DependencyPath;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.LibraryName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * @author Brovin Yaroslav
 * @since 30.01.2017
 */
public class ConflictedLibraryInfo {
    private final LibraryName library;
    private final String version;
    private final String fixedVersion;
    private final List<DependencyPath<ArtifactDependency>> conflictDependentPaths;
    private final Map<ArtifactName, ConflictPathResolutionResult> conflictPathResolutions = new HashMap<>();

    /**
     * Конструктор класса
     *
     * @param artifact имя первоначально запрашиваемого артефакта
     * @param fixedVersion конечная версия
     * @param dependentPaths пути зависимостей, в результате которых произошел конфликт версий
     */
    ConflictedLibraryInfo(ArtifactName artifact, String fixedVersion,
                          List<DependencyPath<ArtifactDependency>> dependentPaths) {
        this(artifact.getLibraryName(), artifact.getVersion(), fixedVersion, dependentPaths);
    }

    /**
     * Конструктор класса
     *
     * @param library имя библиотеки
     * @param version имя первоначально запрашиваемой версии
     * @param fixedVersion конечная версия библиотеки
     * @param dependentPaths пути зависимостей, в результате которых произошел конфликт версий
     */
    ConflictedLibraryInfo(LibraryName library, String version, String fixedVersion,
                          List<DependencyPath<ArtifactDependency>> conflictDependentPaths) {
        this.library = library;
        this.version = version;
        this.fixedVersion = fixedVersion;
        this.conflictDependentPaths = conflictDependentPaths;
    }

    public void addConflictPathResolution(ConflictPathResolutionResult resolutionResult) {
        conflictPathResolutions.put(resolutionResult.getDirectDependency(), resolutionResult);
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

    public List<DependencyPath<ArtifactDependency>> getConflictDependentPaths() {
        return conflictDependentPaths;
    }

    public ConflictPathResolutionResult getResolutionFor(DependencyPath<ArtifactDependency> conflictPath) {
        return conflictPathResolutions.get(conflictPath.getRoot().getRequestedArtifactName());
    }
}
