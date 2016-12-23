package ru.yandex.money.gradle.plugins.library

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.testng.annotations.Test
import ru.yandex.money.gradle.plugins.library.readme.PublishReadmeTask

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.hasSize
import static org.testng.Assert.assertEquals
import static org.testng.Assert.assertTrue

/**
 *
 * @author Kirill Bulatov (mail4score@gmail.com)
 * @since 22.12.2016
 */
class LibraryPluginTest extends AbstractGradleTest {

    @Test
    void testAllTasksArePresent() {
        def expectedTasks = [PublishReadmeTask.TASK_NAME]

        buildFile << COMMON_BUILD_FILE_CONTENTS

        BuildResult result = GradleRunner.create()
                .withProjectDir(temporaryFolder)
                .withArguments("tasks")
                .build()

        //checking, that "tasks" task was the only launched task and that it run successfully
        assertThat(result.tasks, hasSize(1))
        assertEquals(result.task(":tasks").outcome, TaskOutcome.SUCCESS)

        expectedTasks.forEach({
            assertTrue(result.getOutput().contains(it))
        })
    }
}
