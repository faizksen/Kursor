package dev.faizksen.kursor.model

import kotlinx.datetime.LocalDate

data class RatePoint(
    val date: LocalDate,
    val value: Rate,
    val sourceId: SourceId
)
