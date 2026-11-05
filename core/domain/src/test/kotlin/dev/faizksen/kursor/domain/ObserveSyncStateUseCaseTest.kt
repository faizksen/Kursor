package dev.faizksen.kursor.domain

import com.google.common.truth.Truth.assertThat
import dev.faizksen.kursor.model.SyncQueue
import dev.faizksen.kursor.model.SyncState
import dev.faizksen.kursor.model.SyncTrigger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

class ObserveSyncStateUseCaseTest {

    private var queues: Flow<SyncQueue> = emptyFlow()
    private var requestedTrigger: SyncTrigger? = null

    private val syncScheduler = object : SyncScheduler {
        override fun schedulePeriodicSync() = error("not used")

        override fun requestImmediateSync() = error("not used")

        override fun observeSyncQueue(trigger: SyncTrigger): Flow<SyncQueue> {
            requestedTrigger = trigger
            return queues
        }
    }

    private val useCase = ObserveSyncStateUseCaseImpl(syncScheduler)

    @Test
    fun `queue is unwrapped into its state`() = runTest {
        queues = flowOf(queue(SyncState.Idle), queue(SyncState.Running))

        assertThat(useCase(SyncTrigger.Manual).toList())
            .containsExactly(SyncState.Idle, SyncState.Running)
            .inOrder()
    }

    @Test
    fun `repeated states are reported once`() = runTest {
        queues = flowOf(
            queue(SyncState.Idle),
            queue(SyncState.Idle),
            queue(SyncState.Running),
            queue(SyncState.Running),
        )

        assertThat(useCase(SyncTrigger.Manual).toList())
            .containsExactly(SyncState.Idle, SyncState.Running)
            .inOrder()
    }

    @Test
    fun `a state returning after another one is reported again`() = runTest {
        queues = flowOf(
            queue(SyncState.Idle),
            queue(SyncState.Running),
            queue(SyncState.Idle),
        )

        assertThat(useCase(SyncTrigger.Manual).toList())
            .containsExactly(SyncState.Idle, SyncState.Running, SyncState.Idle)
            .inOrder()
    }

    @Test
    fun `the rest of the queue is ignored`() = runTest {
        queues = flowOf(
            SyncQueue(
                state = SyncState.Scheduled,
                nextRunAt = Instant.parse("2026-10-13T10:00:00Z"),
                attemptCount = 3,
            ),
            SyncQueue(
                state = SyncState.Scheduled,
                nextRunAt = Instant.parse("2026-10-13T11:00:00Z"),
                attemptCount = 4,
            ),
        )

        assertThat(useCase(SyncTrigger.Manual).toList())
            .containsExactly(SyncState.Scheduled)
    }

    @Test
    fun `an empty queue reports nothing`() = runTest {
        assertThat(useCase(SyncTrigger.Manual).toList()).isEmpty()
    }

    @Test
    fun `the trigger is passed to the scheduler`() = runTest {
        useCase(SyncTrigger.Periodic).toList()

        assertThat(requestedTrigger).isEqualTo(SyncTrigger.Periodic)
    }

    private fun queue(state: SyncState) =
        SyncQueue(state = state, nextRunAt = null, attemptCount = 0)
}
