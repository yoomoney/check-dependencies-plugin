package ru.yandex.money.gradle.plugins.library.dependencies.exclusions;

import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactName;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.LibraryName;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Содержит правила исключения, допускающие изменение версий библиотек. Для любой библиотеки можно указать, с каких версий
 * допускается замена версий на указанную.
 *
 * @author Brovin Yaroslav (brovin@yamoney.ru)
 * @since 08.02.2017
 */
public class ExclusionsRulesStorage {

    /**
     * Содержит правила разрешающие изменение версий библиотек.
     * <p>
     * Ключ - название библиотеки с конечной (фиксированной) версией, Значение - набор версий с которых разрешено обновление до
     * фиксированной.
     */
    private final Map<ArtifactName, Set<String>> rules = new HashMap<>();

    /**
     * Регистрирует правила исключений
     *
     * @param exclusionRules набор правил исключений
     */
    void registerExclusionRules(@Nonnull Set<ExclusionRule> exclusionRules) {
        exclusionRules.forEach(this::registerExclusionRule);
    }

    /**
     * Регистрирует правило исключений
     *
     * @param exclusionRule правило исключений
     */
    private void registerExclusionRule(@Nonnull ExclusionRule exclusionRule) {
        ArtifactName targetArtifactName = new ArtifactName(exclusionRule.getLibrary(), exclusionRule.getFixedVersion());
        rules.computeIfAbsent(targetArtifactName, version -> new HashSet<>()).add(exclusionRule.getRequestedVersion());
    }

    /**
     * Возвращает список версий библиотеки <i>requestedLibrary</i>, разрешенных к изменению до <i>targetVersion</i>
     *
     * @param requestedLibrary запрашиваемая библиотека
     * @param targetVersion    конечная (зафиксированная) версия
     * @return список версий, разрешеных к изменению до <i>targetVersion</i>
     */
    public Set<String> getAllowedRequestedVersions(@Nonnull LibraryName requestedLibrary, @Nonnull String targetVersion) {
        ArtifactName requestedArtifactName = new ArtifactName(requestedLibrary, targetVersion);
        return rules.get(requestedArtifactName);
    }

    /**
     * Возвращает набор правил исключений
     *
     * @return набор правил исключений
     */
    List<ExclusionRule> getExclusionRules() {
        return rules.entrySet()
                    .stream()
                    .flatMap(entry -> getExclusionRules(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
    }

    private Stream<ExclusionRule> getExclusionRules(@Nonnull ArtifactName artifact, @Nonnull Set<String> allowedRequestedVersion) {
        LibraryName library = artifact.getLibraryName();
        String fixedVersion = artifact.getVersion();

        return allowedRequestedVersion.stream().map(version -> new ExclusionRule(library, version, fixedVersion));
    }

    @Override
    public String toString() {
        return rules.toString();
    }
}
