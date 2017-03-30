package ru.yandex.money.gradle.plugins.library.dependencies.reporters;

import org.gradle.api.artifacts.Configuration;
import ru.yandex.money.gradle.plugins.library.dependencies.analysis.ConflictedLibraryInfo;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactDependency;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import java.util.Objects;

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
        conflictedLibraryInfo.getDependentPaths().forEach(conflictPath -> addConflictPath(conflictPath, indent + 1));
        addEmptyMessage();
    }

    private void addConflictHeader(ConflictedLibraryInfo conflictedLibraryInfo, int indent) {
        String message = String.format("--- %-50s: %s -> %s",
                NameFormatter.format(conflictedLibraryInfo.getLibrary()),
                conflictedLibraryInfo.getVersion(),
                conflictedLibraryInfo.getFixedVersion());
        addMessage(message, indent);
    }

    private void addConflictPath(Iterable<ArtifactDependency> conflictPath, int indent) {
        addMessage(getDependencyPathString(conflictPath), indent);
    }

    private String getDependencyPathString(Iterable<ArtifactDependency> dependencyPath) {
        return "--> " + StreamSupport.stream(dependencyPath.spliterator(), false)
                                     .map(dependency -> String.format("(%s)", DependencyFormatter.format(dependency)))
                                     .reduce((dep1, dep2) -> dep1 + " --> " + dep2).orElse("");
    }

    private void addEmptyMessage() {
        addMessage("");
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
