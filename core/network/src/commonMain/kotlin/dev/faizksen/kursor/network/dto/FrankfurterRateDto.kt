package dev.faizksen.kursor.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class FrankfurterRateDto(
    val date: String,
    val base: String,
    val quote: String,
    val rate: Double,
)
