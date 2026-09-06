package dev.faizksen.kursor.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "watched_pair",
    indices = [Index(value = ["base", "quote"], unique = true)]
)
data class WatchedPairEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val base: String,
    val quote: String,
    val position: Int,
    val addedAt: Long
)