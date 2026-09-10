package dev.faizksen.kursor.model

import java.math.BigDecimal
import java.time.LocalDate

data class RatePoint(
    val date: LocalDate,
    val value: BigDecimal,
    val sourceId: SourceId
)
