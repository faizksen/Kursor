package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.CatalogueRepository
import dev.faizksen.kursor.data.RatesRepository
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.PublishCadence
import dev.faizksen.kursor.model.WatchedPairWithRate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlin.time.Clock

fun interface ObserveWatchlistUseCase {
    operator fun invoke(): Flow<List<WatchedPairWithRate>>
}

internal class ObserveWatchlistUseCaseImpl(
    private val ratesRepository: RatesRepository,
    private val catalogueRepository: CatalogueRepository,
    private val clock: Clock,
): ObserveWatchlistUseCase {

    override operator fun invoke(): Flow<List<WatchedPairWithRate>> = ratesRepository
        .observeWatchlist()
        .map { watchlist -> watchlist.markQuietOnes() }

    private suspend fun List<WatchedPairWithRate>.markQuietOnes(): List<WatchedPairWithRate> {
        val zone = TimeZone.currentSystemDefault()
        val today = clock.now().toLocalDate(zone)
        val cadences = cadencesOf(mapNotNull { watched -> watched.watchedPair.provider })

        return map { watched ->
            watched.copy(
                quiet = hasGoneQuiet(
                    latestRate = watched.rate?.date,
                    addedAt = watched.watchedPair.addedAt.toLocalDate(zone),
                    cadence = cadences[watched.watchedPair.provider] ?: PublishCadence.UNKNOWN,
                    today = today,
                )
            )
        }
    }

    private suspend fun cadencesOf(keys: List<ProviderKey>): Map<ProviderKey, PublishCadence> =
        if (keys.isEmpty()) {
            emptyMap()
        } else {
            catalogueRepository
                .providers(keys.distinct())
                .associate { provider -> provider.key to provider.cadence }
        }
}
