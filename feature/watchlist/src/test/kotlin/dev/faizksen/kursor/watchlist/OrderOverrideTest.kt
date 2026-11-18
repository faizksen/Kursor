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

class OrderOverrideTest {

    private val pairs = listOf(watched(1), watched(2), watched(3))

    @Test
    fun `without a chosen order the list comes through untouched`() {
        assertThat(pairs.inOrderOf(null)).isEqualTo(pairs)
    }

    @Test
    fun `the chosen order wins over the stored one`() {
        assertThat(pairs.inOrderOf(listOf(3L, 1L, 2L)).ids()).containsExactly(3L, 1L, 2L).inOrder()
    }

    @Test
    fun `a rate that arrives mid drag is not lost`() {
        val refreshed = listOf(watched(1), watched(2, value = "9.9999"), watched(3))

        val shown = refreshed.inOrderOf(listOf(3L, 2L, 1L))

        assertThat(shown.ids()).containsExactly(3L, 2L, 1L).inOrder()
        assertThat(shown.first { it.watchedPair.id == 2L }.rate?.value)
            .isEqualTo(Rate.parse("9.9999"))
    }

    @Test
    fun `a pair added during the drag lands at the end rather than vanishing`() {
        val grown = pairs + watched(4)

        assertThat(grown.inOrderOf(listOf(3L, 1L, 2L)).ids())
            .containsExactly(3L, 1L, 2L, 4L).inOrder()
    }

    @Test
    fun `a pair deleted during the drag simply drops out`() {
        val shrunk = listOf(watched(1), watched(3))

        assertThat(shrunk.inOrderOf(listOf(3L, 1L, 2L)).ids()).containsExactly(3L, 1L).inOrder()
    }

    private fun List<WatchedPairWithRate>.ids() = map { it.watchedPair.id }

    private fun watched(id: Long, value: String = "1.0") = WatchedPairWithRate(
        watchedPair = WatchedPair(
            id = id,
            pair = CurrencyPair(CurrencyCode("EUR"), CurrencyCode("USD")),
            provider = ProviderKey("ECB"),
            position = id.toInt(),
            addedAt = Instant.parse("2026-11-18T09:00:00Z"),
        ),
        rate = RatePoint(
            date = LocalDate.parse("2026-11-17"),
            value = Rate.parse(value)!!,
            sourceId = SourceId.FRANKFURTER,
            provider = ProviderKey("ECB"),
        ),
    )
}
