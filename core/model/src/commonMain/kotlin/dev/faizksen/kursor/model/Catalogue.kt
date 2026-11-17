package dev.faizksen.kursor.model

data class Catalogue(
    val currencies: List<Currency>,
    val providers: List<Provider>,
    val coverage: List<ProviderCoverage>,
)

data class ProviderCoverage(
    val provider: ProviderKey,
    val currency: CurrencyCode,
)
