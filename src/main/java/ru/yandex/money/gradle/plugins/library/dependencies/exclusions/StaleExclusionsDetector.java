package ru.yandex.money.gradle.plugins.library.dependencies.exclusions;

import java.util.List;

/**
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 19.03.2017
 */
public class StaleExclusionsDetector {
    private final List<ExclusionRule> staleExclusionRules;

    public static StaleExclusionsDetector create(ExclusionsRulesStorage rulesStorage) {
        List<ExclusionRule> exclusionRules = rulesStorage.getExclusionRules();
        return new StaleExclusionsDetector(exclusionRules);
    }

    private StaleExclusionsDetector(List<ExclusionRule> staleExclusionRules) {
        this.staleExclusionRules = staleExclusionRules;
    }

    public void registerActualConflict(String library, String requestedVersion, String fixedVersion) {
        staleExclusionRules.remove(new ExclusionRule(library, requestedVersion, fixedVersion));
    }

    public boolean hasStaleExclusions() {
        return staleExclusionRules.size() > 0;
    }

    public Iterable<ExclusionRule> getStaleExclusions() {
        return staleExclusionRules;
    }
}
