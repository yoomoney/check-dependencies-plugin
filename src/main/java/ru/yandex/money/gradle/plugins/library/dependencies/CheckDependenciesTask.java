package ru.yandex.money.gradle.plugins.library.dependencies;

import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ComponentSelector;
import org.gradle.api.artifacts.component.ModuleComponentSelector;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.gradle.api.internal.ConventionTask;
import ru.yandex.money.gradle.plugins.library.dependencies.exclusions.ExclusionRulesLoader;
import ru.yandex.money.gradle.plugins.library.dependencies.exclusions.StaleExclusionsDetector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
public class CheckDependenciesTask extends ConventionTask {

    private final Logger log = LoggerFactory.getLogger(CheckDependenciesTask.class);

    private final CheckDependenciesReporter reporter = new CheckDependenciesReporter();
    private ConflictVersionsResolver conflictVersionsResolver;
    private StaleExclusionsDetector staleExclusionsDetector;
    private DependencyManagementExtension dependencyManagementExtension;

    private List<String> exclusionsRulesSources;
    private List<String> excludedConfigurations;

    @TaskAction
    public void check() {
        ExclusionRulesLoader exclusionRulesLoader = new ExclusionRulesLoader();
        loadExclusionsRules(exclusionRulesLoader);
        conflictVersionsResolver = new ConflictVersionsResolver(exclusionRulesLoader.getTotalExclusionRules());
        staleExclusionsDetector = StaleExclusionsDetector.create(exclusionRulesLoader.getLocalExclusionRules());
        dependencyManagementExtension = getProject().getExtensions().getByType(DependencyManagementExtension.class);

        boolean hasVersionsConflict = false;
        for (Configuration configuration : getCheckedConfigurations()) {
            List<ConflictedLibraryInfo> conflictedLibraries = calculateConflictedVersionsLibrariesFor(configuration);
            if (!conflictedLibraries.isEmpty()) {
                reporter.reportConflictedLibrariesForConfiguration(configuration, conflictedLibraries);
                hasVersionsConflict = true;
            }
        }

        if (hasVersionsConflict) {
            throw new IllegalStateException(reporter.getFormattedReport());
        }

        if (staleExclusionsDetector.hasStaleExclusions()) {
            reporter.reportStaleExclusions(staleExclusionsDetector.getStaleExclusions());
            throw new IllegalStateException(reporter.getFormattedReport());
        }
    }

    /**
     * Возвращает сет проверяемых конфигураций с учетом настройки плагина (Списка исключенных из проверки конфигураций)
     *
     * @return Набор проверяемых конфигураций
     */
    private Iterable<Configuration> getCheckedConfigurations() {
        List<String> excludedConfigurations = getExcludedConfigurations();
        return getProject().getConfigurations().matching(configuration ->
                excludedConfigurations == null || !excludedConfigurations.contains(configuration.getName())
        );
    }

    /**
     * Возвращает список источников местоположений файлов с правилами разрешающими изменение версии библиотек.
     * <p>
     * ВАЖНО: Не смотря на тривиальный код геттера, Gradle перехватывает вызов этого геттера и анализирует возвращаемое значение.
     * Если оно null, то gradle попытается взять значение для свойства "exclusionsRulesSources" из getConventionMapping().
     *
     * @return список источников местоположений файлов
     */
    @Nullable
    List<String> getExclusionsRulesSources() {
        return exclusionsRulesSources;
    }

    /**
     * Задает список местоположений файлов с правилами разрешающими изменение версий библиотек. Этот сеттер может быть использован
     * из Gradle Build скрипта.
     *
     * @param exclusionsRulesSources набор местоположений файлов правил
     */
    void setExclusionsRulesSources(List<String> exclusionsRulesSources) {
        this.exclusionsRulesSources = new ArrayList<>(exclusionsRulesSources);
    }

    /**
     * Возвращает список конфигураций, которые не должны проверяться плагином.
     * <p>
     * ВАЖНО: Не смотря на тривиальный код геттера, Gradle перехватывает вызов этого геттера и анализирует возвращаемое значение.
     * Если оно null, то gradle попытается взять значение для свойства "excludedConfigurations" из getConventionMapping().
     *
     * @return список исключенных из проверки конфигураций
     */
    @Nullable
    List<String> getExcludedConfigurations() {
        return excludedConfigurations;
    }

    /**
     * Задает список конфигураций, которые не должны проверяться плагином.
     *
     * @param excludedConfigurations список исключаемых конфигураций
     */
    void setExcludedConfigurations(List<String> excludedConfigurations) {
        this.excludedConfigurations = new ArrayList<>(excludedConfigurations);
    }

    private void loadExclusionsRules(@Nonnull ExclusionRulesLoader loader) {
        List<String> exclusionsSources = getExclusionsRulesSources();
        if (exclusionsSources != null) {
            loader.load(getProject(), exclusionsSources);
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
        Map<String, Set<String>> requestedLibraries = getRequestedLibraries(configuration);
        return calculateConflictedLibraries(fixedLibraries, requestedLibraries);
    }

    /**
     * Возвращает набор всех запрашиваемых (Прямые и транзитивные зависимости) библиотек в проекте для указанной
     * конфигурации до работы ResolutionStrategy.
     *
     * @param configuration конфигурация сборки
     * @return словарь: ключ - название библиотеки, значение - список всех найденных версий (до работы ResolutionStrategy)
     */
    private static Map<String, Set<String>> getRequestedLibraries(@Nonnull Configuration configuration) {
        Set<? extends DependencyResult> projectDependencies = configuration.getIncoming().getResolutionResult().getAllDependencies();
        Map<String, Set<String>> requestedLibraries = new HashMap<>();

        for (DependencyResult dependency : projectDependencies) {
            ComponentSelector selector = dependency.getRequested();
            if (selector instanceof ModuleComponentSelector) {
                ModuleComponentSelector targetLibrary = (ModuleComponentSelector) selector;
                String selectedLibrary = String.format("%s:%s", targetLibrary.getGroup(), targetLibrary.getModule());
                String selectedVersion = targetLibrary.getVersion();

                requestedLibraries.computeIfAbsent(selectedLibrary, key -> new HashSet<>()).add(selectedVersion);
            }
        }

        return requestedLibraries;
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
        return dependencyManagementExtension.getManagedVersionsForConfigurationHierarchy(configuration);
    }

    /**
     * Анализирует версии проектных библиотек и сравнивает их с со списком фиксированных версий библиотек.
     *
     * @param fixedLibraries   список библиотек с зафиксированной версией
     * @param projectLibraries список проектных библиотек
     * @return список библиотек с конфликтом версий
     */
    private List<ConflictedLibraryInfo> calculateConflictedLibraries(@Nonnull Map<String, String> fixedLibraries,
                                                                     @Nonnull Map<String, Set<String>> projectLibraries) {
        List<ConflictedLibraryInfo> conflictedLibraries = new ArrayList<>();

        projectLibraries.forEach((library, versions) -> {
            String fixedVersion = fixedLibraries.get(library);

            if (fixedVersion == null) {
                return;
            }

            versions.forEach(requestedVersion -> {
                if (!Objects.equals(requestedVersion, fixedVersion)) {
                    if (conflictVersionsResolver.checkChangingLibraryVersion(library, requestedVersion, fixedVersion)) {
                        log.info("Approved changing version {} : {} -> {}", library, requestedVersion, fixedVersion);
                    } else {
                        conflictedLibraries.add(new ConflictedLibraryInfo(library, requestedVersion, fixedVersion));
                    }
                    staleExclusionsDetector.registerActualConflict(library, requestedVersion, fixedVersion);
                }
            });
        });
        return conflictedLibraries;
    }
}