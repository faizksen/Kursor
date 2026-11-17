package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.WatchedPairsRepository

fun interface ResolveMissingProvidersUseCase {
    suspend operator fun invoke()
}

internal class ResolveMissingProvidersUseCaseImpl(
    private val watchedPairsRepository: WatchedPairsRepository,
    private val providerCandidates: ProviderCandidatesUseCase,
): ResolveMissingProvidersUseCase {

    override suspend fun invoke() {
        watchedPairsRepository.withoutProvider().forEach { watched ->
            val recommended = providerCandidates(watched.pair).shortlist.firstOrNull()

            if (recommended != null) {
                watchedPairsRepository.setProvider(watched.id, recommended.key)
            }
        }
    }
}
