package dev.faizksen.kursor.watchlist.addpair

import app.cash.turbine.test
import dev.faizksen.kursor.domain.AddPairOutcome
import dev.faizksen.kursor.domain.PickableCurrencies
import dev.faizksen.kursor.domain.ProviderCandidates
import dev.faizksen.kursor.model.Currency
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.CurrencyType
import dev.faizksen.kursor.model.Provider
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.PublishCadence
import dev.faizksen.kursor.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddPairViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    private var pickable = PickableCurrencies(currencies = CURRENCIES, tooOldToPick = false)
    private var candidates = ProviderCandidates(shortlist = listOf(NBP, ECB), all = listOf(NBP, ECB, HNB))
    private var outcome = AddPairOutcome.ADDED
    private var addedPair: CurrencyPair? = null
    private var addedProvider: ProviderKey? = null
    private var askedFor: CurrencyPair? = null

    private fun viewModel() = AddPairViewModel(
        pickableCurrencies = { pickable },
        providerCandidates = { pair ->
            askedFor = pair
            candidates
        },
        addWatchedPair = { pair, provider ->
            addedPair = pair
            addedProvider = provider
            outcome
        },
    )

    @Test
    fun theFirstStepOffersEveryFiatCurrency() = runTest(dispatcher) {
        val model = viewModel()
        runCurrent()

        val state = model.picking()
        assertEquals(AddPairStep.BASE, state.step)
        assertEquals(CURRENCIES, state.currencies)
        assertNull(state.base)
    }

    @Test
    fun aCatalogueOlderThanADayBlocksAdding() = runTest(dispatcher) {
        pickable = PickableCurrencies(currencies = emptyList(), tooOldToPick = true)

        val model = viewModel()
        runCurrent()

        assertEquals(AddPairUiState.CatalogueTooOld, model.screenState.value)
    }

    @Test
    fun pickingTheBaseMovesToTheQuoteAndClearsTheQuery() = runTest(dispatcher) {
        val model = viewModel()
        runCurrent()

        model.obtainAction(AddPairUiAction.QueryChanged("zlo"))
        model.obtainAction(AddPairUiAction.CurrencyPicked(EUR))
        runCurrent()

        val state = model.picking()
        assertEquals(AddPairStep.QUOTE, state.step)
        assertEquals(EUR, state.base)
        assertEquals("", state.query)
    }

    @Test
    fun pickingTheQuoteLoadsTheRankedProvidersForThatPair() = runTest(dispatcher) {
        val model = viewModel()
        runCurrent()

        model.obtainAction(AddPairUiAction.CurrencyPicked(EUR))
        model.obtainAction(AddPairUiAction.CurrencyPicked(PLN))
        runCurrent()

        val state = model.picking()
        assertEquals(AddPairStep.PROVIDER, state.step)
        assertEquals(CurrencyPair(base = EUR, quote = PLN), askedFor)
        assertEquals(listOf(NBP, ECB), state.providers)
        assertEquals(3, state.everyProviderCount)
    }

    @Test
    fun theTopRankedProviderIsSelectedWithoutTheUserTouchingAnything() = runTest(dispatcher) {
        val model = viewModel()
        runCurrent()

        model.obtainAction(AddPairUiAction.CurrencyPicked(EUR))
        model.obtainAction(AddPairUiAction.CurrencyPicked(PLN))
        runCurrent()

        assertEquals(NBP.key, model.picking().provider)
    }

    @Test
    fun showingEveryProviderNeedsNoFurtherLoad() = runTest(dispatcher) {
        val model = viewModel()
        runCurrent()

        model.obtainAction(AddPairUiAction.CurrencyPicked(EUR))
        model.obtainAction(AddPairUiAction.CurrencyPicked(PLN))
        runCurrent()
        askedFor = null
        model.obtainAction(AddPairUiAction.ShowEveryProviderClicked)

        val state = model.picking()
        assertEquals(listOf(NBP, ECB, HNB), state.providers)
        assertTrue(state.showingEveryProvider)
        assertNull(askedFor)
    }

    @Test
    fun backFromTheProviderStepForgetsTheQuoteAndItsProviders() = runTest(dispatcher) {
        val model = viewModel()
        runCurrent()

        model.obtainAction(AddPairUiAction.CurrencyPicked(EUR))
        model.obtainAction(AddPairUiAction.CurrencyPicked(PLN))
        runCurrent()
        model.obtainAction(AddPairUiAction.BackClicked)

        val state = model.picking()
        assertEquals(AddPairStep.QUOTE, state.step)
        assertEquals(EUR, state.base)
        assertNull(state.quote)
        assertNull(state.provider)
        assertEquals(emptyList<Provider>(), state.providers)
    }

    @Test
    fun backFromTheFirstStepLeavesTheScreen() = runTest(dispatcher) {
        val model = viewModel()
        runCurrent()

        model.effect.test {
            model.obtainAction(AddPairUiAction.BackClicked)
            assertEquals(AddPairUiEffect.Dismissed, awaitItem())
        }
    }

    @Test
    fun confirmingAddsThePairThroughTheChosenProvider() = runTest(dispatcher) {
        val model = viewModel()
        runCurrent()

        model.obtainAction(AddPairUiAction.CurrencyPicked(EUR))
        model.obtainAction(AddPairUiAction.CurrencyPicked(PLN))
        runCurrent()
        model.obtainAction(AddPairUiAction.ProviderPicked(ECB.key))

        model.effect.test {
            model.obtainAction(AddPairUiAction.ConfirmClicked)
            runCurrent()
            assertEquals(AddPairUiEffect.Added, awaitItem())
        }

        assertEquals(CurrencyPair(base = EUR, quote = PLN), addedPair)
        assertEquals(ECB.key, addedProvider)
    }

    @Test
    fun confirmingAPairAlreadyWatchedThroughThatProviderSaysSo() = runTest(dispatcher) {
        outcome = AddPairOutcome.ALREADY_WATCHED
        val model = viewModel()
        runCurrent()

        model.obtainAction(AddPairUiAction.CurrencyPicked(EUR))
        model.obtainAction(AddPairUiAction.CurrencyPicked(PLN))
        runCurrent()

        model.effect.test {
            model.obtainAction(AddPairUiAction.ConfirmClicked)
            runCurrent()
            assertEquals(AddPairUiEffect.AlreadyWatched, awaitItem())
        }
    }

    private fun AddPairViewModel.picking(): AddPairUiState.Picking =
        screenState.value as AddPairUiState.Picking

    private companion object {
        val EUR = CurrencyCode("EUR")
        val PLN = CurrencyCode("PLN")

        fun fiat(code: String) = Currency(
            code = CurrencyCode(code),
            name = code,
            symbol = code,
            type = CurrencyType.FIAT,
        )

        val CURRENCIES = listOf(fiat("EUR"), fiat("PLN"), fiat("USD"))

        fun provider(key: String, pivot: String, rateType: String?) = Provider(
            key = ProviderKey(key),
            name = key,
            rateType = rateType,
            pivotCurrency = CurrencyCode(pivot),
            cadence = PublishCadence.DAILY,
            latestDate = LocalDate.parse("2026-11-17"),
            dataUrl = null,
        )

        val NBP = provider("NBP", "PLN", "average rate")
        val ECB = provider("ECB", "EUR", "reference rate")
        val HNB = provider("HNB", "EUR", null)
    }
}
