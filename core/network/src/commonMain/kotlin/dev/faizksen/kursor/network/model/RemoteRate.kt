package dev.faizksen.kursor.network.model

import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.Rate
import kotlinx.datetime.LocalDate

data class RemoteRate(
    val base: CurrencyCode,
    val quote: CurrencyCode,
    val date: LocalDate,
    val value: Rate
)
