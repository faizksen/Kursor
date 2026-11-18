package dev.faizksen.kursor.watchlist

import com.google.common.truth.Truth.assertThat
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.Rate
import dev.faizksen.kursor.model.RatePoint
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.WatchedPair
import dev.faizksen.kursor.model.WatchedPairWithRate
import kotlinx.datetime.LocalDate
import org.junit.Test
import kotlin.time.Instant

class DisplayedPairTest {

    @Test
    fun `a pair left alone reads the way the issuer publishes it`() {
        val watched = watched(inverted = false)

        assertThat(watched.watchedPair.displayedPair())
            .isEqualTo(CurrencyPair(CurrencyCode("PHP"), CurrencyCode("USD")))
        assertThat(watched.displayedValue()).isEqualTo(Rate.parse("0.0157"))
    }

    @Test
    fun `a flipped pair swaps both the label and the number`() {
        val watched = watched(inverted = true)

        assertThat(watched.watchedPair.displayedPair())
            .isEqualTo(CurrencyPair(CurrencyCode("USD"), CurrencyCode("PHP")))
        assertThat(watched.displayedValue()?.toPlainString()).isEqualTo("63.69426752")
    }

    @Test
    fun `a flipped pair with no rate yet still shows no number`() {
        assertThat(watched(inverted = true, value = null).displayedValue()).isNull()
    }

    @Test
    fun `the stored pair is untouched by how it is shown`() {
        val watched = watched(inverted = true)

        assertThat(watched.watchedPair.pair)
            .isEqualTo(CurrencyPair(CurrencyCode("PHP"), CurrencyCode("USD")))
        assertThat(watched.rate?.value).isEqualTo(Rate.parse("0.0157"))
    }

    private fun watched(inverted: Boolean, value: String? = "0.0157") = WatchedPairWithRate(
        watchedPair = WatchedPair(
            id = 1,
            pair = CurrencyPair(CurrencyCode("PHP"), CurrencyCode("USD")),
            provider = ProviderKey("IMF"),
            position = 0,
            addedAt = Instant.parse("2026-11-18T08:00:00Z"),
            inverted = inverted,
        ),
        rate = value?.let {
            RatePoint(
                date = LocalDate.parse("2026-11-17"),
                value = Rate.parse(it)!!,
                sourceId = SourceId.FRANKFURTER,
                provider = ProviderKey("IMF"),
            )
        },
    )
}
