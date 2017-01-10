package ru.yandex.money.gradle.plugins.library.readme

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.joda.time.DateTime
import org.testng.annotations.Test
import ru.yandex.money.gradle.plugins.library.AbstractGradleTest
import ru.yandex.money.gradle.plugins.library.helpers.GitRepositoryProperties

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SKIPPED
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.hasSize
import static org.testng.Assert.assertEquals
import static org.testng.Assert.assertTrue

/**
 *
 * @author Kirill Bulatov (mail4score@gmail.com)
 * @since 22.12.2016
 */
class PublishReadmeTaskTest extends AbstractGradleTest {
    private static final String NON_EXISTENT_FILE_PATH = "non_existent_path_${DateTime.now().getMillis()}"

    private static String forceTaskExecutionIfNeeded(boolean shouldExecute, String taskName) {
        if ((shouldExecute && GitRepositoryProperties.instance.masterBranch) ||
                (!shouldExecute && !GitRepositoryProperties.instance.masterBranch)) {
            return ""
        }
        return "${taskName}.setOnlyIf({ $shouldExecute })"
    }

    private static String generateBuildFileContents(boolean shouldExecuteTask) {
        return COMMON_BUILD_FILE_CONTENTS + """
        readme {
            pathToDocument = '$NON_EXISTENT_FILE_PATH'
        }
        
        ${forceTaskExecutionIfNeeded(shouldExecuteTask, PublishReadmeTask.TASK_NAME)}
        """
    }

    @Test
    void testUploadReadmeTask_onMasterBranch_withIncorrectFilePath() {
        buildFile << generateBuildFileContents(true)

        BuildResult result = GradleRunner.create()
                .withProjectDir(temporaryFolder)
                .withArguments(PublishReadmeTask.TASK_NAME)
                .forwardOutput()
                .buildAndFail()

        assertThat(result.tasks, hasSize(1))
        println result.getOutput()
        assertTrue(result.getOutput().contains("java.nio.file.NoSuchFileException: $NON_EXISTENT_FILE_PATH"))
        assertEquals(result.task(":$PublishReadmeTask.TASK_NAME").getOutcome(), FAILED)
    }

    @Test
    void testUploadReadmeTask_notOnMasterBranch() {
        buildFile << generateBuildFileContents(false)

        BuildResult result = GradleRunner.create()
                .withProjectDir(temporaryFolder)
                .withArguments(PublishReadmeTask.TASK_NAME)
                .forwardOutput()
                .build()

        assertThat(result.tasks, hasSize(1))
        assertEquals(result.task(":$PublishReadmeTask.TASK_NAME").getOutcome(), SKIPPED)
    }
}
