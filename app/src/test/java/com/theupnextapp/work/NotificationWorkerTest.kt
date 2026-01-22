package com.theupnextapp.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.theupnextapp.repository.SettingsRepository
import com.theupnextapp.repository.TraktRepository
import kotlinx.coroutines.flow.flowOf
import org.robolectric.RobolectricTestRunner
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.junit.Ignore
import com.google.firebase.FirebaseApp
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@Ignore("FirebaseApp initialization failure in Robolectric environment")
class NotificationWorkerTest {

    private lateinit var context: Context
    private lateinit var mockSettingsRepository: SettingsRepository
    private lateinit var mockTraktRepository: TraktRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        mockSettingsRepository = mock()
        mockTraktRepository = mock()
    }

    @Test
    fun testNotificationWorker_returnsSuccess_whenNotificationsEnabled() {
        runBlocking {
            whenever(mockSettingsRepository.areNotificationsEnabled).doReturn(flowOf(true))

            val worker = TestListenableWorkerBuilder<NotificationWorker>(context)
                .setWorkerFactory(
                    object : androidx.work.WorkerFactory() {
                        override fun createWorker(
                            appContext: Context,
                            workerClassName: String,
                            workerParameters: androidx.work.WorkerParameters
                        ): ListenableWorker? {
                            return NotificationWorker(
                                appContext,
                                workerParameters,
                                mockTraktRepository,
                                mockSettingsRepository
                            )
                        }
                    }
                )
                .build()

            val result = worker.doWork()
            assertEquals(ListenableWorker.Result.success(), result)
        }
    }

    @Test
    fun testNotificationWorker_returnsSuccess_whenNotificationsDisabled() {
        runBlocking {
            whenever(mockSettingsRepository.areNotificationsEnabled).doReturn(flowOf(false))

            val worker = TestListenableWorkerBuilder<NotificationWorker>(context)
                .setWorkerFactory(
                    object : androidx.work.WorkerFactory() {
                        override fun createWorker(
                            appContext: Context,
                            workerClassName: String,
                            workerParameters: androidx.work.WorkerParameters
                        ): ListenableWorker? {
                            return NotificationWorker(
                                appContext,
                                workerParameters,
                                mockTraktRepository,
                                mockSettingsRepository
                            )
                        }
                    }
                )
                .build()

            val result = worker.doWork()
            assertEquals(ListenableWorker.Result.success(), result)
        }
    }
}
