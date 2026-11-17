package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.AppStateRepository
import dev.faizksen.kursor.data.WatchedPairsRepository
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.ProviderKey
import kotlinx.coroutines.flow.Flow

fun interface SeedDefaultPairsUseCase {
    suspend operator fun invoke()
}

fun interface ObserveDefaultsSeededUseCase {
    operator fun invoke(): Flow<Boolean>
}

internal class SeedDefaultPairsUseCaseImpl(
    private val watchedPairsRepository: WatchedPairsRepository,
    private val appStateRepository: AppStateRepository,
): SeedDefaultPairsUseCase {

    override suspend fun invoke() {
        if (appStateRepository.defaultsSeeded()) return

        DEFAULT_PAIRS.forEach { (pair, provider) ->
            watchedPairsRepository.add(pair, provider)
        }

        appStateRepository.markDefaultsSeeded()
    }
}

internal class ObserveDefaultsSeededUseCaseImpl(
    private val appStateRepository: AppStateRepository,
): ObserveDefaultsSeededUseCase {

    override fun invoke(): Flow<Boolean> = appStateRepository.observeDefaultsSeeded()
}

internal val DEFAULT_PAIRS: List<Pair<CurrencyPair, ProviderKey>> = listOf(
    pair("EUR", "PLN") to ProviderKey("NBP"),
    pair("EUR", "USD") to ProviderKey("ECB"),
    pair("EUR", "RUB") to ProviderKey("CBR"),
)

private fun pair(base: String, quote: String) =
    CurrencyPair(base = CurrencyCode(base), quote = CurrencyCode(quote))
