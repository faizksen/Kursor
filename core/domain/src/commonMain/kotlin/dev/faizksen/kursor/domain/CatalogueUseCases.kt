package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.CatalogueRepository
import dev.faizksen.kursor.model.Currency
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

data class PickableCurrencies(
    val currencies: List<Currency>,
    val tooOldToPick: Boolean,
)

fun interface PickableCurrenciesUseCase {
    suspend operator fun invoke(): PickableCurrencies
}

internal class PickableCurrenciesUseCaseImpl(
    private val catalogueRepository: CatalogueRepository,
    private val clock: Clock,
): PickableCurrenciesUseCase {

    override suspend fun invoke(): PickableCurrencies {
        val fetchedAt = catalogueRepository.fetchedAt()
        val tooOld = fetchedAt == null || clock.now() - fetchedAt >= CATALOGUE_MAX_AGE

        return PickableCurrencies(
            currencies = if (tooOld) emptyList() else catalogueRepository.fiatCurrencies(),
            tooOldToPick = tooOld,
        )
    }
}

fun interface RefreshCatalogueUseCase {
    suspend operator fun invoke()
}

internal class RefreshCatalogueUseCaseImpl(
    private val catalogueRepository: CatalogueRepository,
    private val clock: Clock,
): RefreshCatalogueUseCase {

    override suspend fun invoke() {
        val fetchedAt = catalogueRepository.fetchedAt()

        if (fetchedAt == null || clock.now() - fetchedAt >= CATALOGUE_TTL) {
            catalogueRepository.refresh()
        }
    }
}

internal val CATALOGUE_TTL = 1.hours
internal val CATALOGUE_MAX_AGE = 1.days
