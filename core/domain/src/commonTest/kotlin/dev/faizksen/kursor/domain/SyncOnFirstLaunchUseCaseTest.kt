package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.SyncLogRepository
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.SyncQueue
import dev.faizksen.kursor.model.SyncResult
import dev.faizksen.kursor.model.SyncRun
import dev.faizksen.kursor.model.SyncTrigger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class SyncOnFirstLaunchUseCaseTest {

    private var lastSuccess: SyncRun? = null
    private var immediateSyncCalls = 0

    private val syncLogRepository = object : SyncLogRepository {
        override suspend fun record(run: SyncRun): Long = error("not used")
        override fun observeRecent(limit: Int): Flow<List<SyncRun>> = error("not used")
        override fun observeLastSuccess(): Flow<SyncRun?> = flowOf(lastSuccess)
        override suspend fun deleteOlderThan(threshold: Instant) = error("not used")
    }

    private val syncScheduler = object : SyncScheduler {
        override fun schedulePeriodicSync() = error("not used")

        override fun requestImmediateSync() {
            immediateSyncCalls++
        }

        override fun requestPairSync(pair: CurrencyPair) = error("not used")

        override fun observeSyncQueue(trigger: SyncTrigger): Flow<SyncQueue> = error("not used")
    }

    private val syncOnFirstLaunch = SyncOnFirstLaunchUseCaseImpl(syncLogRepository, syncScheduler)

    @Test
    fun theSeededPairsAreFilledOnTheVeryFirstLaunch() = runTest {
        syncOnFirstLaunch()

        assertEquals(1, immediateSyncCalls)
    }

    @Test
    fun aLaterLaunchLeavesTheScheduleAlone() = runTest {
        lastSuccess = SyncRun(
            id = 1,
            sourceId = SourceId.FRANKFURTER,
            trigger = SyncTrigger.Periodic,
            startedAt = Instant.fromEpochMilliseconds(1_000L),
            finishedAt = Instant.fromEpochMilliseconds(2_000L),
            result = SyncResult.Success,
            errorKind = null,
            updatedCount = 3,
            attempt = 1,
        )

        syncOnFirstLaunch()

        assertEquals(0, immediateSyncCalls)
    }
}
