package ru.yandex.money.gradle.plugins.library.dependencies;

import io.spring.gradle.dependencymanagement.DependencyManagementExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ComponentSelector;
import org.gradle.api.artifacts.component.ModuleComponentSelector;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.tasks.TaskAction;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Задача на проверку согласованности изменений версий используемых библиотек. Если изменение версии библиотеки связано
 * с фиксацией версии в <c>Spring Dependency Management</c> плагине, то останавливает билд и выводит список библиотек,
 * у которых изменение версий не запланировано.
 *
 * @author Brovin Yaroslav (brovin@yamoney.ru)
 * @since 27.01.2017
 */
public class CheckDependenciesTask extends DefaultTask {

    private static final String ERROR_CONFLICTED_DEPENDENCIES_MSG = "Versions conflict used libraries with fixed platform libraries. \n %s";
    private final CheckDependenciesReporter reporter = new CheckDependenciesReporter();
    private DependencyManagementExtension dependencyManagementExtension;

    @TaskAction
    public void check() {
        dependencyManagementExtension = getProject().getExtensions().getByType(DependencyManagementExtension.class);
        boolean totalResult = true;

        for (Configuration configuration : getProject().getConfigurations()) {
            totalResult = checkLibraryVersionFor(configuration) && totalResult;
        }

        if (!totalResult) {
            throw new IllegalStateException(String.format(ERROR_CONFLICTED_DEPENDENCIES_MSG, reporter.toString()));
        }
    }

    /**
     * Проверяет корректность изменения версий используемых библиотек. Если изменения версии не легитимные,
     * то возвращает false
     *
     * @param configuration конфигурация сборки
     * @return Правомерны изменения версий библиотек или нет
     */
    private boolean checkLibraryVersionFor(@Nonnull Configuration configuration) {
        Map<String, String> fixedLibraries = getFixedLibraries(configuration);
        Map<String, String> projectLibraries = getProjectLibraries(configuration);
        List<ConflictedLibraryInfo> conflictedLibraries = calculateConflictedLibraries(fixedLibraries, projectLibraries);

        if (!conflictedLibraries.isEmpty()) {
            reporter.logConflictedLibrary(configuration, conflictedLibraries);
        }

        return conflictedLibraries.isEmpty();
    }

    /**
     * Возвращает набор всех используемых (Прямые и транзитивные зависимости) подключений библиотек в проекте для указанной
     * конфигурации.
     *
     * @param configuration конфигурация сборки
     * @return словарь: ключ - название библиотеки, значение - версия (после работы ResolutionStrategy)
     */
    private static Map<String, String> getProjectLibraries(@Nonnull Configuration configuration) {
        Set<? extends DependencyResult> projectDependencies = configuration.getIncoming().getResolutionResult().getAllDependencies();
        Map<String, String> projectLibraries = new HashMap<>();

        for (DependencyResult dependency : projectDependencies) {
            ComponentSelector selector = dependency.getRequested();
            if (selector instanceof ModuleComponentSelector) {
                ModuleComponentSelector targetLibrary = (ModuleComponentSelector) selector;
                String selectedLibrary = String.format("%s:%s", targetLibrary.getGroup(), targetLibrary.getModule());
                String selectedVersion = targetLibrary.getVersion();

                projectLibraries.put(selectedLibrary, selectedVersion);
            }
        }

        return projectLibraries;
    }

    /**
     * Возвращает набор всех библиотек с зафиксированными версиями в проекте для указанной конфигурации.
     * <p>
     * Использует результат работы стороннего плагина <c>io.spring.dependency-management</c>
     *
     * @param configuration конфигурация сборки
     * @return словарь: ключ - название библиотеки, значение - версия
     */
    private Map<String, String> getFixedLibraries(@Nonnull Configuration configuration) {
        return (Map<String, String>) dependencyManagementExtension.forConfiguration(configuration.getName()).getManagedVersions();
    }

    /**
     * Анализирует версии проектных библиотек и сравнивает их с со списком фиксированных версий библиотек.
     *
     * @param fixedLibraries   список библиотек с зафиксированной версией
     * @param projectLibraries список проектных библиотек
     * @return список библиотек с конфликтом версий
     */
    private static List<ConflictedLibraryInfo> calculateConflictedLibraries(@Nonnull Map<String, String> fixedLibraries,
                                                                            @Nonnull Map<String, String> projectLibraries) {
        List<ConflictedLibraryInfo> conflictedLibraries = new ArrayList<>();

        projectLibraries.forEach((library, version) -> {
            String fixedVersion = fixedLibraries.get(library);
            if (fixedVersion != null && !Objects.equals(version, fixedVersion)) {
                conflictedLibraries.add(new ConflictedLibraryInfo(library, version, fixedVersion));
            }
        });
        return conflictedLibraries;
    }

    /**
     * Информация о конфликте: название библиотеки (Группа + имя артефакта), первоначальная запрашиваемая версия и конечная версии
     * после разрешения конфликта
     */
    private static class ConflictedLibraryInfo {

        private final String library;
        private final String version;
        private final String fixedVersion;

        ConflictedLibraryInfo(String library, String version, String fixedVersion) {
            this.library = library;
            this.version = version;
            this.fixedVersion = fixedVersion;
        }

        String getLibrary() {
            return library;
        }

        String getVersion() {
            return version;
        }

        String getFixedVersion() {
            return fixedVersion;
        }
    }

    /**
     * Формирует отчет о библиотеках с конфликтами версий по каждой конфигурации
     */
    private static class CheckDependenciesReporter {

        private static final String MESSAGES_INDENT = "    ";
        private static final String FORMAT_CONFLICTED_LIBRARY_OUTPUT = "   --- %-40s: %s -> %s";
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
}