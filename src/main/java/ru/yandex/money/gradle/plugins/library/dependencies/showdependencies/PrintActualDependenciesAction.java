package ru.yandex.money.gradle.plugins.library.dependencies.showdependencies;

import groovy.json.JsonBuilder;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

/**
 * Выводит актуальные версии подключаемых библиотек
 */
public class PrintActualDependenciesAction implements Action<Project> {
    private static final Logger log = LoggerFactory.getLogger(PrintActualDependenciesAction.class);

    private final DependencyType dependencyType;

    PrintActualDependenciesAction(DependencyType dependencyType) {
        this.dependencyType = dependencyType;
    }

    @Override
    public void execute(Project project) {
        ConfigurationContainer configurationContainer = project.getConfigurations();

        Set<Dependency> checked = new HashSet<>();
        List<Map<String, String>> dependencies = new ArrayList<>();
        for (Configuration configuration : configurationContainer) {

            try {

                Set<ResolvedArtifact> resolvedArtifacts = configuration
                        .getResolvedConfiguration()
                        .getResolvedArtifacts();

                List<ModuleVersionIdentifier> moduleVersionIdentifiers = resolvedArtifacts.stream().map(ra -> ra.getModuleVersion().getId())
                        .collect(Collectors.toList());

                Map<String, String> resolvedVersionMap = moduleVersionIdentifiers.stream()
                        .collect(Collectors.toMap(mvId -> mvId.getGroup() + ":" + mvId.getName(),
                                ModuleVersionIdentifier::getVersion));

                configuration.getAllDependencies()
                        .forEach(dependency -> {
                                    if (checked.contains(dependency)) {
                                        return;
                                    }
                                    checked.add(dependency);

                                    if (dependencyType.isCorresponds(dependency) &&
                                            nonNull(dependency.getGroup())) {
                                        Map<String, String> dep = new HashMap<>();
                                        dependencies.add(dep);
                                        dep.put("scope", configuration.getName());
                                        dep.put("group", dependency.getGroup());
                                        dep.put("name", dependency.getName());
                                        dep.put("version", resolvedVersionMap.get(dependency.getGroup() + ":" + dependency.getName()));
                                    }
                                }
                        );
            } catch (IllegalStateException e) {
                log.info(String.format("The trouble with resolve configuration: configuration=%s", configuration.getName()), e);
            }
        }
        dependencies.sort(new DependencyComparator());
        JsonBuilder js = new JsonBuilder(dependencies);
        project.getLogger().lifecycle("{}", js.toPrettyString());

        File reportFile = new File(
                project.getBuildDir(),
                "report/dependencies/actual_" + FilenameUtils.getName(dependencyType.getCode()) + "_dependencies.json"
        );
        try {
            writeToFile(js, reportFile);
        } catch (IOException exc) {
            log.warn("Unable to store actual dependencies", exc);
        }
    }

    private static void writeToFile(JsonBuilder js, File reportFile) throws IOException {
        if (!reportFile.getParentFile().exists()) {
            if (reportFile.getParentFile().mkdirs()) {
                log.info("Report dir created");
            }
        }
        if (reportFile.exists()) {
            if (reportFile.delete()) {
                log.info("Existed report deleted");
            }
        }
        try (OutputStream fos = java.nio.file.Files.newOutputStream(reportFile.toPath())) {
            fos.write(js.toPrettyString().getBytes(StandardCharsets.UTF_8));
        }
    }

    private static class DependencyComparator implements Comparator<Map<String, String>>, Serializable {
        @Override
        public int compare(Map<String, String> dep1, Map<String, String> dep2) {
            int compareResult = (dep1.get("scope")).compareTo((dep2.get("scope")));
            if (compareResult != 0) {
                return compareResult;
            }
            compareResult = (dep1.get("group")).compareTo((dep2.get("group")));
            if (compareResult != 0) {
                return compareResult;
            }
            compareResult = (dep1.get("name")).compareTo((dep2.get("name")));
            return compareResult;
        }
    }
}
