package ru.yandex.money.gradle.plugins.library.dependencies.exclusions;

import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactName;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ConflictRegister;

import java.util.List;
import java.util.Set;

/**
 * Определяет неиспользуемые локальные правила исключений на основании зарегистрированных конфликтов версий.
 * Исключение считается неиспользуемым, если для него не было зарегистрировано ни одного конфликта
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 19.03.2017
 */
public class StaleExclusionsDetector implements ConflictRegister {
    private final List<ExclusionRule> staleExclusionRules;

    /**
     * Создает объект класса на основании переданного набора исключений
     *
     * @param rulesStorage набор исключений
     * @return объект класса
     */
    public static StaleExclusionsDetector create(ExclusionsRulesStorage rulesStorage) {
        List<ExclusionRule> exclusionRules = rulesStorage.getExclusionRules();
        return new StaleExclusionsDetector(exclusionRules);
    }

    /**
     * Конструктор класса
     *
     * @param staleExclusionRules набор исключений
     */
    private StaleExclusionsDetector(List<ExclusionRule> staleExclusionRules) {
        this.staleExclusionRules = staleExclusionRules;
    }

    /**
     * Регистрирует обнаруженный конфликт версий
     *
     * @param requestedArtifact требуемая библиотека
     * @param fixedVersion зафиксированная версия библиотеки
     */
    @Override
    public void registerConflict(ArtifactName requestedArtifact, String fixedVersion) {
        staleExclusionRules.remove(new ExclusionRule(requestedArtifact, fixedVersion));
    }

    /**
     * Определяет, обнаружены ли неиспользуемые исключения
     *
     * @return true, если обнаружены, false - иначе
     */
    public boolean hasStaleExclusions() {
        return staleExclusionRules.size() > 0;
    }

    /**
     * Возвращает неиспользуемые исключения, определенные в локальных файлах
     *
     * @return итератор по набору неиспользуемых исключений
     */
    public Iterable<ExclusionRule> getStaleExclusions() {
        return staleExclusionRules;
    }
}
