package ru.yandex.money.gradle.plugins.library.dependencies.exclusions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Set;

/**
 * Читает правила исключения изменения версий из properties файла
 *
 * @author Brovin Yaroslav (brovin@yamoney.ru)
 * @since 08.02.2017
 */
public class ExclusionsRulesPropertiesReader {

    private final Logger log = LoggerFactory.getLogger(ExclusionsRulesPropertiesReader.class);

    private final ExclusionsRulesStorage rulesStorage;

    /**
     * Считывает из указанного properties файла разрешающие правила изменения версий библиотек.
     *
     * @param fileName имя файла с правилами
     */
    public ExclusionsRulesPropertiesReader(@Nonnull String fileName) {
        rulesStorage = new ExclusionsRulesStorage();

        if (!Files.isReadable(Paths.get(fileName))) {
            log.warn(String.format("Cannot read file \"%s\" with upgrade versions rules.", fileName));
            return;
        }

        try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
            load(rulesStorage, fileInputStream);
        } catch (FileNotFoundException e) {
            log.warn("Cannot find file with upgrade versions rules.", e);
        } catch (IOException e) {
            log.warn("Cannot load file with upgrade versions rules.", e);
        }
    }

    /**
     * Считывает из входного потока правила допустимых изменений версий библиотек.
     * Формат записи правила:
     * <p>
     * {@code
     * com.fasterxml.jackson.core.jackson-annotations = 2.4.3, 2.4.5, 2.6.0 -> 2.6.5
     * commons-digester.commons-digester = 1.8.1 -> 2.1
     * }
     * <p>
     * Property файл не позволяет использовать двоеточие в названии ключа, поэтому используем точку, как разделитель группы и
     * названия артефакта. При чтении правил, считается, что название после последней точки - название артефакта.
     *
     * @param inputStream входной поток с правилами
     */
    private void load(@Nonnull ExclusionsRulesStorage storage, @Nonnull InputStream inputStream) {
        Properties property = new Properties();
        try {
            property.load(inputStream);
        } catch (IOException e) {
            log.warn("Cannot load of dependencies resolutions rules", e);
        }
        Set<String> libraries = property.stringPropertyNames();

        for (String library : libraries) {
            String value = property.getProperty(library).replace(" ", "");
            String[] versionsRule = value.split("->");
            if (versionsRule.length == 2) {
                String requestedVersions = versionsRule[0];
                String targetVersion = versionsRule[1];
                storage.registerAllowedVersionsChanges(library, requestedVersions.split(","), targetVersion);
            } else {
                log.warn("Wrong value format of versions rule for {}: {}", library, value);
            }
        }
    }

    public ExclusionsRulesStorage getRulesStorage() {
        return rulesStorage;
    }

}
