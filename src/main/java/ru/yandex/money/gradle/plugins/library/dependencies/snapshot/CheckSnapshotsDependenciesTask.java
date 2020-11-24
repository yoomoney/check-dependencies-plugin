package ru.yandex.money.gradle.plugins.library.dependencies.snapshot;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.internal.artifacts.dependencies.ProjectDependencyInternal;
import org.gradle.api.tasks.TaskAction;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Проверяет наличие snapshot-зависимостей
 *
 * @author horyukova
 * @since 27.02.2019
 */
public class CheckSnapshotsDependenciesTask extends DefaultTask {
    private static final Pattern SNAPSHOT_PATTERN = Pattern.compile("^.+(\\d{8}\\.\\d{6}-\\d+)$");
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

        Set<String> snapshotRepositories = getProject().getBuildscript().getRepositories().stream()
                .filter(repository -> repository instanceof MavenArtifactRepository)
                .map(r -> ((MavenArtifactRepository) r).getUrl().toString())
                .filter(this::isSnapshotRepository)
                .collect(Collectors.toSet());

        Set<String> snapshotPackages = Stream.concat(getProject().getConfigurations().stream(),
                getProject().getBuildscript().getConfigurations().stream())
                .flatMap(configuration -> configuration.getAllDependencies().stream())
                .filter(this::isSnapshotDependencies)
                .map(dependency -> String.format("%s:%s:%s",
                        dependency.getGroup(), dependency.getName(), dependency.getVersion()))
                .collect(Collectors.toSet());

        if (!snapshotRepositories.isEmpty()) {
            throw new IllegalStateException("You have the following SNAPSHOT repositories:" + System.lineSeparator()
                    + snapshotRepositories);
        }

        if (!snapshotPackages.isEmpty()) {
            throw new IllegalStateException("You have the following SNAPSHOT dependencies:" + System.lineSeparator()
                    + snapshotPackages);
        }
    }

    private boolean isSnapshotRepository(String repository) {
        return repository.endsWith("snapshots/");
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
