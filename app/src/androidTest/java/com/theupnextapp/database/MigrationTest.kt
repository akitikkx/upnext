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

    @Test
    @Throws(IOException::class)
    fun migrate30To31() {
        helper.createDatabase(TEST_DB_NAME, 30).apply {
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB_NAME, 31, true, MIGRATION_30_31)

        db.execSQL(
            """
            INSERT INTO watched_episodes (showTraktId, showTvMazeId, showImdbId, seasonNumber, episodeNumber, episodeTraktId, watchedAt, syncStatus, lastModified)
            VALUES (101, 202, 'tt1234567', 1, 5, 303, 1622548800000, 1, 1622548800000)
            """.trimIndent()
        )
        val cursor = db.query("SELECT * FROM watched_episodes WHERE showTraktId = 101")
        cursor.use {
            assertTrue(it.moveToFirst())
            assertEquals(1, it.count)
            assertEquals(202, it.getInt(it.getColumnIndexOrThrow("showTvMazeId")))
            assertEquals("tt1234567", it.getString(it.getColumnIndexOrThrow("showImdbId")))
            assertEquals(1, it.getInt(it.getColumnIndexOrThrow("seasonNumber")))
            assertEquals(5, it.getInt(it.getColumnIndexOrThrow("episodeNumber")))
            assertEquals(303, it.getInt(it.getColumnIndexOrThrow("episodeTraktId")))
            assertEquals(1622548800000L, it.getLong(it.getColumnIndexOrThrow("watchedAt")))
            assertEquals(1, it.getInt(it.getColumnIndexOrThrow("syncStatus")))
            assertEquals(1622548800000L, it.getLong(it.getColumnIndexOrThrow("lastModified")))
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate31To32() {
        helper.createDatabase(TEST_DB_NAME, 31).apply {
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB_NAME, 32, true, MIGRATION_31_32)

        db.execSQL(
            """
            INSERT INTO recent_searches (query, searchTime)
            VALUES ('Breaking Bad', 1622548800000)
            """.trimIndent()
        )
        val cursor = db.query("SELECT * FROM recent_searches WHERE query = 'Breaking Bad'")
        cursor.use {
            assertTrue(it.moveToFirst())
            assertEquals(1, it.count)
            assertEquals(1622548800000L, it.getLong(it.getColumnIndexOrThrow("searchTime")))
        }
     }

    @Test
    @Throws(IOException::class)
    fun migrate32To33() {
        helper.createDatabase(TEST_DB_NAME, 32).apply {
            execSQL(
                """
                INSERT INTO favorite_shows (id, title, year, mediumImageUrl, originalImageUrl, imdbID, slug, tmdbID, traktID, tvdbID, tvMazeID)
                VALUES (123, 'Show Title', '2021', 'med_url', 'orig_url', 'imdb_id', 'slug-name', 456, 789, 1011, 1213)
                """.trimIndent()
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB_NAME, 33, true, MIGRATION_32_33)

        val cursor = db.query("SELECT * FROM favorite_shows WHERE id = 123")
        cursor.use {
            assertTrue(it.moveToFirst())
            assertEquals("Show Title", it.getString(it.getColumnIndexOrThrow("title")))
            assertTrue(it.isNull(it.getColumnIndexOrThrow("network")))
            assertTrue(it.isNull(it.getColumnIndexOrThrow("status")))
            assertTrue(it.isNull(it.getColumnIndexOrThrow("rating")))
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate33To34() {
        helper.createDatabase(TEST_DB_NAME, 33).apply {
            execSQL(
                """
                INSERT INTO trakt_trending (id, title, year, medium_image_url, original_image_url, imdbID, slug, tmdbID, traktID, tvdbID, tvMazeID)
                VALUES (1, 'Trending Title', '2021', 'med', 'orig', 'imdb', 'slug', 2, 3, 4, 5)
                """.trimIndent()
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB_NAME, 34, true, MIGRATION_33_34)

        db.execSQL(
            """
            INSERT INTO trending_shows (showId, title, year, medium_image_url, original_image_url, imdbID, slug, tmdbID, traktID, tvdbID, tvMazeID, providerId)
            VALUES (99, 'Trending Show', '2022', 'm', 'o', 'i', 's', 9, 8, 7, 6, 'simkl')
            """.trimIndent()
        )

        val cursor = db.query("SELECT * FROM trending_shows WHERE showId = 99")
        cursor.use {
            assertTrue(it.moveToFirst())
            assertEquals("Trending Show", it.getString(it.getColumnIndexOrThrow("title")))
            assertEquals("simkl", it.getString(it.getColumnIndexOrThrow("providerId")))
        }
    }
}
