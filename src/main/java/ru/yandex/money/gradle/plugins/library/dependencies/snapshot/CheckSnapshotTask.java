package ru.yandex.money.gradle.plugins.library.dependencies.snapshot;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.internal.artifacts.dependencies.ProjectDependencyInternal;
import org.gradle.api.tasks.TaskAction;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Проверяет наличие snapshot-зависимостей
 *
 * @author horyukova
 * @since 27.02.2019
 */
public class CheckSnapshotTask extends DefaultTask {
    private static final Pattern SNAPSHOT_PATTERN = Pattern.compile("^.+(\\d{8}\\.\\d{6}-\\d+)$");
    private static final String FORCE_FLAG = "forceRelease";

    /**
     * Проверка snapshot-зависимостей
     */
    @TaskAction
    public void checkSnapshot() {
        boolean isForceRelease = getProject().hasProperty(FORCE_FLAG)
                && Boolean.parseBoolean(getProject().findProperty(FORCE_FLAG).toString());

        if (isForceRelease) {
            getProject().getLogger().lifecycle("Force release action. SKIPPED");
            return;
        }

        Set<String> snapshotPackages = getProject().getConfigurations().stream()
                .flatMap(configuration -> configuration.getAllDependencies().stream())
                .filter(this::isSnapshotDependencies)
                .map(dependency -> String.format("%s:%s:%s",
                        dependency.getGroup(), dependency.getName(), dependency.getVersion()))
                .collect(Collectors.toSet());

        if (!snapshotPackages.isEmpty()) {
            throw new IllegalStateException("You have the following SNAPSHOT dependencies:\n" + snapshotPackages);
        }
    }

    private boolean isSnapshotDependencies(Dependency dependency) {
        String version = dependency.getVersion();
        if (version == null) {
            return false;
        }

        return (version.toUpperCase().contains("-SNAPSHOT") ||
                SNAPSHOT_PATTERN.matcher(version).matches())
                && !(dependency instanceof ProjectDependencyInternal);
    }
}
