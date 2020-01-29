package ru.yandex.money.gradle.plugins.library.dependencies.exclusions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Абстрактный базовый класс читателя правил исключения изменения версий из properties файла. Читает правила из потока
 * с properties файлом.
 *
 * @author Brovin Yaroslav (brovin@yamoney.ru)
 * @since 08.02.2017
 */
abstract class ExclusionsRulesPropertiesReader {
    private final Logger log = LoggerFactory.getLogger(ExclusionsRulesPropertiesReader.class);

    /**
     * Осуществляет чтение и добавление прочитанных правил в <b>storage</b>
     *
     * @param rulesStorage хранилище правил изменения версий библиотек
     */
    public abstract void loadTo(@Nonnull ExclusionsRulesStorage rulesStorage);

    /**
     * Считывает из входного потока правила допустимых изменений версий библиотек
     * и сохраняет их в переданное хранилище правил исключений
     */
    void load(@Nonnull ExclusionsRulesStorage rulesStorage, @Nonnull InputStream exclusionRulesInputStream) {
        ExclusionRulesParser exclusionRulesParser = new ExclusionRulesParser();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exclusionRulesInputStream, UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("\\s+", "");
                if (!isNullOrEmpty(line) && !isComment(line)) {
                    Set<ExclusionRule> rules = exclusionRulesParser.parseFrom(line);
                    rulesStorage.registerExclusionRules(rules);
                }
            }
        } catch (IOException e) {
            log.warn("Cannot load dependencies resolutions rules", e);
        }
    }

    private static boolean isComment(String line) {
        return line.startsWith("#");
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }
}
