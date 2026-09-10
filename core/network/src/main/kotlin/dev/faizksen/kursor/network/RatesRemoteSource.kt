package dev.faizksen.kursor.network

import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.network.model.RemoteRate

interface RatesRemoteSource {
    val id: SourceId
    suspend fun latest(pairs: List<CurrencyPair>): List<RemoteRate>
}