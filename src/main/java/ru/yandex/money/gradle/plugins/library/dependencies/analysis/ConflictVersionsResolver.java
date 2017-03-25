package ru.yandex.money.gradle.plugins.library.dependencies.analysis;

import ru.yandex.money.gradle.plugins.library.dependencies.dsl.LibraryName;
import ru.yandex.money.gradle.plugins.library.dependencies.exclusions.ExclusionsRulesStorage;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Проверяет корректность изменений версий библиотек с запрашиваемых на фиксированную. Позволяет проверить, разрешено ли
 * изменение запрашиваемой версии библиотеки до версии, зафиксированной <c>Spring Dependency Management Plugin</c>, или нет.
 *
 * @author Brovin Yaroslav (brovin@yamoney.ru)
 * @since 03.02.2017
 */
public class ConflictVersionsResolver {
    /**
     * Хранилище правил исключений изменения версий библиотек
     */
    private final ExclusionsRulesStorage rulesStorage;

    public ConflictVersionsResolver(@Nonnull ExclusionsRulesStorage storage) {
        this.rulesStorage = storage;
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
    boolean checkChangingLibraryVersion(@Nonnull LibraryName requestedLibrary, String requestedVersion, String targetVersion) {
        Set<String> versions = rulesStorage.getAllowedRequestedVersions(requestedLibrary, targetVersion);
        return versions != null && versions.contains(requestedVersion);
    }
}
