package ru.yoomoney.gradle.plugins.library.dependencies;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yoomoney.gradle.plugins.library.dependencies.analysis.FixedDependencies;
import ru.yoomoney.gradle.plugins.library.dependencies.analysis.conflicts.ConfigurationConflictsAnalyzer;
import ru.yoomoney.gradle.plugins.library.dependencies.analysis.conflicts.ConflictedLibraryInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Задача на проверку согласованности изменений версий используемых библиотек. Если изменение версии библиотеки связано
 * с фиксацией версии в <i>Spring Dependency Management</i> плагине, то останавливает билд и выводит список библиотек,
 * у которых изменение версий не запланировано.
 *
 * @author Brovin Yaroslav
 * @since 27.01.2017
 */
public class CheckDependenciesTask extends ConventionTask {

    private final Logger log = LoggerFactory.getLogger(CheckDependenciesTask.class);

    private FixedDependencies fixedDependencies;

    @Input
    private List<String> includedConfigurations;

    /**
     * Запускается при выполнении таски
     */
    @TaskAction
    public void check() {
        fixedDependencies = FixedDependencies.from(getProject());

        for (Configuration configuration : getCheckedConfigurations()) {
            List<ConflictedLibraryInfo> conflictedLibraries = calculateConflictedVersionsLibrariesFor(configuration);
            if (!conflictedLibraries.isEmpty()) {
                log.warn("There are conflicts: {}", conflictedLibraries);
            }
        }
    }

    /**
     * Возвращает сет проверяемых конфигураций с учетом настройки плагина (Списка исключенных из проверки конфигураций)
     *
     * @return Набор проверяемых конфигураций
     */
    private Iterable<Configuration> getCheckedConfigurations() {
        List<String> included = getIncludedConfigurations();

        return getProject().getConfigurations().matching(configuration ->
                included != null && included.contains(configuration.getName()));
    }

    /**
     * Анализирует версии библиотек для конфигурации проекта и сравнивает их с со списком фиксированных версий библиотек для конфигурации.
     *
     * @param configuration конфигурация сборки
     * @return Правомерны изменения версий библиотек или нет
     **/
    private List<ConflictedLibraryInfo> calculateConflictedVersionsLibrariesFor(@Nonnull Configuration configuration) {
        return ConfigurationConflictsAnalyzer.create(fixedDependencies, configuration)
                .findConflictedLibraries();
    }

    /**
     * Возвращает список конфигураций, которые должны проверяться плагином.
     *
     * @return список конфигураций
     */
    @Nullable
    List<String> getIncludedConfigurations() {
        return includedConfigurations;
    }

    /**
     * Задает список конфигураций, которые должны проверяться плагином.
     *
     * @param includedConfigurations список конфигураций
     */
    void setIncludedConfigurations(List<String> includedConfigurations) {
        this.includedConfigurations = new ArrayList<>(includedConfigurations);
    }
}