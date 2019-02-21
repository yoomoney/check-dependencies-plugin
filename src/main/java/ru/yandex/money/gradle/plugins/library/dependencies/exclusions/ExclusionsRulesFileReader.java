package ru.yandex.money.gradle.plugins.library.dependencies.exclusions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    @Override
    public void loadTo(@Nonnull ExclusionsRulesStorage rulesStorage) {
        Path fileNamePath = Paths.get(fileName);
        if (!Files.isReadable(fileNamePath)) {
            log.warn("Cannot read file \"{}\" with upgrade versions rules.", fileName);
            return;
        }

        try (InputStream fileInputStream = java.nio.file.Files.newInputStream(fileNamePath)) {
            load(rulesStorage, fileInputStream);
        } catch (FileNotFoundException e) {
            log.warn("Cannot find file with upgrade versions rules.", e);
        } catch (IOException e) {
            log.warn("Cannot loadTo file with upgrade versions rules.", e);
        }
    }
}
