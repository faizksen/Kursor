package dev.faizksen.kursor.data

import dev.faizksen.kursor.data.model.AddPairResult
import dev.faizksen.kursor.database.dao.WatchedPairsDao
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.ProviderKey
import kotlin.time.Clock

interface WatchedPairsRepository {
    suspend fun add(pair: CurrencyPair, provider: ProviderKey): AddPairResult
}

internal class WatchedPairsRepositoryImpl(
    private val watchedPairsDao: WatchedPairsDao,
    private val clock: Clock,
): WatchedPairsRepository {

    override suspend fun add(pair: CurrencyPair, provider: ProviderKey): AddPairResult {
        val inserted = watchedPairsDao.addAtEnd(
            base = pair.base.code,
            quote = pair.quote.code,
            provider = provider.key,
            addedAt = clock.now().toEpochMilliseconds(),
        )

        return if (inserted.alreadyExisted) {
            AddPairResult.AlreadyWatched(inserted.id)
        } else {
            AddPairResult.Added(inserted.id)
        }
    }
}
