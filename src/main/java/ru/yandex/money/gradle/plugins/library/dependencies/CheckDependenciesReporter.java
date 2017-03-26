package ru.yandex.money.gradle.plugins.library.dependencies;

import org.gradle.api.artifacts.Configuration;
import ru.yandex.money.gradle.plugins.library.dependencies.analysis.conflicts.ConflictedLibraryInfo;
import ru.yandex.money.gradle.plugins.library.dependencies.exclusions.ExclusionRule;
import ru.yandex.money.gradle.plugins.library.dependencies.reporters.ConflictedLibrariesReporter;
import ru.yandex.money.gradle.plugins.library.dependencies.reporters.StaleExclusionsReporter;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Формирует отчет о библиотеках с конфликтами версий по каждой конфигурации
 *
 * @author Brovin Yaroslav (brovin@yamoney.ru)
 * @since 30.01.2017
 */
class CheckDependenciesReporter {

    private static final char MESSAGES_INDENT = '\t';
    private static final int BASE_REPORTER_CAPACITY = 1000;
    private final Collection<String> messages = new ArrayList<>();

    private final ConflictedLibrariesReporter conflictedLibrariesReporter = new ConflictedLibrariesReporter(messages);
    private final StaleExclusionsReporter staleExclusionsReporter = new StaleExclusionsReporter(messages);

    /**
     * Фиксация в отчете списка проблемных библиотек с конфликтными версиями
     *
     * @param configuration       конфигурация
     * @param conflictedLibraries список конфликтных библиотек
     */
    void reportConflictedLibrariesForConfiguration(Configuration configuration, Iterable<ConflictedLibraryInfo> conflictedLibraries) {
        conflictedLibrariesReporter.report(configuration, conflictedLibraries);
    }

    void reportStaleExclusions(Iterable<ExclusionRule> exclusionRules) {
        staleExclusionsReporter.report(exclusionRules);
    }

    /**
     * Возвращает строковое представление отчета
     *
     * @return отчет
     */
    String getFormattedReport() {
        StringBuilder sb = new StringBuilder(BASE_REPORTER_CAPACITY);
        messages.forEach(message -> sb.append(MESSAGES_INDENT).append(message).append('\n'));

        return sb.toString();
    }
}