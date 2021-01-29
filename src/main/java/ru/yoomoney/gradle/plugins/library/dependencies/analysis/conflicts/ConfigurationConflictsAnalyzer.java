package ru.yoomoney.gradle.plugins.library.dependencies.analysis.conflicts;

import org.gradle.api.artifacts.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yoomoney.gradle.plugins.library.dependencies.analysis.ArtifactDependentPathsFinder;
import ru.yoomoney.gradle.plugins.library.dependencies.analysis.ConfigurationDependencies;
import ru.yoomoney.gradle.plugins.library.dependencies.analysis.FixedDependencies;
import ru.yoomoney.gradle.plugins.library.dependencies.analysis.conflicts.resolvers.VersionConflictInfo;
import ru.yoomoney.gradle.plugins.library.dependencies.analysis.conflicts.resolvers.VersionConflictResolver;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.ArtifactDependency;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.ArtifactDependent;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.ArtifactName;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.ArtifactNameSet;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.ConflictRegister;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.DependencyPath;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.LibraryName;
import ru.yoomoney.gradle.plugins.library.dependencies.reporters.NameFormatter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Анализирует заданную конфигурацию проекта на наличие конфликтов версиий между требуемыми и фиксированными версиями библиотек
 *
 * @author Konstantin Novokreshchenov
 * @since 25.03.2017
 */
public class ConfigurationConflictsAnalyzer {
    private final Logger log = LoggerFactory.getLogger(ConfigurationConflictsAnalyzer.class);

    private final ConfigurationDependencies dependencies;
    private final ArtifactNameSet fixedDependencies;
    private final ConflictVersionsChecker conflictChecker;
    private final ConflictRegister conflictRegister;
    private final VersionConflictResolver conflictResolver;

    /**
     * Фабричный метод для создания объекта класса.
     *
     * @param projectFixedDependencies зависимости проекта, указанные в dependencyManagement секции
     * @param configuration проверяемая конфигурация проекта
     * @param conflictResolver объект, проверяющий допустимость конфликта
     * @param conflictRegister объект для регистрации обнаруженных конфликтов
     * @return новый объект класса для анализа конфиктов в конфигурации
     */
    public static ConfigurationConflictsAnalyzer create(@Nonnull FixedDependencies projectFixedDependencies,
                                                        @Nonnull Configuration configuration,
                                                        @Nonnull ConflictVersionsChecker conflictChecker,
                                                        @Nonnull ConflictRegister conflictRegister,
                                                        @Nonnull VersionConflictResolver conflictResolver) {
        ArtifactNameSet configurationFixedDependencies = projectFixedDependencies.forConfiguration(configuration);
        return new ConfigurationConflictsAnalyzer(configuration, configurationFixedDependencies,
                                                  conflictChecker, conflictRegister, conflictResolver);
    }

    /**
     * Констурктор класса
     *
     * @param configuration объект конфигурации проекта
     * @param fixedDependencies фиксированные зависимости проекта
     * @param conflictChecker объект, проверяющий допустимость изменения версии
     * @param conflictRegister объект для регистрации обнаруженных конфликтов
     */
    private ConfigurationConflictsAnalyzer(@Nonnull Configuration configuration,
                                           @Nonnull ArtifactNameSet fixedDependencies,
                                           @Nonnull ConflictVersionsChecker conflictChecker,
                                           @Nonnull ConflictRegister conflictRegister,
                                           @Nonnull VersionConflictResolver conflictResolver) {
        this.dependencies = new ConfigurationDependencies(configuration);
        this.fixedDependencies = fixedDependencies;
        this.conflictChecker = conflictChecker;
        this.conflictRegister = conflictRegister;
        this.conflictResolver = conflictResolver;
    }

    /**
     * Ищет недопустимые конфликты версий между запращиваемыми версиями библиотек и фиксированными версиями библиотек
     *
     * @return обнаруженные конфликты версий
     */
    public List<ConflictedLibraryInfo> findConflictedLibraries() {
        ArtifactNameSet requestedDependencies = getRequestedDependencies();
        return calculateConflictedLibraries(fixedDependencies, requestedDependencies);
    }

    /**
     * Возвращает набор имен всех запрашиваемых (прямые и транзитивные зависимости) библиотек до работы ResolutionStrategy
     * для данной конфигурации проекта.
     *
     * @return набор имен запрашиваемых артефактов
     */
    private ArtifactNameSet getRequestedDependencies() {
        Map<LibraryName, Set<String>> requestedLibraryVersions = new HashMap<>();
        for (ArtifactDependency artifact: dependencies.all()) {
            requestedLibraryVersions.computeIfAbsent(artifact.getRequestedLibraryName(), l -> new HashSet<>())
                    .add(artifact.getRequestedVersion());
        }
        return ArtifactNameSet.fromLibraryVersions(requestedLibraryVersions);
    }

    /**
     * Анализирует версии проектных библиотек и сравнивает их с со списком фиксированных версий библиотек.
     *
     * @param fixedDependencies список библиотек с зафиксированной версией
     * @param requestedDependencies список требуемых библиотек для конфигурации проекта
     * @return список библиотек с конфликтом версий
     */
    private List<ConflictedLibraryInfo> calculateConflictedLibraries(@Nonnull ArtifactNameSet fixedDependencies,
                                                                     @Nonnull ArtifactNameSet requestedDependencies) {
        List<ConflictedLibraryInfo> conflictedLibraries = new ArrayList<>();

        requestedDependencies.forEach(artifact -> {
            String requestedVersion = artifact.getVersion();
            String fixedVersion = fixedDependencies.getVersions(artifact.getLibraryName())
                                                   .stream().findFirst().orElse(null);

            if (fixedVersion == null || Objects.equals(fixedVersion, requestedVersion)) {
                return;
            }

            conflictRegister.registerConflict(artifact, fixedVersion);
            if (canSkipConflict(artifact, fixedVersion)) {
                log.info("Approved changing version {} : {} -> {}", NameFormatter.format(artifact.getLibraryName()),
                                                                    requestedVersion, fixedVersion);
            } else {
                conflictedLibraries.add(createConflictInfo(artifact, fixedVersion));
            }
        });

        return conflictedLibraries;
    }

    /**
     * Проверяет, является ли изменение версии допустимым
     *
     * @return true - если конфликт допустим, false - иначе
     */
    private boolean canSkipConflict(ArtifactName requestedArtifact, String fixedVersion) {
        LibraryName library = requestedArtifact.getLibraryName();
        String version = requestedArtifact.getVersion();
        return conflictChecker.checkChangingLibraryVersion(library, version, fixedVersion);
    }

    /**
     * Ищет пути до запрашиваемой зависимости (артефакта) в графе зависимостей для конфигурации проекта
     *
     * @param requestedArtifactName запрашиваемый артефакт
     * @return список всех обнаруженных путей до запрашиваемого артефакта
     */
    private List<DependencyPath<ArtifactDependency>> findDependentPaths(ArtifactName requestedArtifactName) {
        ArtifactDependent<ArtifactDependency> root = dependencies.root();
        Predicate<? super ArtifactName> isRequested = artifactName -> artifactName.equals(requestedArtifactName);
        ArtifactDependentPathsFinder<ArtifactDependency> finder = new ArtifactDependentPathsFinder<>(root, isRequested);
        return finder.findPaths();
    }

    private ConflictedLibraryInfo createConflictInfo(ArtifactName requestedArtifact, String fixedVersion) {
        List<DependencyPath<ArtifactDependency>> conflictPaths = findDependentPaths(requestedArtifact);
        ConflictedLibraryInfo conflictedLibraryInfo = new ConflictedLibraryInfo(requestedArtifact, fixedVersion, conflictPaths);

        conflictPaths.forEach(conflictPath -> {
            VersionConflictInfo versionConflict = new VersionConflictInfo(conflictPath, fixedVersion);
            conflictedLibraryInfo.addConflictPathResolution(conflictResolver.resolveConflict(versionConflict));
        });

        return conflictedLibraryInfo;
    }
}
