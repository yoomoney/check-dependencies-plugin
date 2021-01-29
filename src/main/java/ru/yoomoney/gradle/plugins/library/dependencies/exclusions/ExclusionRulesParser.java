package ru.yoomoney.gradle.plugins.library.dependencies.exclusions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.LibraryName;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Преобразует строку из файла с правилами исключений в совокупность правил исключений {@link ExclusionRule}
 *
 * @author Konstantin Novokreshchenov
 * @since 13.04.2017
 */
class ExclusionRulesParser {
    private final Logger log = LoggerFactory.getLogger(ExclusionRulesParser.class);
    private static final Pattern EXCLUSION_RULE_PATTERN = Pattern.compile("(?<library>.+)=(?<requestedVersions>.+)->(?<targetVersion>.+)");

    /**
     * Преобразует правило исключений для библиотеки, представленное в виде строки, в совокупность правил исключений
     * Формат записи правила:
     * <p>
     * {@code
     * com.fasterxml.jackson.core.jackson-annotations = 2.4.3, 2.4.5, 2.6.0 -> 2.6.5
     * commons-digester:commons-digester = 1.8.1 -> 2.1
     * }
     * <p>
     * В имени библиотеки в качестве разделителя группы и имени артефакта можно использовать как точку, так и двоеточие.
     * Использование двоеточия считается предпочтительным и проверяется в первую очередь.
     * Если при чтении правила в имени библиотеки двоеточия не обнаруживается, то считается,
     * что название после последней точки - название артефакта.
     *
     * @param exclusionRules правила исключений для одной библиотеки в виде строки
     * @return набор правил исключений
     */
    @SuppressFBWarnings("CRLF_INJECTION_LOGS")
    Set<ExclusionRule> parseFrom(String exclusionRules) {
        Matcher matcher = EXCLUSION_RULE_PATTERN.matcher(exclusionRules);
        if (!matcher.matches()) {
            log.warn("Wrong value format of versions rule: {}", exclusionRules);
            return Collections.emptySet();
        }

        LibraryName library = parseLibraryName(matcher.group("library"));
        if (library == null) {
            log.warn("Wrong key format of library name. library={}", matcher.group("library"));
            return Collections.emptySet();
        }

        String[] requestedVersions = matcher.group("requestedVersions").split(",");
        String targetVersion = matcher.group("targetVersion");

        return Arrays.stream(requestedVersions)
                     .map(requestedVersion -> new ExclusionRule(library, requestedVersion, targetVersion))
                     .collect(Collectors.toSet());
    }

    @Nullable
    private LibraryName parseLibraryName(@Nonnull String library) {
        try {
            return LibraryName.parse(library);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to parse library name as <group>:<name>. Try to parse as <group>.<name>. library={}", library);
        }

        int artifactIndex = library.lastIndexOf('.');
        if (artifactIndex == -1) {
            return null;
        }

        String group = library.substring(0, artifactIndex);
        String artifact = library.substring(artifactIndex + 1);

        return new LibraryName(group, artifact);
    }
}
