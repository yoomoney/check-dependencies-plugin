package ru.yoomoney.gradle.plugins.library.dependencies.snapshot;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.internal.artifacts.dependencies.ProjectDependencyInternal;
import org.gradle.api.tasks.TaskAction;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Проверяет наличие snapshot-зависимостей
 *
 * @author horyukova
 * @since 27.02.2019
 */
public class CheckSnapshotsDependenciesTask extends DefaultTask {
    private static final Pattern SNAPSHOT_PATTERN = Pattern.compile("^.+(\\d{8}\\.\\d{6}-\\d+)$");
    private static final Pattern SNAPSHOT_REPOSITORY_PATTERN = Pattern.compile("^.+snapshots/?$");
    private static final String FORCE_FLAG = "allowSnapshot";

    /**
     * Проверка snapshot-зависимостей
     */
    @TaskAction
    public void checkSnapshotsDependencies() {
        boolean isForceRelease = getProject().hasProperty(FORCE_FLAG)
                && Boolean.parseBoolean(getProject().findProperty(FORCE_FLAG).toString());

        if (isForceRelease) {
            getProject().getLogger().lifecycle("Snapshot dependencies are allowed. SKIPPED");
            return;
        }

        checkBuildscript();
        checkDependencies(getProject().getConfigurations());
    }

    private void checkBuildscript() {
        ScriptHandler buildscript = getProject().getBuildscript();

        Set<String> buildScriptSnapshotRepositories = buildscript.getRepositories().stream()
                .filter(repository -> repository instanceof MavenArtifactRepository)
                .map(r -> ((MavenArtifactRepository) r).getUrl().toString())
                .filter(this::isSnapshotRepository)
                .collect(Collectors.toSet());

        if (!buildScriptSnapshotRepositories.isEmpty()) {
            throw new IllegalStateException("You have the following SNAPSHOT repositories:" + System.lineSeparator()
                    + buildScriptSnapshotRepositories);
        }

        checkDependencies(buildscript.getConfigurations());
    }

    private void checkDependencies(Collection<Configuration> configurationContainer) {
        Set<String> snapshotPackages = configurationContainer.stream()
                .flatMap(configuration -> configuration.getAllDependencies().stream())
                .filter(this::isSnapshotDependencies)
                .map(dependency -> String.format("%s:%s:%s",
                        dependency.getGroup(), dependency.getName(), dependency.getVersion()))
                .collect(Collectors.toSet());

        if (!snapshotPackages.isEmpty()) {
            throw new IllegalStateException("You have the following SNAPSHOT dependencies:" + System.lineSeparator()
                    + snapshotPackages);
        }
    }

    private boolean isSnapshotRepository(String repository) {
        return SNAPSHOT_REPOSITORY_PATTERN.matcher(repository).matches();
    }

    private boolean isSnapshotDependencies(Dependency dependency) {
        String version = dependency.getVersion();
        if (version == null) {
            return false;
        }

        if (dependency instanceof ProjectDependencyInternal) {
            return false;
        }

        return version.toUpperCase().contains("-SNAPSHOT") ||
                SNAPSHOT_PATTERN.matcher(version).matches();
    }
}
