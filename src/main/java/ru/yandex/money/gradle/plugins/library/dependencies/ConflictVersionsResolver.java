package ru.yandex.money.gradle.plugins.library.dependencies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Проверяет корректность изменений версий библиотек с запрашиваемых на фиксированную. Позволяет проверить, разрешено ли
 * изменение запрашиваемой версии библиотеки до версии, зафиксированной <c>Spring Dependency Management Plugin</c>, или нет.
 *
 * @author Brovin Yaroslav (brovin@yamoney.ru)
 * @since 03.02.2017
 */
class ConflictVersionsResolver {

    private final Logger log = LoggerFactory.getLogger(ConflictVersionsResolver.class);

    /**
     * Содержит правила разрешающие изменение версий библиотек.
     * <p>
     * Ключ - название библиотеки с конечной (фиксированной) версией, Значение - набор версий с которых разрешено обновление до
     * фиксированной.
     */
    private final Map<String, Set<String>> rules = new HashMap<>();

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
    public void load(@Nonnull InputStream inputStream) {
        Properties property = new Properties();
        try {
            rules.clear();
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
                registerRules(library, requestedVersions.split(","), targetVersion);
            } else {
                log.warn("Wrong value format of versions rule for {}: {}", library, value);
            }
        }
    }

    /**
     * Регистрирует правила перехода библиотеки <c>library</c> с версий <c>fromVersions</c> до версии <c>toVersion</c>
     *
     * @param library      название библиотеки
     * @param fromVersions массив запрашиваемых версий
     * @param toVersion    конечная (фиксированная) версия
     */
    private void registerRules(@Nonnull String library, @Nonnull String[] fromVersions, String toVersion) {
        for (String fromVersion : fromVersions) {
            registerRule(library, fromVersion, toVersion);
        }
    }

    /**
     * Регистрирует правило перехода библиотеки <c>library</c> версии <c>fromVersion</c> до версии <c>toVersion</c>
     *
     * @param library     название библиотеки
     * @param fromVersion запрашиваемая версия
     * @param toVersion   конечная (фиксированная) версия
     */
    private void registerRule(@Nonnull String library, @Nonnull String fromVersion, String toVersion) {
        int artifactIndex = library.lastIndexOf('.');
        if (artifactIndex == -1) {
            log.warn("Wrong key format of library name. library={}", library);
            return;
        }
        String group = library.substring(0, artifactIndex);
        String artifact = library.substring(artifactIndex + 1);
        String libraryID = String.format("%s:%s:%s", group, artifact, toVersion);

        rules.computeIfAbsent(libraryID, version -> new HashSet<>()).add(fromVersion);
    }

    /**
     * Разрешено ли изменение версии указанной библиотеки <c>requestedLibrary</c> с версии <c>requestedVersion</c> до
     * <c>targetVersion</c>
     *
     * @param requestedLibrary проверяемая библиотека. Название состоит из: Имя группы + "." + имя артефакта
     * @param requestedVersion запрашиваемая версия
     * @param targetVersion    конечная (фиксированная) версия
     * @return true, если есть правило перехода с указанной версии на конечную, false - в противном случае.
     */
    boolean checkChangingLibraryVersion(@Nonnull String requestedLibrary, String requestedVersion, String targetVersion) {
        Set<String> versions = rules.get(String.format("%s:%s", requestedLibrary, targetVersion));
        return versions != null && versions.contains(requestedVersion);
    }

    @Override
    public String toString() {
        return rules.toString();
    }
}
