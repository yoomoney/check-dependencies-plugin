package ru.yoomoney.gradle.plugins.library.dependencies.exclusions;

import org.gradle.api.Project;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Загрузчик правил-исключений из локального файла и maven-артефакта
 *
 * @author Konstantin Novokreshchenov
 * @since 20.03.2017
 */
public class ExclusionRulesLoader {
    private final ExclusionsRulesStorage localExclusionRules = new ExclusionsRulesStorage();
    private final ExclusionsRulesStorage totalExclusionRules = new ExclusionsRulesStorage();

    /**
     * Возвращает набор правил исключений, определенных в локальных файлах проекта
     *
     * @return набор локальных правил исключений
     */
    public ExclusionsRulesStorage getLocalExclusionRules() {
        return localExclusionRules;
    }

    /**
     * Возвращает полный набор правил-исключений, определенных как в локальных файлах,
     * так и в сторонних maven-артефактах
     *
     * @return полный набор правил исключений
     */
    public ExclusionsRulesStorage getTotalExclusionRules() {
        return totalExclusionRules;
    }

    /**
     * Загружает набор исключений из переданных путей до источников исключений
     *
     * @param project           текущий проект
     * @param exclusionsSources источники исключений: локальные файлы и maven-артефакта
     */
    public void load(final Project project, final List<String> exclusionsSources) {
        for (String exclusionSource : exclusionsSources) {
            load(project, exclusionSource);
        }
    }

    /**
     * Загружает набор правил исключений из данного источника: локального файла или maven-артефакта
     *
     * @param project         текущий проект
     * @param exclusionSource источник исключений
     */
    private void load(final Project project, final String exclusionSource) {
        if (isMavenArtifact(exclusionSource, project)) {
            ExclusionsRulesPropertiesReader reader = new ExclusionsRulesPackageReader(project, exclusionSource,
                    "libraries-versions-exclusions.properties");
            reader.loadTo(totalExclusionRules);
        } else {
            ExclusionsRulesPropertiesReader reader = new ExclusionsRulesFileReader(project.file(exclusionSource).getAbsolutePath());
            reader.loadTo(localExclusionRules);
            reader.loadTo(totalExclusionRules);
        }

    }

    /**
     * Определяет, является ли переданное имя файла именем артефакта
     *
     * @param name имя файла
     * @return true, если файл является артефактом, false - иначе
     */
    private static boolean isMavenArtifact(@Nonnull String name, Project project) {
        try {
            return !project.file(name).exists();
        } catch (Exception e) {
            return true;
        }
    }
}
