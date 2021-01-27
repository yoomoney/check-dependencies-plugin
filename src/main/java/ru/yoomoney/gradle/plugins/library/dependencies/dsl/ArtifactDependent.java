package ru.yoomoney.gradle.plugins.library.dependencies.dsl;

import java.util.List;

/**
 * Описывает сущность, которая имеет зависимости от артефактов.
 * Примерами таких сущностей является проект, конфигурация, артефакт.
 *
 * @param <ArtifactT> тип артефакта
 * @author Konstantin Novokreshchenov
 * @since 15.03.2017
 */
public interface ArtifactDependent<ArtifactT extends Artifact<ArtifactT>> {
    /**
     * Возвращает список зависимостей
     *
     * @return список зависимостей
     */
    List<ArtifactT> getDependencies();
}
