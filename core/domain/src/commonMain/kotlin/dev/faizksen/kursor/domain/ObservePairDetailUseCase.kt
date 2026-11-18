package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.CatalogueRepository
import dev.faizksen.kursor.model.WatchedPairDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun interface ObservePairDetailUseCase {
    operator fun invoke(id: Long): Flow<WatchedPairDetail?>
}

internal class ObservePairDetailUseCaseImpl(
    private val observeWatchlist: ObserveWatchlistUseCase,
    private val catalogueRepository: CatalogueRepository,
): ObservePairDetailUseCase {

    override fun invoke(id: Long): Flow<WatchedPairDetail?> = observeWatchlist()
        .map { watchlist -> watchlist.firstOrNull { watched -> watched.watchedPair.id == id } }
        .map { watched ->
            if (watched == null) {
                null
            } else {
                WatchedPairDetail(
                    watched = watched,
                    provider = watched.watchedPair.provider?.let { key ->
                        catalogueRepository.providers(listOf(key)).firstOrNull()
                    },
                )
            }
        }
}
