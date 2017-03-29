package ru.yandex.money.gradle.plugins.library.dependencies.exclusions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Читатель правил исключения изменения версий из properties файла локальной файловой системы.
 *
 * @author Brovin Yaroslav (brovin@yamoney.ru)
 * @since 10.02.2017
 */
public class ExclusionsRulesFileReader extends ExclusionsRulesPropertiesReader {

    private final Logger log = LoggerFactory.getLogger(ExclusionsRulesFileReader.class);
    private final String fileName;

    ExclusionsRulesFileReader(@Nonnull String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void loadTo(@Nonnull ExclusionsRulesStorage rulesStorage) {
        if (!Files.isReadable(Paths.get(fileName))) {
            log.warn("Cannot read file \"{}\" with upgrade versions rules.", fileName);
            return;
        }

        try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
            load(rulesStorage, fileInputStream);
        } catch (FileNotFoundException e) {
            log.warn("Cannot find file with upgrade versions rules.", e);
        } catch (IOException e) {
            log.warn("Cannot loadTo file with upgrade versions rules.", e);
        }
    }
}
