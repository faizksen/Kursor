package dev.faizksen.kursor.model

import java.time.Instant

data class SyncQueue(
    val state: SyncState,
    val nextRunAt: Instant?,
    val attemptCount: Int,
) {
    val isWaitingForRetry: Boolean
        get() = state == SyncState.Scheduled && attemptCount > 0

    companion object {
        val Idle = SyncQueue(state = SyncState.Idle, nextRunAt = null, attemptCount = 0)
    }
}
