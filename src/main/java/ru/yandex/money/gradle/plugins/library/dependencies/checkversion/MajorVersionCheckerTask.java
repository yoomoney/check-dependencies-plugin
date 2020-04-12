package ru.yandex.money.gradle.plugins.library.dependencies.checkversion;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.TaskAction;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.LibraryName;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static ru.yandex.money.gradle.plugins.library.dependencies.NexusUtils.getArtifactLatestVersion;

/**
 * Задача для проверки конфликтов мажорных версий подключенных библиотек
 *
 * @author horyukova
 * @since 15.03.2020
 */
public class MajorVersionCheckerTask extends DefaultTask {
    private final Map<LibraryName, String> majorModuleVersions = new HashMap<>();

    private Set<LibraryName> excludedLibraries;
    private Set<String> includePrefixLibraries;

    public void setExcludedLibraries(Set<LibraryName> excludedLibraries) {
        this.excludedLibraries = excludedLibraries;
    }

    public void setIncludePrefixLibraries(Set<String> includePrefixLibraries) {
        this.includePrefixLibraries = includePrefixLibraries;
    }

    /**
     * Проверяем наличие конфликтов мажорных версий
     */
    @TaskAction
    public void majorVersionCheck() {
        ConfigurationContainer allConfigurations = getProject().getConfigurations();
        Map<LibraryName, Set<String>> conflictModules = new HashMap<>();

        allConfigurations.stream()
                .filter(MajorVersionCheckerTask::isValidConfiguration)
                .forEach(conf -> conf.getDependencies()
                        .forEach(dep -> conflictModules.putAll(findAllVersionConflict(dep))));

        checkVersion(conflictModules);
    }

    private static boolean isValidConfiguration(Configuration configuration) {
        String configurationLowerName = configuration.getName().toLowerCase();

        return configurationLowerName.endsWith("compile")
                || configurationLowerName.endsWith("runtime")
                || configurationLowerName.endsWith("implementation")
                || Objects.equals(configurationLowerName, "compileclasspath");
    }

    private Map<LibraryName, Set<String>> findAllVersionConflict(Dependency dependency) {
        Map<LibraryName, Set<String>> conflictModules = new HashMap<>();

        String requestedVersion = dependency.getVersion();
        if (requestedVersion == null) {
            return conflictModules;
        }
        String groupDependency = dependency.getGroup();
        String nameDependency = dependency.getName();

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
        return conflictModules;
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

    private void checkVersion(Map<LibraryName, Set<String>> conflictModules) {
        if (!conflictModules.isEmpty()) {
            conflictModules.forEach((library, versions) -> {
                String errorMsg = String.format("There is major version conflict for dependency=%s:%s, versions=%s",
                        library.getGroup(), library.getName(), versions);
                getProject().getLogger().error(errorMsg);
            });
            throw new RuntimeException("There is major version conflict");
        }
    }
}
