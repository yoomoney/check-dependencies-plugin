package ru.yandex.money.gradle.plugins.library.dependencies.reporters;

import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactName;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.LibraryName;

/**
 * Класс для получения форматированного имени для библиотек и артефактов
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 29.03.2017
 */
class NameFormatter {
    static String format(LibraryName libraryName) {
        return String.format("%s:%s", libraryName.getGroup(), libraryName.getName());
    }

    static String format(ArtifactName artifactName) {
        return String.format("%s:%s", format(artifactName.getLibraryName()), artifactName.getVersion());
    }
}
