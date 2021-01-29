package ru.yoomoney.gradle.plugins.library.dependencies.reporters;

import ru.yoomoney.gradle.plugins.library.dependencies.dsl.ArtifactName;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.LibraryName;

/**
 * Класс для получения форматированного имени для библиотек и артефактов
 *
 * @author Konstantin Novokreshchenov
 * @since 29.03.2017
 */
public class NameFormatter {
    public static String format(LibraryName libraryName) {
        return String.format("%s:%s", libraryName.getGroup(), libraryName.getName());
    }

    static String format(ArtifactName artifactName) {
        return String.format("%s:%s", format(artifactName.getLibraryName()), artifactName.getVersion());
    }
}
