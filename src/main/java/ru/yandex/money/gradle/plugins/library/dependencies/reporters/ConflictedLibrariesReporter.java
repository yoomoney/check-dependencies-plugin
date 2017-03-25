package ru.yandex.money.gradle.plugins.library.dependencies.reporters;

import org.gradle.api.artifacts.Configuration;
import ru.yandex.money.gradle.plugins.library.dependencies.analysis.ConflictedLibraryInfo;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactDependency;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.DependencyPath;

import java.util.Collection;
import java.util.Collections;
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
    private static final String CONFLICT_PATHS_HEADER = "Following dependencies cause conflicts:";

    private final Collection<String> messages;
    private boolean isHeaderAdded;

    public ConflictedLibrariesReporter(Collection<String> messages) {
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
    }

    private void addHeaderIfNecessary() {
        if (!isHeaderAdded) {
            addMessage(ERROR_CONFLICTED_DEPENDENCIES_MSG);
            isHeaderAdded = true;
        }
    }

    private void addConfigurationSection(String configuration) {
        if (!messages.isEmpty()) {
            addMessage("");
        }
        addMessage(configuration);
    }

    private void addConflictSection(ConflictedLibraryInfo conflictedLibraryInfo, int indent) {
        addConflictHeader(conflictedLibraryInfo, indent);
        addMessage(CONFLICT_PATHS_HEADER, indent + 1);
        conflictedLibraryInfo.getDependentPaths().forEach(conflictPath -> addConflictPath(conflictPath, indent + 1));
    }

    private void addConflictHeader(ConflictedLibraryInfo conflictedLibraryInfo, int indent) {
        String message = String.format("--- %-50s: %s -> %s",
                conflictedLibraryInfo.getLibrary(),
                conflictedLibraryInfo.getVersion(),
                conflictedLibraryInfo.getFixedVersion());
        addMessage(message, indent);
    }

    private void addConflictPath(DependencyPath<ArtifactDependency> conflictPath, int indent) {
        addMessage(getDependencyPathString(conflictPath), indent);
    }

    private String getDependencyPathString(DependencyPath<ArtifactDependency> dependencyPath) {
        return "--> " + StreamSupport.stream(dependencyPath.spliterator(), false)
                                     .map(dependency -> String.format("(%s)", DependencyFormatter.format(dependency)))
                                     .reduce((dep1, dep2) -> dep1 + " --> " + dep2).orElse("");
    }

    private void addMessage(String message) {
        addMessage(message, 0);
    }

    private void addMessage(String message, int indentLevel) {
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
