package dev.faizksen.kursor.watchlist

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.RatePoint
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.SyncRun
import dev.faizksen.kursor.model.SyncState
import dev.faizksen.kursor.model.WatchedPair
import dev.faizksen.kursor.model.WatchedPairWithRate
import dev.faizksen.kursor.testing.MainDispatcherRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class WatchlistViewModelTest {

    @get: Rule val mainDispatcherRule = MainDispatcherRule()

    private val pairs = MutableStateFlow<List<WatchedPairWithRate>>(emptyList())
    private val runs = MutableStateFlow<List<SyncRun>>(emptyList())
    private val syncState = MutableStateFlow(SyncState.Idle)
    private var manualSyncCalls = 0
    private var logLimit: Int? = null

    private fun viewModel() = WatchlistViewModel(
        watchlistUseCase = { pairs },
        requestManualSync = { manualSyncCalls++ },
        observeSyncState = { syncState },
        observeSyncLog = { limit ->
            logLimit = limit
            runs
        }
    )

    private fun watchedPair(id: Long = 1, base: String = "EUR", quote: String = "RUB") =
        WatchedPairWithRate(
            watchedPair = WatchedPair(
                id = id,
                pair = CurrencyPair(CurrencyCode(base), CurrencyCode(quote)),
                position = 0
            ),
            rate = RatePoint(
                date = LocalDate.of(2026,10,13),
                value = BigDecimal("95.42"),
                sourceId = SourceId.FRANKFURTER
            )
        )

    @Test
    fun `pairs in watchlist produce results`() = runTest {
        pairs.value = listOf(watchedPair())

        viewModel().screenState.test {
            assertThat(awaitItem()).isEqualTo(
                WatchlistUiState.Results(
                    lastUpdate = LastUpdate.Empty,
                    pairs = listOf(watchedPair())
                )
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty pairs in watchlist produce Empty`() = runTest {
        assertThat(viewModel().screenState.value).isEqualTo(WatchlistUiState.Empty)
    }

    @Test
    fun `refresh click increase manualSyncCalls`() = runTest {
        val viewModel = viewModel()
        viewModel.obtainAction(WatchlistUiAction.RefreshClicked)
        assertThat(manualSyncCalls).isEqualTo(1)
    }

    @Test
    fun `view model request only last run`() = runTest {
        viewModel()
        assertThat(logLimit).isEqualTo(1)
    }

    @Test
    fun `emit pairs produce Results`() = runTest {
        viewModel().screenState.test {
            assertThat(awaitItem()).isEqualTo(WatchlistUiState.Empty)
            pairs.value = listOf(watchedPair())
            assertThat(awaitItem()).isEqualTo(
                WatchlistUiState.Results(
                    lastUpdate = LastUpdate.Empty,
                    pairs = listOf(watchedPair())
                )
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `syncState emit change only lastUpdate`() = runTest {
        pairs.value = listOf(watchedPair())

        viewModel().screenState.test {
            assertThat(awaitItem()).isEqualTo(
                WatchlistUiState.Results(
                    lastUpdate = LastUpdate.Empty,
                    pairs = listOf(watchedPair())
                )
            )
            syncState.value = SyncState.Running
            assertThat(awaitItem()).isEqualTo(
                WatchlistUiState.Results(
                    lastUpdate = LastUpdate.Progress(0),
                    pairs = listOf(watchedPair())
                )
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `start state is Loading`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = viewModel()
        assertThat(viewModel.screenState.value).isEqualTo(WatchlistUiState.Loading)
        advanceUntilIdle()
        assertThat(viewModel.screenState.value).isEqualTo(WatchlistUiState.Empty)
    }
}