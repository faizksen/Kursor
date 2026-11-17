package dev.faizksen.kursor.data

import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.network.RatesRemoteSource
import dev.faizksen.kursor.network.model.RemoteRate

class FakeRatesRemoteSource(
    override val id: SourceId = SourceId.FRANKFURTER,
    private val result: Result<List<RemoteRate>> = Result.success(emptyList()),
    private val perProvider: Map<ProviderKey?, Result<List<RemoteRate>>> = emptyMap(),
) : RatesRemoteSource {

    var callCount: Int = 0
        private set

    var lastRequestedPairs: List<CurrencyPair>? = null
        private set

    val askedProviders: MutableList<ProviderKey?> = mutableListOf()

    override suspend fun latest(
        provider: ProviderKey?,
        pairs: List<CurrencyPair>,
    ): List<RemoteRate> {
        callCount++
        lastRequestedPairs = pairs
        askedProviders += provider

        return (perProvider[provider] ?: result).getOrThrow()
    }
}
