package ru.yoomoney.gradle.plugins.library.dependencies.repositories;

import ru.yoomoney.gradle.plugins.library.dependencies.dsl.ArtifactName;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.LibraryName;

import java.util.List;

/**
 * Интерфейс для получения информации об артефактах, хранящихся в репозитории.
 * Предоставляет возможность получить для библиотеки по её имени все доступные версии
 * и прямые зависимости доступных артефактов
 *
 * @author Konstantin Novokreshchenov
 * @since 15.03.2017
 */
public interface Repository {
    List<String> findVersions(LibraryName libraryName);

    List<ArtifactName> findDirectDependencies(ArtifactName artifactName);
}