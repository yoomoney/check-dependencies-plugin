package ru.yandex.money.gradle.plugins.library.dependencies.exclusions;

import org.gradle.api.Project;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 20.03.2017
 */
public class ExclusionRulesLoader {
    private final ExclusionsRulesStorage localExclusionRules = new ExclusionsRulesStorage();
    private final ExclusionsRulesStorage totalExclusionRules = new ExclusionsRulesStorage();

    public ExclusionsRulesStorage getLocalExclusionRules() {
        return localExclusionRules;
    }

    public ExclusionsRulesStorage getTotalExclusionRules() {
        return totalExclusionRules;
    }

    public void load(final Project project, final List<String> exclusionsSources) {
        for (String exclusionSource: exclusionsSources) {
            load(project, exclusionSource);
        }
    }

    private void load(final Project project, final String exclusionSource) {
        if (isMavenArtifact(exclusionSource)) {
            ExclusionsRulesPropertiesReader reader = new ExclusionsRulesPackageReader(project, exclusionSource, "libraries-versions-exclusions.properties");
            reader.loadTo(totalExclusionRules);
        }
        else {
            ExclusionsRulesPropertiesReader reader = new ExclusionsRulesFileReader(exclusionSource);
            reader.loadTo(localExclusionRules);
            reader.loadTo(totalExclusionRules);
        }

    }

    private static boolean isMavenArtifact(@Nonnull String name) {
        return name.contains(":");
    }
}
