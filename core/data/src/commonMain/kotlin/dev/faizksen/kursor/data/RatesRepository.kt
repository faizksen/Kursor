package dev.faizksen.kursor.data

import dev.faizksen.kursor.data.mappers.toColumn
import dev.faizksen.kursor.data.mappers.toSourceId
import dev.faizksen.kursor.model.PermanentKind
import dev.faizksen.kursor.model.SyncOutcome
import dev.faizksen.kursor.database.dao.RatesDao
import dev.faizksen.kursor.database.dao.WatchedPairsDao
import dev.faizksen.kursor.database.entities.RateEntity
import dev.faizksen.kursor.database.entities.WatchedPairEntity
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.Rate
import dev.faizksen.kursor.model.RatePoint
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.WatchedPair
import dev.faizksen.kursor.model.WatchedPairWithRate
import dev.faizksen.kursor.network.RatesRemoteSource
import dev.faizksen.kursor.network.model.RemoteRate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

interface RatesRepository {
    fun observeWatchlist(): Flow<List<WatchedPairWithRate>>
    suspend fun refresh(sourceId: SourceId, only: CurrencyPair? = null): SyncOutcome
}

internal class RatesRepositoryImpl(
    private val ratesDao: RatesDao,
    private val watchedPairsDao: WatchedPairsDao,
    private val sources: Map<SourceId, RatesRemoteSource>,
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
                        .filter { rate -> rate.describes(pair) }
                        .firstNotNullOfOrNull { rate -> rate.toRatePoint() }
                )
            }
        }

    override suspend fun refresh(sourceId: SourceId, only: CurrencyPair?): SyncOutcome {
        val watched = watchedPairsDao.getAll().map { entity -> entity.toWatchedPair() }
        val selected = if (only == null) watched else watched.filter { it.pair == only }
        val source = sources[sourceId]
            ?: return SyncOutcome.Permanent(PermanentKind.UnknownSource)

        if (selected.isEmpty()) {
            return SyncOutcome.Success(0)
        }

        var updated = 0
        var failure: Throwable? = null

        selected.groupBy({ it.provider }, { it.pair }).forEach { (provider, pairs) ->
            try {
                updated += fetch(source, sourceId, provider, pairs)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                failure = failure ?: e
            }
        }

        return when {
            updated > 0 -> SyncOutcome.Success(updated, selected.size)
            failure != null -> failure.toSyncOutcome()
            else -> SyncOutcome.Success(0, selected.size)
        }
    }

    private suspend fun fetch(
        source: RatesRemoteSource,
        sourceId: SourceId,
        provider: ProviderKey?,
        pairs: List<CurrencyPair>,
    ): Int {
        val fetchedAt = clock.now().toEpochMilliseconds()
        val returned = source.latest(provider, pairs)

        ratesDao.upsertAll(
            returned.map { remote -> remote.toRateEntity(sourceId, provider, fetchedAt) }
        )

        return pairs.count { pair ->
            returned.any { remote -> remote.base == pair.base && remote.quote == pair.quote }
        }
    }
}

internal fun RateEntity.describes(pair: WatchedPairEntity): Boolean =
    base == pair.base && quote == pair.quote && provider == pair.provider

internal fun RateEntity.toRatePoint(): RatePoint? {
    val source = sourceId.toSourceId() ?: return null
    val parsed = Rate.parse(value) ?: return null

    return RatePoint(
        date = LocalDate.parse(date),
        value = parsed,
        sourceId = source,
        provider = provider.toProviderKey()
    )
}

internal fun RemoteRate.toRateEntity(
    sourceId: SourceId,
    provider: ProviderKey?,
    fetchedAt: Long,
) = RateEntity(
    base = base.code,
    quote = quote.code,
    date = date.toString(),
    sourceId = sourceId.toColumn(),
    provider = provider.toColumn(),
    value = value.toPlainString(),
    fetchedAt = fetchedAt
)

internal fun WatchedPairEntity.toWatchedPair() = WatchedPair(
    id = id,
    pair = CurrencyPair(
        base = CurrencyCode(base),
        quote = CurrencyCode(quote),
    ),
    provider = provider.toProviderKey(),
    position = position
)

internal fun String.toProviderKey(): ProviderKey? = takeIf { it.isNotBlank() }?.let(::ProviderKey)

internal fun ProviderKey?.toColumn(): String = this?.key.orEmpty()