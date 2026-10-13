package dev.faizksen.kursor.data

import dev.faizksen.kursor.database.consts.SourceIdColumn
import dev.faizksen.kursor.model.PermanentKind
import dev.faizksen.kursor.model.SyncOutcome
import dev.faizksen.kursor.database.dao.RatesDao
import dev.faizksen.kursor.database.dao.WatchedPairsDao
import dev.faizksen.kursor.database.entities.RateEntity
import dev.faizksen.kursor.database.entities.WatchedPairEntity
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.RatePoint
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.WatchedPair
import dev.faizksen.kursor.model.WatchedPairWithRate
import dev.faizksen.kursor.network.RatesRemoteSource
import dev.faizksen.kursor.network.model.RemoteRate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

interface RatesRepository {
    fun observeWatchlist(): Flow<List<WatchedPairWithRate>>
    suspend fun refresh(sourceId: SourceId): SyncOutcome
}

class RatesRepositoryImpl @Inject constructor(
    private val ratesDao: RatesDao,
    private val watchedPairsDao: WatchedPairsDao,
    private val sources: Map<SourceId, @JvmSuppressWildcards RatesRemoteSource>,
    private val clock: Clock
): RatesRepository {

    override fun observeWatchlist(): Flow<List<WatchedPairWithRate>> =
        combine(
            watchedPairsDao.observeAll(),
            ratesDao.observeLatestRates()
        ) { pairs, rates ->
            pairs.map { pair ->
                WatchedPairWithRate(
                    watchedPair = pair.toWatchedPair(),
                    rate = rates
                        .filter { it.base == pair.base && it.quote == pair.quote }
                        .mapNotNull { it.toRatePoint() }
                        .pickPreferred()
                )
            }
        }

    override suspend fun refresh(sourceId: SourceId): SyncOutcome {
        val pairs = watchedPairsDao.getAll().map { it.toWatchedPair() }.map { it.pair }
        val source = sources[sourceId]
            ?: return SyncOutcome.Permanent(PermanentKind.UnknownSource)
        if (pairs.isEmpty()) {
            return SyncOutcome.Success(0)
        }
        return try {
            val results = source.latest(pairs)
            val fetchedAt = clock.millis()
            val rateEntities = results.map { it.toRateEntity(sourceId, fetchedAt) }
            ratesDao.upsertAll(rateEntities)
            SyncOutcome.Success(rateEntities.size)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            e.toSyncOutcome()
        }
    }
}

internal fun List<RatePoint>.pickPreferred(): RatePoint? = minByOrNull { it.sourceId.ordinal }

internal fun RateEntity.toRatePoint() = sourceId.toSourceId()?.let { sourceId ->
    RatePoint(
        date = LocalDate.parse(date),
        value = BigDecimal(value),
        sourceId = sourceId
    )
}

internal fun RemoteRate.toRateEntity(sourceId: SourceId, fetchedAt: Long) = RateEntity(
    base = base.code,
    quote = quote.code,
    date = date.toString(),
    sourceId = sourceId.toColumn(),
    value = value.toPlainString(),
    fetchedAt = fetchedAt
)

internal fun SourceId.toColumn() = when(this) {
    SourceId.FRANKFURTER -> SourceIdColumn.Frankfurter
    SourceId.COINGECKO -> SourceIdColumn.CoinGecko
    SourceId.CBR -> SourceIdColumn.CBR
}

internal fun SourceIdColumn.toSourceId() = when(this) {
    SourceIdColumn.Frankfurter -> SourceId.FRANKFURTER
    SourceIdColumn.CoinGecko -> SourceId.COINGECKO
    SourceIdColumn.CBR -> SourceId.CBR
    is SourceIdColumn.Unrecognized -> null
}

internal fun WatchedPairEntity.toWatchedPair() = WatchedPair(
    id = id,
    pair = CurrencyPair(
        base = CurrencyCode(base),
        quote = CurrencyCode(quote),
    ),
    position = position
)