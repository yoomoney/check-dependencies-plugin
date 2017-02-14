package ru.yandex.money.gradle.plugins.library.dependencies.exclusions;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;

/**
 * Читатель правил исключения изменения версий из мавен артефакта. Сканирует входящие зависимости билд скрипта, определяет список
 * подключенных артефатов. Среди них находит указанный и пытается из него загрузить указанный файл с правилами.
 *
 * @author Brovin Yaroslav (brovin@yamoney.ru)
 * @since 10.02.2017
 */
public class ExclusionsRulesPackageReader extends ExclusionsRulesPropertiesReader {

    private final Logger log = LoggerFactory.getLogger(ExclusionsRulesPackageReader.class);
    private final Project project;
    private final String artifact;
    private final String exclusionFileName;

    public ExclusionsRulesPackageReader(@Nonnull Project project, @Nonnull String artifact, @Nonnull String exclusionFileName) {
        this.project = project;
        this.artifact = artifact;
        this.exclusionFileName = exclusionFileName;
    }

    /**
     * Ищет указанный файл в указанном мавен артефакте с учетом текущей конфигурации
     *
     * @param configuration конфигурация с входящими зависимостями
     * @param artifact      мавен артефакт
     * @param fileName      имя файла внутри артефакта
     * @return файл, если он был найден, null - в противном случае
     */
    @Nullable
    private File findFileInArtifact(@Nonnull Configuration configuration, @Nonnull String artifact, @Nonnull String fileName) {
        File artifactFile = findArtifactFile(configuration, artifact);
        return artifactFile != null ? findFileInZip(artifactFile, fileName) : null;
    }

    /**
     * Ищет указанный файл мавен артефакта с учетом текущей конфигурации. Если в названии артефакта опущена версия, то в случае
     * наличия несколько подходящих пакетов, вернет первый найденный.
     *
     * @param configuration конфигурация с входящими зависимостями
     * @param artifact      мавен артефакт. Может опускать версию и содержать только группу и id
     * @return файл артефакта, если он был найден, null - в противном случае
     */
    @Nullable
    private static File findArtifactFile(@Nonnull Configuration configuration, @Nonnull String artifact) {
        Set<ResolvedArtifact> artifacts = configuration.getResolvedConfiguration().getResolvedArtifacts();
        for (ResolvedArtifact requestedArtifact : artifacts) {
            String artifactID = requestedArtifact.getId().getComponentIdentifier().getDisplayName();
            if (artifactID.startsWith(artifact)) {
                return requestedArtifact.getFile();
            }
        }
        return null;
    }

    /**
     * Ищет в указанном zip архиве указанный файл
     *
     * @param zipFile  zip архив
     * @param fileName имя разыскиваемого файла
     * @return Файл, если он есть в архиве, null - в противном случае
     */
    @Nullable
    private File findFileInZip(@Nonnull File zipFile, @Nonnull String fileName) {
        FileTree files = project.zipTree(zipFile);
        FileCollection filteredFiles = files.filter(element -> Objects.equals(element.getName(), fileName));
        return filteredFiles.getSingleFile();
    }

    @Override
    public void loadTo(@Nonnull ExclusionsRulesStorage rulesStorage) {

        for (Configuration configuration : project.getBuildscript().getConfigurations()) {
            File rulesFile = findFileInArtifact(configuration, artifact, exclusionFileName);
            if (rulesFile != null) {
                try (FileInputStream fileInputStream = new FileInputStream(rulesFile)) {
                    load(rulesStorage, fileInputStream);
                } catch (FileNotFoundException e) {
                    log.warn("Cannot find file with upgrade versions rules.", e);
                } catch (IOException e) {
                    log.warn("Cannot loadTo file with upgrade versions rules.", e);
                }
            }
        }
    }
}
