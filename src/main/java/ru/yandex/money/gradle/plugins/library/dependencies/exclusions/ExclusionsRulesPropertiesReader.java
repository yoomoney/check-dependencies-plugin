package ru.yandex.money.gradle.plugins.library.dependencies.exclusions;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.LibraryName;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Абстрактный базовый класс читателя правил исключения изменения версий из properties файла. Читает правила из потока
 * с properties файлом.
 *
 * @author Brovin Yaroslav (brovin@yamoney.ru)
 * @since 08.02.2017
 */
public abstract class ExclusionsRulesPropertiesReader {
    private static final Pattern EXCLUSION_RULE_PATTERN = Pattern.compile("(?<library>.+)=(?<requestedVersions>.+)->(?<targetVersion>.+)");
    private final Logger log = LoggerFactory.getLogger(ExclusionsRulesPropertiesReader.class);

    /**
     * Осуществляет чтение и добавление прочитанных правил в <b>storage</b>
     *
     * @param rulesStorage хранилище правил изменения версий библиотек
     */
    public abstract void loadTo(@Nonnull ExclusionsRulesStorage rulesStorage);

    /**
     * Считывает из входного потока правила допустимых изменений версий библиотек.
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
     * @param rulesStorage хранилище правил изменения версий библиотек
     * @param inputStream  входной поток с правилами
     */
    void load(@Nonnull ExclusionsRulesStorage rulesStorage, @Nonnull InputStream inputStream) {
        Set<String> exclusionRules = readExclusionRules(inputStream);

        for (String exclusionRule : exclusionRules) {
            Matcher matcher = EXCLUSION_RULE_PATTERN.matcher(exclusionRule);
            if (matcher.matches()) {
                String library = matcher.group("library");
                String requestedVersions = matcher.group("requestedVersions");
                String targetVersion = matcher.group("targetVersion");
                registerAllowedVersionsChanges(rulesStorage, library, requestedVersions.split(","), targetVersion);
            } else {
                log.warn("Wrong value format of versions rule: {}", exclusionRule);
            }
        }
    }

    private void registerAllowedVersionsChanges(@Nonnull ExclusionsRulesStorage rulesStorage,
                                                @Nonnull String library, @Nonnull String[] fromVersions, String toVersion) {
        LibraryName libraryName = parseLibraryName(library);
        if (libraryName != null) {
            rulesStorage.registerAllowedVersionsChanges(libraryName, fromVersions, toVersion);
        }
    }

    @Nullable
    private LibraryName parseLibraryName(@Nonnull String library) {
        try {
            return LibraryName.parse(library);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to parse library name '{}' as <group>:<name>. Try to parse as <group>.<name>...", library);
        }

        int artifactIndex = library.lastIndexOf('.');
        if (artifactIndex == -1) {
            log.warn("Wrong key format of library name. library={}", library);
            return null;
        }

        String group = library.substring(0, artifactIndex);
        String artifact = library.substring(artifactIndex + 1);

        return new LibraryName(group, artifact);
    }

    private Set<String> readExclusionRules(InputStream inputStream) {
        Set<String> exclusionRules = new HashSet<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, UTF_8));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("\\s+", "");
                if (!Strings.isNullOrEmpty(line) && !line.startsWith("#")) {
                    exclusionRules.add(line);
                }
            }
        } catch (IOException e) {
            log.warn("Cannot loadTo of dependencies resolutions rules", e);
        }
        return exclusionRules;
    }
}
