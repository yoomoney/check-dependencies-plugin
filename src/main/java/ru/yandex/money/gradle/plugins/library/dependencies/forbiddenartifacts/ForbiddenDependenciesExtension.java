package ru.yandex.money.gradle.plugins.library.dependencies.forbiddenartifacts;

import java.util.HashSet;
import java.util.Set;

/**
 * Класс, позволяющий настраивать проверку запрещенных артефактов
 *
 * @author horyukova
 * @since 26.05.2019
 */
public class ForbiddenDependenciesExtension {
    /**
     * Сет запрещенных артефактов
     */
    public Set<String> forbiddenArtifacts = new HashSet<>();
}
