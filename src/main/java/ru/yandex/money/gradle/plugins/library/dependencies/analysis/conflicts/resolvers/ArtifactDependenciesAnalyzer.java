package ru.yandex.money.gradle.plugins.library.dependencies.analysis.conflicts.resolvers;

import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactName;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.LibraryName;

/**
 * Интерфейс для проверки наличия у артефакта зависимости от данной библиотеки с версией, отличной от данной
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 25.03.2017
 */
public interface ArtifactDependenciesAnalyzer {
    /**
     * Проверяет, имеет ли данный артефакт в качестве прямой или транзитивной зависимости
     * данную библиотеку с другой версией
     *
     * @param artifact имя артефакта, для которого анализируются зависимости
     * @param dependency имя библиотеки
     * @param version версия библиотеки
     * @return true, если артефакт имеет зависимости от данной библиотеки, но с другой версией, false - иначе
     */
    boolean hasAnotherDependencyVersions(ArtifactName artifact, LibraryName dependency, String version);
}
