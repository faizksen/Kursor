package dev.faizksen.kursor.network.util

import kotlinx.serialization.json.Json

object JsonCreator {
    fun create() = Json { ignoreUnknownKeys = true }
}