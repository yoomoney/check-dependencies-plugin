package ru.yandex.money.gradle.plugins.library.dependencies.reporters;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.Versioned;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.DefaultVersionComparator;
import ru.yandex.money.gradle.plugins.library.dependencies.analysis.conflicts.ConflictedLibraryInfo;
import ru.yandex.money.gradle.plugins.library.dependencies.analysis.conflicts.resolvers.ConflictPathResolutionResult;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactDependency;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactName;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * Формирует отчет об обнаруженных конфликтах версий, не зарегистрированных в исключениях
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 19.03.2017
 */
public class ConflictedLibrariesReporter {
    private static final int MAX_INDENT_LEVEL = 10;
    private static final String[] INDENTS = createIndents(MAX_INDENT_LEVEL);

    private static final String ERROR_CONFLICTED_DEPENDENCIES_MSG = "Versions conflict used libraries with fixed platform libraries.";

    private final Collection<String> messages;
    private boolean isHeaderAdded;

    public ConflictedLibrariesReporter(@Nonnull Collection<String> messages) {
        Objects.nonNull(messages);
        this.messages = messages;
    }

    /**
     * Фиксация в отчете списка проблемных библиотек с конфликтными версиями
     *
     * @param configuration       конфигурация
     * @param conflictedLibraries список конфликтных библиотек
     */
    public void report(Configuration configuration, Iterable<ConflictedLibraryInfo> conflictedLibraries) {
        addHeaderIfNecessary();
        addConfigurationSection(String.format("%s - %s", configuration.getName(), configuration.getDescription()));
        conflictedLibraries.forEach(conflict -> addConflictSection(conflict, 1));
        addEmptyMessage();
    }

    private void addHeaderIfNecessary() {
        if (!isHeaderAdded) {
            addMessage(ERROR_CONFLICTED_DEPENDENCIES_MSG);
            isHeaderAdded = true;
        }
    }

    private void addConfigurationSection(String configuration) {
        if (!messages.isEmpty()) {
            addEmptyMessage();
        }
        addMessage(configuration);
    }

    private void addConflictSection(ConflictedLibraryInfo conflictedLibraryInfo, int indent) {
        addConflictHeader(conflictedLibraryInfo, indent);
        conflictedLibraryInfo.getConflictDependentPaths().forEach(conflictPath -> {
            ConflictPathResolutionResult resolutionResult = conflictedLibraryInfo.getResolutionFor(conflictPath);
            addConflictPath(conflictPath, resolutionResult, indent + 1);
        });
        addEmptyMessage();
    }

    private void addConflictHeader(ConflictedLibraryInfo conflictedLibraryInfo, int indent) {
        String message = String.format("--- %-50s: %s -> %s",
                NameFormatter.format(conflictedLibraryInfo.getLibrary()),
                conflictedLibraryInfo.getVersion(),
                conflictedLibraryInfo.getFixedVersion());
        addMessage(message, indent);
    }

    private void addConflictPath(Iterable<ArtifactDependency> conflictPath,
                                 ConflictPathResolutionResult resolutionResult,
                                 int indent) {
        addMessage(getDependencyPathString(conflictPath), indent);
        addResolutionResult(resolutionResult, indent + 1);
    }

    private void addResolutionResult(ConflictPathResolutionResult resolutionResult, int indent) {
        ArtifactName directDependency = resolutionResult.getDirectDependency();

        if (resolutionResult.getSuggestedVersions().isEmpty()) {
            String message = String.format("NO SOLUTIONS FOUND for %s with versions:",
                                           NameFormatter.format(directDependency.getLibraryName()));
            addMessage(message, indent);
            Set<String> checkedVersions = resolutionResult.getCheckedVersions();
            addMessage(checkedVersions.isEmpty() ? "[no any versions were analyzed]" : getSortedVersions(checkedVersions), indent + 1);
            return;
        }

        String suggestedVersionsHeader = String.format("TRY TO CHANGE VERSION OF %s FROM %s to one of the proposed:",
                                                       NameFormatter.format(directDependency.getLibraryName()),
                                                       directDependency.getVersion());

        addMessage(suggestedVersionsHeader, indent);
        addMessage(getSortedVersions(resolutionResult.getSuggestedVersions()), indent + 1);
    }

    private static Collection<String> getSortedVersions(Set<String> versions) {
        TreeSet<Versioned> versioneds = new TreeSet<>(new DefaultVersionComparator());
        versions.stream().map(version -> (Versioned)(() -> version)).forEach(versioneds::add);
        return versioneds.stream().map(Versioned::getVersion).collect(Collectors.toList());
    }

    private String getDependencyPathString(Iterable<ArtifactDependency> dependencyPath) {
        return "--> " + StreamSupport.stream(dependencyPath.spliterator(), false)
                                     .map(dependency -> String.format("(%s)", DependencyFormatter.format(dependency)))
                                     .reduce((dep1, dep2) -> dep1 + " --> " + dep2).orElse("");
    }

    private void addEmptyMessage() {
        addMessage("");
    }

    private void addMessage(Object message) {
        addMessage(message, 0);
    }

    private void addMessage(Object message, int indentLevel) {
        messages.add(INDENTS[indentLevel] + message);
    }

    private static String[] createIndents(int maxIndentLevel) {
        return IntStream.range(0, maxIndentLevel + 1).boxed()
                .map(ConflictedLibrariesReporter::createIndent)
                .toArray(String[]::new);
    }

    private static String createIndent(int indentLevel) {
        return String.join("", Collections.nCopies(indentLevel, "    "));
    }
}
