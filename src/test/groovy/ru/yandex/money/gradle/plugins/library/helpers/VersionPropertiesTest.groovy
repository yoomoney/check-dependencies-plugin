package ru.yandex.money.gradle.plugins.library.helpers

import org.testng.annotations.Test

import static org.testng.Assert.assertNotNull
import static org.testng.Assert.assertTrue

/**
 *
 * @author Kirill Bulatov (mail4score@gmail.com)
 * @since 23.12.2016
 */
class VersionPropertiesTest {

    @Test
    void testOpenCorrectVersionFile() {
        def versionFile = new File("./gradle.properties")

        def result = VersionProperties.loadPluginVersion(versionFile)

        assertNotNull(result)
        assertTrue(result.length() > 0)
    }

    @Test(expectedExceptions = IllegalStateException)
    void testOpenIncorrectVersionFile() {
        VersionProperties.loadPluginVersion(new File("./build.gradle"))
    }

    @Test(expectedExceptions = IllegalStateException)
    void testOpenNonExistentFile() {
        VersionProperties.loadPluginVersion(new File("./zzzz"))
    }
}
