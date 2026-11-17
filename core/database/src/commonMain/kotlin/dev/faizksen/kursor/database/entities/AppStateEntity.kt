package dev.faizksen.kursor.database.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "app_state")
data class AppStateEntity(
    @PrimaryKey
    val id: Int = SINGLE_ROW,
    val defaultsSeededAt: Long?,
) {
    companion object {
        const val SINGLE_ROW = 0
    }
}
