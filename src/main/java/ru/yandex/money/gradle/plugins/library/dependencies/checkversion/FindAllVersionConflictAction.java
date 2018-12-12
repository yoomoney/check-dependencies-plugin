package ru.yandex.money.gradle.plugins.library.dependencies.checkversion;

import org.gradle.api.Action;
import org.gradle.api.artifacts.DependencyResolveDetails;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.LibraryName;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static ru.yandex.money.gradle.plugins.library.dependencies.NexusUtils.getArtifactLatestVersion;

/**
 * Используется для прохода по всем зависимостям для поиска конфликта мажорных версий
 *
 * @author horyukova
 * @since 09.12.2018
 */
class FindAllVersionConflictAction implements Action<DependencyResolveDetails> {
    private final Set<LibraryName> excludedLibraries;
    private final Set<String> includePrefixLibraries;

    private final Map<LibraryName, String> majorModuleVersions = new HashMap<>();
    private final Map<LibraryName, Set<String>> conflictModules;

    FindAllVersionConflictAction(Set<LibraryName> excludedLibraries,
                                 Set<String> includePrefixLibraries,
                                 Map<LibraryName, Set<String>> conflictModules) {
        this.excludedLibraries = excludedLibraries;
        this.conflictModules = conflictModules;
        this.includePrefixLibraries = includePrefixLibraries;
    }

    @Override
    public void execute(DependencyResolveDetails dependency) {
        String requestedVersion = dependency.getRequested().getVersion();
        if (requestedVersion == null) {
            return;
        }
        String groupDependency = dependency.getRequested().getGroup();
        String nameDependency = dependency.getRequested().getName();

        if ("+".equals(requestedVersion) || "latest.release".equals(requestedVersion)) {
            requestedVersion = getArtifactLatestVersion(groupDependency, nameDependency);
        }

        LibraryName libraryName = new LibraryName(groupDependency, nameDependency);
        String savedVersion = majorModuleVersions.get(libraryName);
        String majorRequestedVersion = getMajorVer(requestedVersion);

        if (savedVersion == null) {
            majorModuleVersions.put(libraryName, majorRequestedVersion);
        } else {

            if (isNeedCheck(libraryName) && !Objects.equals(majorRequestedVersion, savedVersion)) {

                Set<String> versionSet = conflictModules.getOrDefault(libraryName, new HashSet<>());

                versionSet.add(requestedVersion);
                versionSet.add(savedVersion);

                conflictModules.put(libraryName, versionSet);
            }
        }

    }

    private static String getMajorVer(String ver) {
        return ver.split("\\.")[0];
    }

    private boolean isNeedCheck(LibraryName libraryName) {
        boolean isLibraryInExcluded = excludedLibraries.contains(libraryName);

        return !isLibraryInExcluded
                && (includePrefixLibraries.isEmpty() ||
                includePrefixLibraries.stream().anyMatch(prefix -> libraryName.getGroup().startsWith(prefix)));
    }
}
