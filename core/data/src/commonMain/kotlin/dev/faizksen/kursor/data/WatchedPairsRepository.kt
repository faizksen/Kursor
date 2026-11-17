package dev.faizksen.kursor.data

import dev.faizksen.kursor.data.model.AddPairResult
import dev.faizksen.kursor.database.dao.WatchedPairsDao
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.WatchedPair
import kotlin.time.Clock

interface WatchedPairsRepository {
    suspend fun add(pair: CurrencyPair, provider: ProviderKey): AddPairResult
    suspend fun withoutProvider(): List<WatchedPair>
    suspend fun byId(id: Long): WatchedPair?
    suspend fun setProvider(id: Long, provider: ProviderKey)
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

    override suspend fun byId(id: Long): WatchedPair? = watchedPairsDao
        .getAll()
        .firstOrNull { entity -> entity.id == id }
        ?.toWatchedPair()

    override suspend fun withoutProvider(): List<WatchedPair> = watchedPairsDao
        .getAll()
        .map { entity -> entity.toWatchedPair() }
        .filter { watched -> watched.provider == null }

    override suspend fun setProvider(id: Long, provider: ProviderKey) =
        watchedPairsDao.setProvider(id = id, provider = provider.key)
}
