package dev.faizksen.kursor.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import dev.faizksen.kursor.database.entities.RateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RatesDao {
    @Upsert
    suspend fun upsertAll(rates: List<RateEntity>)

    @Query(""" 
      SELECT * FROM rate r 
      WHERE r.date = ( 
          SELECT MAX(date) FROM rate 
          WHERE base = r.base AND quote = r.quote AND sourceId = r.sourceId
      )
  """)
    fun observeLatestRates(): Flow<List<RateEntity>>
}