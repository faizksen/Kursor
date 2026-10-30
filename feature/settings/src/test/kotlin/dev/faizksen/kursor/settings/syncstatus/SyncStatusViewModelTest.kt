package dev.faizksen.kursor.settings.syncstatus

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.faizksen.kursor.domain.ObserveSyncLogUseCase
import dev.faizksen.kursor.domain.ObserveSyncQueueUseCase
import dev.faizksen.kursor.domain.RequestManualSyncUseCase
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.SyncQueue
import dev.faizksen.kursor.model.SyncResult
import dev.faizksen.kursor.model.SyncRun
import dev.faizksen.kursor.model.SyncState
import dev.faizksen.kursor.model.SyncTrigger
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
import java.time.Instant

class SyncStatusViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val runs = MutableStateFlow<List<SyncRun>>(emptyList())
    private val periodic = MutableStateFlow(SyncQueue.Idle)
    private val manual = MutableStateFlow(SyncQueue.Idle)
    private var manualSyncCalls = 0
    private var logLimit: Int? = null

    private fun viewModel() = SyncStatusViewModel(
        requestManualSync = { manualSyncCalls++ },
        observeSyncLog = { limit ->
            logLimit = limit
            runs
        },
        observeSyncQueue = { trigger ->
            when (trigger) {
                SyncTrigger.Periodic -> periodic
                SyncTrigger.Manual -> manual
            }
        },
    )

    @Test
    fun `the three sources are collected into content`() = runTest {
        runs.value = listOf(syncRun())
        periodic.value = queue(SyncState.Scheduled, attemptCount = 1)
        manual.value = queue(SyncState.Running, attemptCount = 2)

        assertThat(viewModel().screenState.value).isEqualTo(
            SyncStatusUiState.Content(
                periodic = queue(SyncState.Scheduled, attemptCount = 1),
                manual = queue(SyncState.Running, attemptCount = 2),
                runs = listOf(syncRun()),
            )
        )
    }

    @Test
    fun `the periodic and the manual queue do not get mixed up`() = runTest {
        periodic.value = queue(SyncState.Scheduled)
        manual.value = queue(SyncState.Running)

        val state = viewModel().screenState.value as SyncStatusUiState.Content

        assertThat(state.periodic.state).isEqualTo(SyncState.Scheduled)
        assertThat(state.manual.state).isEqualTo(SyncState.Running)
    }

    @Test
    fun `the manual queue alone rebuilds the state`() = runTest {
        viewModel().screenState.test {
            assertThat((awaitItem() as SyncStatusUiState.Content).manual.state)
                .isEqualTo(SyncState.Idle)

            manual.value = queue(SyncState.Running)

            assertThat((awaitItem() as SyncStatusUiState.Content).manual.state)
                .isEqualTo(SyncState.Running)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `the periodic queue alone rebuilds the state`() = runTest {
        viewModel().screenState.test {
            assertThat((awaitItem() as SyncStatusUiState.Content).periodic.state)
                .isEqualTo(SyncState.Idle)

            periodic.value = queue(SyncState.Scheduled)

            assertThat((awaitItem() as SyncStatusUiState.Content).periodic.state)
                .isEqualTo(SyncState.Scheduled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `the log alone rebuilds the state`() = runTest {
        viewModel().screenState.test {
            assertThat((awaitItem() as SyncStatusUiState.Content).runs).isEmpty()

            runs.value = listOf(syncRun())

            assertThat((awaitItem() as SyncStatusUiState.Content).runs)
                .containsExactly(syncRun())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `the log is read in full history depth`() = runTest {
        viewModel()

        assertThat(logLimit).isEqualTo(50)
    }

    @Test
    fun `run sync click reaches the use case`() = runTest {
        viewModel().obtainAction(SyncStatusUiAction.RunSyncClicked)

        assertThat(manualSyncCalls).isEqualTo(1)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `start state is Loading`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = viewModel()

        assertThat(viewModel.screenState.value).isEqualTo(SyncStatusUiState.Loading)

        advanceUntilIdle()

        assertThat(viewModel.screenState.value).isInstanceOf(SyncStatusUiState.Content::class.java)
    }

    private fun queue(state: SyncState, attemptCount: Int = 0) =
        SyncQueue(state = state, nextRunAt = null, attemptCount = attemptCount)

    private fun syncRun(attempt: Int = 1) = SyncRun(
        id = 1,
        sourceId = SourceId.FRANKFURTER,
        trigger = SyncTrigger.Manual,
        startedAt = Instant.parse("2026-10-13T10:00:00Z"),
        finishedAt = Instant.parse("2026-10-13T10:00:02Z"),
        result = SyncResult.Success,
        errorKind = null,
        updatedCount = 4,
        attempt = attempt,
    )
}
