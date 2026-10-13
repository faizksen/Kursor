package dev.faizksen.kursor.database.entities

import androidx.room.Entity
import androidx.room.Index
import dev.faizksen.kursor.database.consts.SourceIdColumn

@Entity(
    tableName = "rate",
    primaryKeys = [ "base", "quote", "date", "sourceId" ],
    indices = [
        Index(
            value = ["base", "quote", "date"],
            orders = [Index.Order.ASC, Index.Order.ASC, Index.Order.DESC],
        )
    ]
)
data class RateEntity(
    val base: String,
    val quote: String,
    val date: String,
    val sourceId: SourceIdColumn,
    val value: String,
    val fetchedAt: Long
)