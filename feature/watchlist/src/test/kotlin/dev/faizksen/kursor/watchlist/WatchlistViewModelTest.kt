package dev.faizksen.kursor.watchlist

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.Rate
import dev.faizksen.kursor.model.ProviderKey
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
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import org.junit.Rule
import org.junit.Test

class WatchlistViewModelTest {

    @get: Rule val mainDispatcherRule = MainDispatcherRule()

    private val pairs = MutableStateFlow<List<WatchedPairWithRate>>(emptyList())
    private val runs = MutableStateFlow<List<SyncRun>>(emptyList())
    private val syncState = MutableStateFlow(SyncState.Idle)
    private var manualSyncCalls = 0
    private var logLimit: Int? = null
    private val seeded = MutableStateFlow(true)
    private val deleted = MutableStateFlow<CurrencyPair?>(null)
    private var undoCalls = 0
    private var forgetCalls = 0
    private var reordered: List<Long>? = null

    @Test
    fun `a deleted pair is offered back until something decides`() = runTest {
        deleted.value = CurrencyPair(CurrencyCode("EUR"), CurrencyCode("PLN"))

        viewModel().deletedPair.test {
            assertThat(awaitItem()?.quote).isEqualTo(CurrencyCode("PLN"))
        }
    }

    @Test
    fun `tapping undo restores the pair`() = runTest {
        val viewModel = viewModel()

        viewModel.obtainAction(WatchlistUiAction.UndoDeleteClicked)
        runCurrent()

        assertThat(undoCalls).isEqualTo(1)
    }

    @Test
    fun `letting the snackbar go makes the deletion final`() = runTest {
        val viewModel = viewModel()

        viewModel.obtainAction(WatchlistUiAction.DeleteNoticeDismissed)

        assertThat(forgetCalls).isEqualTo(1)
        assertThat(undoCalls).isEqualTo(0)
    }

    private fun viewModel() = WatchlistViewModel(
        watchlistUseCase = { pairs },
        requestManualSync = { manualSyncCalls++ },
        reorderPairs = { ids -> reordered = ids },
        undoDeletedPair = {
            undoCalls++
            true
        },
        forgetDeletedPair = { forgetCalls++ },
        observeDeletedPair = { deleted },
        observeSyncState = { syncState },
        observeSyncLog = { limit ->
            logLimit = limit
            runs
        },
        observeDefaultsSeeded = { seeded },
    )

    private fun watchedPair(id: Long = 1, base: String = "EUR", quote: String = "RUB") =
        WatchedPairWithRate(
            watchedPair = WatchedPair(
                id = id,
                pair = CurrencyPair(CurrencyCode(base), CurrencyCode(quote)),
                provider = ProviderKey("CBR"),
                position = 0,
                addedAt = Instant.parse("2026-10-13T10:00:00Z"),
            ),
            rate = RatePoint(
                date = LocalDate(2026, 10, 13),
                value = Rate.parse("95.42")!!,
                sourceId = SourceId.FRANKFURTER,
                provider = ProviderKey("CBR")
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
    fun `an empty watchlist before seeding is still loading`() = runTest {
        seeded.value = false

        viewModel().screenState.test {
            assertThat(awaitItem()).isEqualTo(WatchlistUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `an empty watchlist after seeding is empty on purpose`() = runTest {
        seeded.value = true

        viewModel().screenState.test {
            assertThat(awaitItem()).isEqualTo(WatchlistUiState.Empty)
            cancelAndIgnoreRemainingEvents()
        }
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