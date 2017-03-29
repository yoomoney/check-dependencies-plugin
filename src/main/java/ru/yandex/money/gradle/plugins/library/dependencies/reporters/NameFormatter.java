package ru.yandex.money.gradle.plugins.library.dependencies.reporters;

import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactName;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.LibraryName;

/**
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 29.03.2017
 */
public class NameFormatter {
    private NameFormatter() { }

    public static String format(LibraryName libraryName) {
        return String.format("%s:%s", libraryName.getGroup(), libraryName.getName());
    }

    public static String format(ArtifactName artifactName) {
        return String.format("%s:%s", format(artifactName.getLibraryName()), artifactName.getVersion());
    }
}
