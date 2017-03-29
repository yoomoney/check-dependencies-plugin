package ru.yandex.money.gradle.plugins.library.dependencies.exclusions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Logger log = LoggerFactory.getLogger(ExclusionsRulesStorage.class);

    /**
     * Содержит правила разрешающие изменение версий библиотек.
     * <p>
     * Ключ - название библиотеки с конечной (фиксированной) версией, Значение - набор версий с которых разрешено обновление до
     * фиксированной.
     */
    private final Map<String, Set<String>> rules = new HashMap<>();

    /**
     * Регистрирует правила перехода библиотеки <i>library</i> с версий <i>fromVersions</i> до версии <i>toVersion</i>
     *
     * @param library      название библиотеки
     * @param fromVersions массив запрашиваемых версий
     * @param toVersion    конечная (фиксированная) версия
     */
    void registerAllowedVersionsChanges(@Nonnull String library, @Nonnull String[] fromVersions, String toVersion) {
        for (String fromVersion : fromVersions) {
            registerAllowedVersionChange(library, fromVersion, toVersion);
        }
    }

    /**
     * Регистрирует правило перехода библиотеки <i>library</i> версии <i>fromVersion</i> до версии <i>toVersion</i>
     *
     * @param library     название библиотеки
     * @param fromVersion запрашиваемая версия
     * @param toVersion   конечная (фиксированная) версия
     */
    private void registerAllowedVersionChange(@Nonnull String library, @Nonnull String fromVersion, @Nonnull String toVersion) {
        int artifactIndex = library.lastIndexOf('.');
        if (artifactIndex == -1) {
            log.warn("Wrong key format of library name. library={}", library);
            return;
        }
        String group = library.substring(0, artifactIndex);
        String artifact = library.substring(artifactIndex + 1);
        String libraryId = String.format("%s:%s:%s", group, artifact, toVersion);

        rules.computeIfAbsent(libraryId, version -> new HashSet<>()).add(fromVersion);
    }

    /**
     * Возвращает список версий библиотеки <i>requestedLibrary</i>, разрешенных к изменению до <i>targetVersion</i>
     *
     * @param requestedLibrary запрашиваемая библиотека
     * @param targetVersion    конечная (зафиксированная) версия
     * @return список версий, разрешеных к изменению до <i>targetVersion</i>
     */
    public Set<String> getAllowedRequestedVersions(@Nonnull String requestedLibrary, @Nonnull String targetVersion) {
        return rules.get(String.format("%s:%s", requestedLibrary, targetVersion));
    }

    /**
     * Возвращает набор правил исключений
     *
     * @return набор правил исключений
     */
    public List<ExclusionRule> getExclusionRules() {
        return rules.entrySet()
                    .stream()
                    .flatMap(entry -> getExclusionRules(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
    }

    private Stream<ExclusionRule> getExclusionRules(@Nonnull String artifact, @Nonnull Set<String> allowedRequestedVersion) {
        int libraryLength = artifact.lastIndexOf(':');
        String library = artifact.substring(0, libraryLength);
        String fixedVersion = artifact.substring(libraryLength + 1);

        return allowedRequestedVersion.stream().map(version -> new ExclusionRule(library, version, fixedVersion));
    }

    @Override
    public String toString() {
        return rules.toString();
    }
}
