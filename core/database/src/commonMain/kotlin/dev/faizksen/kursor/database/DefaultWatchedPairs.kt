package dev.faizksen.kursor.database

internal data class SeedPair(
    val base: String,
    val quote: String,
    val provider: String,
)

internal val DEFAULT_WATCHED_PAIRS = listOf(
    SeedPair(base = "EUR", quote = "PLN", provider = "NBP"),
    SeedPair(base = "EUR", quote = "USD", provider = "ECB"),
    SeedPair(base = "EUR", quote = "RUB", provider = "CBR"),
)

internal const val NO_PROVIDER = ""
