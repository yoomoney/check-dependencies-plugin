package ru.yandex.money.gradle.plugins.library.dependencies.analysis;

import org.gradle.api.artifacts.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.*;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;

/**
 * Анализирует заданную конфигурацию проекта на наличие конфликтов версиий между требуемыми и фиксированными версиями библиотек
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 25.03.2017
 */
public class ConfigurationConflictsAnalyzer {
    private final Logger log = LoggerFactory.getLogger(ConfigurationConflictsAnalyzer.class);

    private final ConfigurationDependencies dependencies;
    private final ArtifactNameSet fixedDependencies;
    private final ConflictVersionsResolver conflictResolver;
    private final ConflictRegister conflictRegister;

    public static ConfigurationConflictsAnalyzer create(@Nonnull FixedDependencies projectFixedDependencies,
                                                        @Nonnull Configuration configuration,
                                                        @Nonnull ConflictVersionsResolver conflictResolver,
                                                        @Nonnull ConflictRegister conflictRegister) {
        ArtifactNameSet configurationFixedDependencies = projectFixedDependencies.forConfiguration(configuration);
        return new ConfigurationConflictsAnalyzer(configuration, configurationFixedDependencies,
                                                  conflictResolver, conflictRegister);
    }

    /**
     * Констурктор класса
     *
     * @param configuration объект конфигурации проекта
     * @param fixedDependencies фиксированные зависимости проекта
     * @param conflictResolver объект, проверяющий допустимость изменения версии
     * @param conflictRegister объект для регистрации обнаруженных конфликтов
     */
    private ConfigurationConflictsAnalyzer(@Nonnull Configuration configuration,
                                           @Nonnull ArtifactNameSet fixedDependencies,
                                           @Nonnull ConflictVersionsResolver conflictResolver,
                                           @Nonnull ConflictRegister conflictRegister) {
        this.dependencies = new ConfigurationDependencies(configuration);
        this.fixedDependencies = fixedDependencies;
        this.conflictResolver = conflictResolver;
        this.conflictRegister = conflictRegister;
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
        for(ArtifactDependency artifact: dependencies.all()) {
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
                log.info("Approved changing version {} : {} -> {}", artifact.getLibraryName(),
                                                                    requestedVersion, fixedVersion);
            } else {
                conflictedLibraries.add(new ConflictedLibraryInfo(artifact, fixedVersion, findDependentPaths(artifact)));
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
        return conflictResolver.checkChangingLibraryVersion(library, version, fixedVersion);
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
}
