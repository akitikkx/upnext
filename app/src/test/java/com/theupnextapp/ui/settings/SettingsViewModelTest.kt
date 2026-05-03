package com.theupnextapp.ui.settings

import com.theupnextapp.domain.SimklAccessToken
import com.theupnextapp.domain.Theme
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.repository.ProviderManager
import com.theupnextapp.repository.SettingsRepository
import com.theupnextapp.repository.SimklAuthManager
import com.theupnextapp.repository.SimklRepository
import com.theupnextapp.repository.TraktRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
class SettingsViewModelTest {
    private lateinit var viewModel: SettingsViewModel
    private val mockSettingsRepository = mock(SettingsRepository::class.java)
    private val mockTraktRepository = mock(TraktRepository::class.java)
    private val mockProviderManager = mock(ProviderManager::class.java)
    private val mockSimklAuthManager = mock(SimklAuthManager::class.java)
    private val mockSimklRepository = mock(SimklRepository::class.java)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val themeFlow = MutableStateFlow(Theme.DARK)
        val dataSaverFlow = MutableStateFlow(true)
        val providerFlow = MutableStateFlow(ProviderManager.PROVIDER_TRAKT)
        val simklTokenFlow = MutableStateFlow<SimklAccessToken?>(null)
        val traktTokenFlow =
            MutableStateFlow(
                TraktAccessToken(
                    access_token = "test_token",
                    created_at = 0,
                    expires_in = 0,
                    refresh_token = "",
                    scope = "",
                    token_type = "",
                ),
            )

        `when`(mockSettingsRepository.themeStream).thenReturn(themeFlow)
        `when`(mockSettingsRepository.dataSaverStream).thenReturn(dataSaverFlow)
        `when`(mockTraktRepository.traktAccessToken).thenReturn(traktTokenFlow)
        `when`(mockProviderManager.activeProvider).thenReturn(providerFlow)
        `when`(mockSimklAuthManager.simklAccessToken).thenReturn(simklTokenFlow)

        viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockTraktRepository,
            mockProviderManager,
            mockSimklAuthManager,
            mockSimklRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onThemeSelected calls repository with correct theme`() =
        runTest {
            viewModel.onThemeSelected(Theme.LIGHT)
            testDispatcher.scheduler.advanceUntilIdle()
            verify(mockSettingsRepository).setTheme(Theme.LIGHT)
        }

    @Test
    fun `onDataSaverToggled calls repository with correct flag`() =
        runTest {
            viewModel.onDataSaverToggled(false)
            testDispatcher.scheduler.advanceUntilIdle()
            verify(mockSettingsRepository).setDataSaverEnabled(false)
        }

    @Test
    fun `onDisconnectTrakt revokes token and clears history`() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.traktAccessToken.collect {}
            }

            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onDisconnectTrakt()
            testDispatcher.scheduler.advanceUntilIdle()
            verify(mockTraktRepository).clearWatchlist()
        }
}
