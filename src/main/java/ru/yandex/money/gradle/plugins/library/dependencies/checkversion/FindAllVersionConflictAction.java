package ru.yandex.money.gradle.plugins.library.dependencies.checkversion;

import org.gradle.api.Action;
import org.gradle.api.artifacts.DependencyResolveDetails;

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
    private final Set<String> excludedVersionConflictLibraries;

    private final Map<String, String> moduleVersions = new HashMap<>();
    private final Map<String, Set<String>> conflictModules;

    FindAllVersionConflictAction(Set<String> excludedVersionConflictLibraries, Map<String, Set<String>> conflictModules) {
        this.excludedVersionConflictLibraries = excludedVersionConflictLibraries;
        this.conflictModules = conflictModules;
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

        String moduleGroupAndName = groupDependency + ':' + nameDependency;
        String savedVersion = moduleVersions.get(moduleGroupAndName);
        if (savedVersion == null) {
            moduleVersions.put(moduleGroupAndName, requestedVersion);
        } else {
            boolean isVersionConflict = !Objects.equals(getMajorVer(requestedVersion), getMajorVer(savedVersion));

            if (isVersionConflict && isNeedCheck(moduleGroupAndName)) {
                Set<String> versionSet = conflictModules.getOrDefault(moduleGroupAndName, new HashSet<>());

                versionSet.add(requestedVersion);
                versionSet.add(savedVersion);

                conflictModules.put(moduleGroupAndName, versionSet);
            }
        }

    }

    private static String getMajorVer(String ver) {
        return ver.split("\\.")[0];
    }

    private boolean isNeedCheck(String moduleGroupAndName) {
        return !excludedVersionConflictLibraries.contains(moduleGroupAndName)
                && (moduleGroupAndName.contains("ru.yandex.money") || moduleGroupAndName.contains("ru.yamoney"));
    }
}
