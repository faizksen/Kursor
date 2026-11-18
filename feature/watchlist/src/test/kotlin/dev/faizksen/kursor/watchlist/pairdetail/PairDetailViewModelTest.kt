package dev.faizksen.kursor.watchlist.pairdetail

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.Provider
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.PublishCadence
import dev.faizksen.kursor.model.Rate
import dev.faizksen.kursor.model.RatePoint
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.WatchedPair
import dev.faizksen.kursor.model.WatchedPairDetail
import dev.faizksen.kursor.model.WatchedPairWithRate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.time.Instant

class PairDetailViewModelTest {

    private val detail = MutableStateFlow<WatchedPairDetail?>(null)
    private var deletedId: Long? = null

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the screen waits before it claims the pair is missing`() = runTest {
        viewModel().screenState.test {
            assertThat(awaitItem()).isEqualTo(PairDetailUiState.Loading)
        }
    }

    @Test
    fun `a watched pair arrives with its issuer`() = runTest {
        detail.value = WatchedPairDetail(watched = watched(), provider = NBP)

        viewModel().screenState.test {
            skipItems(1)

            val state = awaitItem() as PairDetailUiState.Watching

            assertThat(state.provider?.name).isEqualTo("Narodowy Bank Polski")
            assertThat(state.watched.watchedPair.pair.base).isEqualTo(CurrencyCode("EUR"))
        }
    }

    @Test
    fun `deleting the pair elsewhere turns the screen into a dead end`() = runTest {
        detail.value = WatchedPairDetail(watched = watched(), provider = NBP)

        viewModel().screenState.test {
            skipItems(2)

            detail.value = null

            assertThat(awaitItem()).isEqualTo(PairDetailUiState.Gone)
        }
    }

    @Test
    fun `a new rate reaches the open screen`() = runTest {
        detail.value = WatchedPairDetail(watched = watched(), provider = NBP)

        viewModel().screenState.test {
            skipItems(2)

            detail.value = WatchedPairDetail(
                watched = watched(value = "4.5000"),
                provider = NBP,
            )

            val state = awaitItem() as PairDetailUiState.Watching

            assertThat(state.watched.rate?.value).isEqualTo(Rate.parse("4.5000"))
        }
    }

    @Test
    fun `deleting from the menu removes the pair that was open`() = runTest {
        detail.value = WatchedPairDetail(watched = watched(), provider = NBP)

        val viewModel = viewModel()

        viewModel.screenState.test { skipItems(2) }
        viewModel.obtainAction(PairDetailUiAction.DeleteClicked)
        runCurrent()

        assertThat(deletedId).isEqualTo(1L)
    }

    @Test
    fun `the screen asks to be closed once the pair is gone`() = runTest {
        detail.value = WatchedPairDetail(watched = watched(), provider = NBP)

        val viewModel = viewModel()

        viewModel.effect.test {
            viewModel.obtainAction(PairDetailUiAction.DeleteClicked)

            assertThat(awaitItem()).isEqualTo(PairDetailUiEffect.Deleted)
        }
    }

    @Test
    fun `a pair whose issuer went quiet says so on the detail screen too`() = runTest {
        detail.value = WatchedPairDetail(
            watched = watched().copy(quiet = true),
            provider = NBP,
        )

        viewModel().screenState.test {
            skipItems(1)

            assertThat((awaitItem() as PairDetailUiState.Watching).watched.quiet).isTrue()
        }
    }

    private fun viewModel() = PairDetailViewModel(
        pairId = 1,
        observePairDetail = { detail },
        deletePair = { id ->
            deletedId = id
            detail.value = null
            PAIR
        },
    )

    private fun watched(value: String = "4.4032") = WatchedPairWithRate(
        watchedPair = WatchedPair(
            id = 1,
            pair = CurrencyPair(CurrencyCode("EUR"), CurrencyCode("PLN")),
            provider = NBP.key,
            position = 0,
            addedAt = Instant.parse("2026-11-01T09:00:00Z"),
        ),
        rate = RatePoint(
            date = LocalDate.parse("2026-11-17"),
            value = Rate.parse(value)!!,
            sourceId = SourceId.FRANKFURTER,
            provider = NBP.key,
        ),
    )

    private companion object {
        val PAIR = CurrencyPair(CurrencyCode("EUR"), CurrencyCode("PLN"))

        val NBP = Provider(
            key = ProviderKey("NBP"),
            name = "Narodowy Bank Polski",
            rateType = "official",
            pivotCurrency = CurrencyCode("PLN"),
            cadence = PublishCadence.DAILY,
            latestDate = LocalDate.parse("2026-11-17"),
            dataUrl = null,
        )
    }
}
