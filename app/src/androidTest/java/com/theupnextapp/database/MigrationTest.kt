package com.theupnextapp.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

private const val TEST_DB_NAME = "test-db"

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper: MigrationTestHelper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            UpnextDatabase::class.java.canonicalName!!,
            FrameworkSQLiteOpenHelperFactory(),
        )

    @Test
    @Throws(IOException::class)
    fun migrate29To30() {
        // Create the database at version 29
        helper.createDatabase(TEST_DB_NAME, 29).apply {
            // Insert some data into the schedule_yesterday table
            execSQL(
                """
                INSERT INTO schedule_yesterday (id, image, language, mediumImage, name, officialSite, premiered, runtime, status, summary, type, updated, url) 
                VALUES (1, 'image_url', 'en', 'medium_image_url', 'Test Show', 'http://test.com', '2023-01-01', '60', 'Running', 'Summary text', 'Scripted', '12345', 'http://url.com')
                """.trimIndent(),
            )
            close()
        }

        // Re-open the database with version 30 and run the migration.
        val db = helper.runMigrationsAndValidate(TEST_DB_NAME, 30, true, MIGRATION_29_30)

        // Validate the data was migrated properly.
        val cursor = db.query("SELECT * FROM schedule_yesterday")
        cursor.use {
            assertTrue(it.moveToFirst())
            assertEquals(1, it.count)
            // Check that the old data is still there
            assertEquals("Test Show", it.getString(it.getColumnIndexOrThrow("name")))
            // Check that the new column exists and has the correct default value (null)
            assertTrue(it.isNull(it.getColumnIndexOrThrow("showId")))
        }
    }
}
