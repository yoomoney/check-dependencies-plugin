package ru.yoomoney.gradle.plugins.library.dependencies.dsl;

/**
 * Интерфейс для регистрации обнаруженных конфликтов версий
 *
 * @author Konstantin Novokreshchenov
 * @since 25.03.2017
 */
public interface ConflictRegister {
    /**
     * Регистрирует обнаруженный конфликт
     *
     * @param requestedArtifact первоначально запрошенный артефакт
     * @param fixedVersion конечная версия
     */
    void registerConflict(ArtifactName requestedArtifact, String fixedVersion);
}
