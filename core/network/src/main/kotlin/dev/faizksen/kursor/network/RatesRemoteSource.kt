package dev.faizksen.kursor.network

import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.network.model.RemoteRate

interface RatesRemoteSource {
    val id: SourceId
    suspend fun latest(base: CurrencyCode, symbols: Set<CurrencyCode>): List<RemoteRate>
}