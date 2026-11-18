package dev.faizksen.kursor.data

import dev.faizksen.kursor.data.model.AddPairResult
import dev.faizksen.kursor.database.dao.RatesDao
import dev.faizksen.kursor.database.dao.WatchedPairsDao
import dev.faizksen.kursor.database.entities.RateEntity
import dev.faizksen.kursor.database.entities.WatchedPairEntity
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.WatchedPair
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

interface WatchedPairsRepository {
    suspend fun add(pair: CurrencyPair, provider: ProviderKey): AddPairResult
    suspend fun withoutProvider(): List<WatchedPair>
    suspend fun byId(id: Long): WatchedPair?
    suspend fun setProvider(id: Long, provider: ProviderKey)
    suspend fun delete(id: Long): CurrencyPair?
    suspend fun undoDelete(): Boolean
    fun forgetDeleted()
    fun observeDeleted(): Flow<CurrencyPair?>
}

private data class DeletedPair(
    val pair: WatchedPairEntity,
    val rates: List<RateEntity>,
)

internal class WatchedPairsRepositoryImpl(
    private val watchedPairsDao: WatchedPairsDao,
    private val ratesDao: RatesDao,
    private val clock: Clock,
): WatchedPairsRepository {

    private val deleted = MutableStateFlow<DeletedPair?>(null)

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

    override suspend fun byId(id: Long): WatchedPair? = entityById(id)?.toWatchedPair()

    override suspend fun withoutProvider(): List<WatchedPair> = watchedPairsDao
        .getAll()
        .map { entity -> entity.toWatchedPair() }
        .filter { watched -> watched.provider == null }

    override suspend fun setProvider(id: Long, provider: ProviderKey) =
        watchedPairsDao.setProvider(id = id, provider = provider.key)

    override suspend fun delete(id: Long): CurrencyPair? {
        val entity = entityById(id) ?: return null

        val rates = ratesDao.ratesOf(
            base = entity.base,
            quote = entity.quote,
            provider = entity.provider,
        )

        watchedPairsDao.deleteById(id)
        ratesDao.deleteRatesOf(
            base = entity.base,
            quote = entity.quote,
            provider = entity.provider,
        )

        deleted.value = DeletedPair(pair = entity, rates = rates)

        return entity.toWatchedPair().pair
    }

    override suspend fun undoDelete(): Boolean {
        val restored = deleted.value ?: return false

        watchedPairsDao.insert(restored.pair)
        ratesDao.upsertAll(restored.rates)
        deleted.value = null

        return true
    }

    override fun forgetDeleted() {
        deleted.value = null
    }

    override fun observeDeleted(): Flow<CurrencyPair?> = deleted
        .asStateFlow()
        .map { held -> held?.pair?.toWatchedPair()?.pair }

    private suspend fun entityById(id: Long): WatchedPairEntity? = watchedPairsDao
        .getAll()
        .firstOrNull { entity -> entity.id == id }
}
