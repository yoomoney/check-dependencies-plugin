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
        boolean hasVersionsConflict = false;

        for (Configuration configuration : getProject().getConfigurations()) {
            List<ConflictedLibraryInfo> conflictedLibraries = calculateConflictedVersionsLibrariesFor(configuration);
            if (!conflictedLibraries.isEmpty()) {
                reporter.reportConflictedLibrariesForConfiguration(configuration, conflictedLibraries);
            }
            if (!hasVersionsConflict) {
                hasVersionsConflict = !conflictedLibraries.isEmpty();
            }
        }

        if (hasVersionsConflict) {
            throw new IllegalStateException(String.format(ERROR_CONFLICTED_DEPENDENCIES_MSG, reporter.getFormattedReport()));
        }
    }

    /**
     * Анализирует версии проектных библиотек и сравнивает их с со списком фиксированных версий библиотек для конфигурации.
     *
     * @param configuration конфигурация сборки
     * @return Правомерны изменения версий библиотек или нет
     **/
    private List<ConflictedLibraryInfo> calculateConflictedVersionsLibrariesFor(@Nonnull Configuration configuration) {
        Map<String, String> fixedLibraries = getFixedLibraries(configuration);
        Map<String, ArrayList<String>> requestedLibraries = getRequestedLibraries(configuration);
        return calculateConflictedLibraries(fixedLibraries, requestedLibraries);
    }

    /**
     * Возвращает набор всех запрашиваемых (Прямые и транзитивные зависимости) библиотек в проекте для указанной
     * конфигурации до работы ResolutionStrategy.
     *
     * @param configuration конфигурация сборки
     * @return словарь: ключ - название библиотеки, значение - список всех найденных версий (до работы ResolutionStrategy)
     */
    private static Map<String, ArrayList<String>> getRequestedLibraries(@Nonnull Configuration configuration) {
        Set<? extends DependencyResult> projectDependencies = configuration.getIncoming().getResolutionResult().getAllDependencies();
        Map<String, ArrayList<String>> requestedLibraries = new HashMap<>();

        for (DependencyResult dependency : projectDependencies) {
            ComponentSelector selector = dependency.getRequested();
            if (selector instanceof ModuleComponentSelector) {
                ModuleComponentSelector targetLibrary = (ModuleComponentSelector) selector;
                String selectedLibrary = String.format("%s:%s", targetLibrary.getGroup(), targetLibrary.getModule());
                String selectedVersion = targetLibrary.getVersion();

                addLibraryVersion(requestedLibraries, selectedLibrary, selectedVersion);
            }
        }

        return requestedLibraries;
    }

    /**
     * Фиксирует указанную версию библиотеки в списке libraries
     *
     * @param libraries Словарь: ключ - название библиотеки, значение - список версий
     * @param library   Название библиотеки
     * @param version   Версия библиотеки
     */
    private static void addLibraryVersion(@Nonnull Map<String, ArrayList<String>> libraries, @Nonnull String library,
                                          @Nonnull String version) {
        ArrayList<String> versions;
        if (libraries.containsKey(version)) {
            versions = libraries.get(version);
        } else {
            versions = new ArrayList<>();
            libraries.put(library, versions);
        }

        if (!versions.contains(version)) {
            versions.add(version);
        }
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
                                                                            @Nonnull Map<String, ArrayList<String>> projectLibraries) {
        List<ConflictedLibraryInfo> conflictedLibraries = new ArrayList<>();

        projectLibraries.forEach((library, versions) -> {
            String fixedVersion = fixedLibraries.get(library);

            if (fixedVersion != null) {
                versions.forEach(requestedVersion -> {
                    if (!Objects.equals(requestedVersion, fixedVersion)) {
                        conflictedLibraries.add(new ConflictedLibraryInfo(library, requestedVersion, fixedVersion));
                    }
                });
            }
        });
        return conflictedLibraries;
    }
}