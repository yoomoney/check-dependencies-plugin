package ru.yandex.money.gradle.plugins.library.dependencies.forbiddenartifacts;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.TaskAction;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactName;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ru.yandex.money.gradle.plugins.library.dependencies.NexusUtils.getArtifactLatestVersion;

/**
 * Задача, проверяющая наличие запрещенных артефактов
 *
 * @author horyukova
 * @since 26.05.2019
 */
public class CheckForbiddenDependenciesTask extends DefaultTask {
    private Set<ArtifactName> forbiddenArtifacts;

    public void setForbiddenArtifacts(Set<ArtifactName> forbiddenArtifacts) {
        this.forbiddenArtifacts = forbiddenArtifacts;
    }

    /**
     * Проверяем наличие запрещенных артефактов
     */
    @TaskAction
    public void forbiddenArtifacts() {
        Set<Dependency> foundForbiddenArtifacts = getProject().getConfigurations().stream()
                .flatMap(configuration -> configuration.getAllDependencies().stream())
                .filter(this::isForbiddenArtifacts)
                .collect(Collectors.toSet()).stream()
                .peek(this::printForbiddenVersion)
                .peek(this::printRecommendedVersion)
                .collect(Collectors.toSet());

        if (!foundForbiddenArtifacts.isEmpty()) {
            throw new GradleException("There is forbidden dependencies");
        }
    }

    private boolean isForbiddenArtifacts(Dependency dependency) {
        ArtifactName artifactName =
                new ArtifactName(dependency.getGroup(), dependency.getName(), dependency.getVersion());

        return forbiddenArtifacts.stream()
                .filter(forbiddenArtifact -> forbiddenArtifact.getLibraryName().equals(artifactName.getLibraryName()))
                .anyMatch(forbiddenArtifact ->
                        isForbiddenVersion(forbiddenArtifact.getVersion(), artifactName.getVersion()));
    }

    /**
     * Проверяет, входит ли данная версия в запрещенные.
     *
     * @param forbiddenVersion запрещенная версия
     * @param realVersion      актуальная версия
     * @return true, если актуальная версия входит в запрещенные
     */
    private boolean isForbiddenVersion(String forbiddenVersion, String realVersion) {
        if (forbiddenVersion.contains(">")) {
            //если realVersion больше forbiddenVersion - она запрещена
            return compareVersions(realVersion, removeComparisonSigns(forbiddenVersion)) > 0;
        }

        if (forbiddenVersion.contains("<")) {
            //если realVersion меньше forbiddenVersion - она запрещена
            return compareVersions(realVersion, removeComparisonSigns(forbiddenVersion)) < 0;
        }
        return forbiddenVersion.equals(realVersion);
    }

    private void printForbiddenVersion(Dependency dependency) {
        getProject().getLogger().lifecycle("Forbidden dependency: {}:{}:{}",
                dependency.getGroup(), dependency.getName(), dependency.getVersion());
    }

    private void printRecommendedVersion(Dependency dependency) {
        Optional.of(dependency)
                .filter(dep -> dep.getGroup() != null)
                .map(dep -> getArtifactLatestVersion(dependency.getGroup(), dependency.getName()))
                .filter(newVersion -> !Objects.equals(dependency.getVersion(), newVersion))
                .ifPresent(newVersion -> printVersion(dependency, dependency.getVersion(), newVersion));
    }

    private void printVersion(Dependency dependency, String realVersion, String newVersion) {

        getProject().getLogger().lifecycle("Recommended version: {}:{} {} -> {}",
                dependency.getGroup(), dependency.getName(), realVersion, newVersion);
    }

    /**
     * Сравнивает версии
     *
     * @return отрицательное число, если version1 меньше version2,
     * положительное, если version1 больше version2,
     * ноль, если версии равны
     */
    private int compareVersions(String version1, String version2) {
        String[] versionAsArray1 = version1.split("\\.");
        String[] versionAsArray2 = version2.split("\\.");

        for (int i = 0; i < versionAsArray2.length; i++) {
            Integer num1 = Integer.parseInt(versionAsArray1[i]);
            Integer num2 = Integer.parseInt(versionAsArray2[i]);

            if (!num1.equals(num2)) {
                return num1.compareTo(num2);
            }
        }
        return 0;
    }

    private String removeComparisonSigns(String versionWithComparisonSigns) {
        Pattern pattern = Pattern.compile("\\d+\\.\\d+\\.\\d+");
        Matcher matcher = pattern.matcher(versionWithComparisonSigns);
        if (matcher.find()) {
            return matcher.group(0);
        }
        throw new GradleException("Forbidden dependency should match version pattern. " +
                "For example: 1.2.3; >1.2.3, <1.2.3");
    }
}
