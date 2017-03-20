package ru.yandex.money.gradle.plugins.library.dependencies.reporters;

import ru.yandex.money.gradle.plugins.library.dependencies.exclusions.ExclusionRule;

import java.util.Collection;

/**
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 19.03.2017
 */
public class StaleExclusionsReporter {
    private static final String HEADER = "There are some stale exclusions.";
    private final Collection<String> messages;

    public StaleExclusionsReporter(Collection<String> messages) {
        this.messages = messages;
    }

    public void report(Iterable<ExclusionRule> exclusionRules) {
        messages.add(HEADER);
        exclusionRules.forEach(exclusionRule -> messages.add(formatExclusionRule(exclusionRule)));
    }

    private static String formatExclusionRule(ExclusionRule exclusionRule) {
        return String.format("   --- %-50s: %s -> %s", exclusionRule.getLibrary(),
                                                       exclusionRule.getRequestedVersion(),
                                                       exclusionRule.getFixedVersion());
    }
}
