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

    private static final String MESSAGES_INDENT = "\t";
    private static final int BASE_REPORTER_CAPACITY = 1000;
    private final Collection<String> messages = new ArrayList<>();

    /**
     * Фиксация в отчете списка проблемных библиотек с конфликтными версиями
     *
     * @param configuration       конфигурация
     * @param conflictedLibraries список конфликтных библиотек
     */
    void reportConflictedLibrariesForConfiguration(Configuration configuration, Iterable<ConflictedLibraryInfo> conflictedLibraries) {
        addConfigurationSection(String.format("%s - %s", configuration.getName(), configuration.getDescription()));

        conflictedLibraries.forEach(library -> {
            String message = String.format("   --- %-50s: %s -> %s", library.getLibrary(), library.getVersion(),
                    library.getFixedVersion());
            messages.add(message);
        });
    }

    private void addConfigurationSection(String configuration) {
        if (!messages.isEmpty()) {
            messages.add("");
        }
        messages.add(configuration);
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