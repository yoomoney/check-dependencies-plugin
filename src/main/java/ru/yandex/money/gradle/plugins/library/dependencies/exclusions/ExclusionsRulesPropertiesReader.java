package ru.yandex.money.gradle.plugins.library.dependencies.exclusions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.LibraryName;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

/**
 * Абстрактный базовый класс читателя правил исключения изменения версий из properties файла. Читает правила из потока
 * с properties файлом.
 *
 * @author Brovin Yaroslav (brovin@yamoney.ru)
 * @since 08.02.2017
 */
public abstract class ExclusionsRulesPropertiesReader {

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
     * commons-digester.commons-digester = 1.8.1 -> 2.1
     * }
     * <p>
     * Property файл не позволяет использовать двоеточие в названии ключа, поэтому используем точку, как разделитель группы и
     * названия артефакта. При чтении правил, считается, что название после последней точки - название артефакта.
     *
     * @param rulesStorage хранилище правил изменения версий библиотек
     * @param inputStream  входной поток с правилами
     */
    void load(@Nonnull ExclusionsRulesStorage rulesStorage, @Nonnull InputStream inputStream) {
        Properties property = new Properties();
        try {
            property.load(inputStream);
        } catch (IOException e) {
            log.warn("Cannot loadTo of dependencies resolutions rules", e);
        }
        Set<String> libraries = property.stringPropertyNames();

        for (String library : libraries) {
            String value = property.getProperty(library).replace(" ", "");
            String[] versionsRule = value.split("->");
            if (versionsRule.length == 2) {
                String requestedVersions = versionsRule[0];
                String targetVersion = versionsRule[1];
                registerAllowedVersionsChanges(rulesStorage, library, requestedVersions.split(","), targetVersion);
            } else {
                log.warn("Wrong value format of versions rule for {}: {}", library, value);
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
        int artifactIndex = library.lastIndexOf('.');
        if (artifactIndex == -1) {
            log.warn("Wrong key format of library name. library={}", library);
            return null;
        }

        String group = library.substring(0, artifactIndex);
        String artifact = library.substring(artifactIndex + 1);

        return new LibraryName(group, artifact);
    }
}
