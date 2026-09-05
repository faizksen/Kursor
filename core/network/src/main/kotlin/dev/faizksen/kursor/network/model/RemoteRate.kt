package dev.faizksen.kursor.network.model

import dev.faizksen.kursor.model.CurrencyCode
import java.math.BigDecimal
import java.time.LocalDate

data class RemoteRate(
    val base: CurrencyCode,
    val quote: CurrencyCode,
    val date: LocalDate,
    val value: BigDecimal
)