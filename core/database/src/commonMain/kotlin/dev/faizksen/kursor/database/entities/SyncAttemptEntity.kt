package dev.faizksen.kursor.database.entities

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import dev.faizksen.kursor.database.consts.AttemptResultColumn
import dev.faizksen.kursor.database.consts.ErrorColumn

@Entity(
    tableName = "sync_attempt",
    foreignKeys = [
        ForeignKey(
            entity = SyncLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["runId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["runId"])],
)
data class SyncAttemptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val runId: Long,
    val provider: String,
    val result: AttemptResultColumn,
    val errorKind: ErrorColumn?,
    val requestedCount: Int,
    val coveredCount: Int,
    val startedAt: Long,
    val finishedAt: Long,
)
