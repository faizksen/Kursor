package dev.faizksen.kursor.sync

import androidx.work.ListenableWorker
import dev.faizksen.kursor.model.PermanentKind
import dev.faizksen.kursor.model.SyncOutcome
import dev.faizksen.kursor.model.TransientKind
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkerResultTest {

    @Test
    fun `a successful sync succeeds`() {
        assertEquals(
            ListenableWorker.Result.success(),
            SyncOutcome.Success(3).toWorkerResult(canRetry = true),
        )
    }

    @Test
    fun `a successful sync succeeds even on the last attempt`() {
        assertEquals(
            ListenableWorker.Result.success(),
            SyncOutcome.Success(3).toWorkerResult(canRetry = false),
        )
    }

    @Test
    fun `a transient failure asks for a retry while attempts remain`() {
        assertEquals(
            ListenableWorker.Result.retry(),
            SyncOutcome.Transient(TransientKind.NoNetwork).toWorkerResult(canRetry = true),
        )
    }

    @Test
    fun `a transient failure on the last attempt gives up`() {
        assertEquals(
            ListenableWorker.Result.failure(),
            SyncOutcome.Transient(TransientKind.NoNetwork).toWorkerResult(canRetry = false),
        )
    }

    @Test
    fun `a permanent failure never asks for a retry`() {
        assertEquals(
            ListenableWorker.Result.failure(),
            SyncOutcome.Permanent(PermanentKind.ParseError).toWorkerResult(canRetry = true),
        )
    }
}
