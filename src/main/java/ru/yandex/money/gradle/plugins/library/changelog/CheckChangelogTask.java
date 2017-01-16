package ru.yandex.money.gradle.plugins.library.changelog;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Задача на проверку коректности содержимого changelog файла
 *
 * @author Konstantin Rashev (rashev@yamoney.ru)
 * @since 09.01.2017
 */
public class CheckChangelogTask extends DefaultTask {

    static final String CHANGELOG_FILE_NAME = "CHANGELOG.md";
    private static final String SNAPSHOT_VERSION_POSTFIX = "-SNAPSHOT";

    @TaskAction
    public void check() throws IOException {
        File changelogFile = getProject().file(CHANGELOG_FILE_NAME);
        if (!changelogFile.exists()) {
            getLogger().info("Changelog file doesn't exist");
            return;
        }

        String expectedEntry = "## [" + getReleaseVersion() + "]()";
        Pattern lineWithExpectedEntryPattern = Pattern.compile('^' + Pattern.quote(expectedEntry) + ".*$");

        boolean isExpectedEntryFound = Files.lines(changelogFile.toPath())
                .anyMatch(line -> lineWithExpectedEntryPattern.matcher(line).find());
        if (!isExpectedEntryFound) {
            throw new IllegalStateException(
                    String.format("Changelog entry not found.%nPlease add description for changes.%nExpected: '%s'", expectedEntry));
        }
    }


    private String getReleaseVersion() {
        return getProject().getVersion().toString().replace(SNAPSHOT_VERSION_POSTFIX, "");
    }
}
