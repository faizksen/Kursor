package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.CatalogueRepository
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.Provider

data class ProviderCandidates(
    val shortlist: List<Provider>,
    val all: List<Provider>,
)

fun interface ProviderCandidatesUseCase {
    suspend operator fun invoke(pair: CurrencyPair): ProviderCandidates
}

internal class ProviderCandidatesUseCaseImpl(
    private val catalogueRepository: CatalogueRepository,
): ProviderCandidatesUseCase {

    override suspend fun invoke(pair: CurrencyPair): ProviderCandidates {
        val ranked = catalogueRepository.providersFor(pair).rankFor(pair)

        return ProviderCandidates(
            shortlist = ranked.topWithIssuers(pair, PROVIDER_SHORTLIST),
            all = ranked,
        )
    }
}
