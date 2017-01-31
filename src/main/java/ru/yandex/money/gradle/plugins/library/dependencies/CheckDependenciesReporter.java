package ru.yandex.money.gradle.plugins.library.dependencies;

import org.gradle.api.artifacts.Configuration;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Формирует отчет о библиотеках с конфликтами версий по каждой конфигурации
 *
 * @author Brovin Yaroslav (brovin@yamoney.ru)
 * @since 30.01.2017
 */
class CheckDependenciesReporter {

    private static final String MESSAGES_INDENT = "    ";
    private static final String FORMAT_CONFLICTED_LIBRARY_OUTPUT = "   --- %-50s: %s -> %s";
    private static final int BASE_REPORTER_CAPACITY = 1000;
    private final Collection<String> messages = new ArrayList<>();

    /**
     * Фиксация в отчете списка проблемных библиотек с конфликтными версиями
     * @param configuration конфигурация
     * @param conflictedLibraries список конфликтных библиотек
     */
    void logConflictedLibrary(Configuration configuration, Iterable<ConflictedLibraryInfo> conflictedLibraries) {
        logConfiguration(String.format("%s - %s", configuration.getName(), configuration.getDescription()));

        conflictedLibraries.forEach(library -> {
            String message = String.format(FORMAT_CONFLICTED_LIBRARY_OUTPUT, library.getLibrary(), library.getVersion(),
                    library.getFixedVersion());
            logLibrary(message);
        });
    }

    private void logLibrary(String message) {
        messages.add(message);
    }

    private void logConfiguration(String catalog) {
        if (!messages.isEmpty()) {
            messages.add("");
        }
        messages.add(catalog);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(BASE_REPORTER_CAPACITY);
        messages.forEach(message -> sb.append(MESSAGES_INDENT).append(message).append('\n'));

        return sb.toString();
    }
}
