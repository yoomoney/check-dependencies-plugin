package ru.yandex.money.gradle.plugins.library.dependencies.reporters;

import org.gradle.api.artifacts.Configuration;
import ru.yandex.money.gradle.plugins.library.dependencies.ConflictedLibraryInfo;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Objects;

/**
 * Формирует отчет об обнаруженных конфликтах версий, не зарегистрированных в исключениях
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 19.03.2017
 */
public class ConflictedLibrariesReporter {
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

        conflictedLibraries.forEach(library -> {
            String message = String.format("   --- %-50s: %s -> %s", library.getLibrary(), library.getVersion(),
                    library.getFixedVersion());
            messages.add(message);
        });
    }

    private void addHeaderIfNecessary() {
        if (!isHeaderAdded) {
            messages.add(ERROR_CONFLICTED_DEPENDENCIES_MSG);
            isHeaderAdded = true;
        }
    }

    private void addConfigurationSection(String configuration) {
        if (!messages.isEmpty()) {
            messages.add("");
        }
        messages.add(configuration);
    }
}
