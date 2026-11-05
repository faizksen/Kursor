package dev.faizksen.kursor.model

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

class RateTest {

    @Test
    fun `a rate survives the round trip through text`() {
        assertThat(Rate.parse("4.2718")?.toPlainString()).isEqualTo("4.2718")
    }

    @Test
    fun `a whole number keeps no decimal point`() {
        assertThat(Rate.parse("4")?.toPlainString()).isEqualTo("4")
    }

    @Test
    fun `zero is written as a single digit`() {
        assertThat(Rate.parse("0")?.toPlainString()).isEqualTo("0")
        assertThat(Rate.parse("0.000")?.toPlainString()).isEqualTo("0")
    }

    @Test
    fun `trailing zeroes are dropped`() {
        assertThat(Rate.parse("4.27180000")?.toPlainString()).isEqualTo("4.2718")
    }

    @Test
    fun `the whole precision is kept`() {
        assertThat(Rate.parse("0.00000001")?.units).isEqualTo(1)
        assertThat(Rate.parse("0.00000001")?.toPlainString()).isEqualTo("0.00000001")
    }

    @Test
    fun `a ninth digit of five rounds up`() {
        assertThat(Rate.parse("1.123456785")?.units).isEqualTo(112_345_679)
    }

    @Test
    fun `a ninth digit below five is dropped`() {
        assertThat(Rate.parse("1.123456784")?.units).isEqualTo(112_345_678)
    }

    @Test
    fun `digits past the ninth do not carry`() {
        assertThat(Rate.parse("1.1234567849999")?.units).isEqualTo(112_345_678)
    }

    @Test
    fun `a sign is read and written back`() {
        assertThat(Rate.parse("-4.2718")?.units).isEqualTo(-427_180_000)
        assertThat(Rate.parse("-4.2718")?.toPlainString()).isEqualTo("-4.2718")
        assertThat(Rate.parse("+4.2718")).isEqualTo(Rate.parse("4.2718"))
    }

    @Test
    fun `surrounding spaces are ignored`() {
        assertThat(Rate.parse("  4.2718  ")).isEqualTo(Rate.parse("4.2718"))
    }

    @Test
    fun `a missing part on either side of the point is allowed`() {
        assertThat(Rate.parse(".5")?.units).isEqualTo(50_000_000)
        assertThat(Rate.parse("5.")?.units).isEqualTo(500_000_000)
    }

    @Test
    fun `text that is not a decimal number is refused`() {
        listOf(
            "",
            "   ",
            ".",
            "abc",
            "1.2.3",
            "1,5",
            "4 2",
            "1e-6",
            "0x10",
            "٤",
        ).forEach { text ->
            assertWithMessage("parsing %s", text).that(Rate.parse(text)).isNull()
        }
    }

    @Test
    fun `a value too large to hold is refused`() {
        assertThat(Rate.parse("99999999999999999999")).isNull()
        assertThat(Rate.parse("92233720368")).isNull()
        assertThat(Rate.parse("92233720367")).isNotNull()
    }

    @Test
    fun `a double is taken without a floating point artefact`() {
        assertThat(Rate.ofDouble(1.0845).toPlainString()).isEqualTo("1.0845")
        assertThat(Rate.ofDouble(4.2718).toPlainString()).isEqualTo("4.2718")
    }

    @Test
    fun `the smallest step survives a double`() {
        assertThat(Rate.ofDouble(0.00000001).units).isEqualTo(1)
    }

    @Test
    fun `a double round trips back to itself`() {
        assertThat(Rate.ofDouble(1.0845).toDouble()).isEqualTo(1.0845)
    }

    @Test
    fun `two equal rates are equal whatever text they came from`() {
        assertThat(Rate.parse("4.27")).isEqualTo(Rate.parse("4.2700"))
    }

    @Test
    fun `what the old decimal column holds is still readable`() {
        val stored = mapOf(
            "1.0845" to 108_450_000L,
            "0.9231" to 92_310_000L,
            "162.74" to 16_274_000_000L,
            "0.0000010" to 100L,
            "87" to 8_700_000_000L,
        )

        stored.forEach { (text, units) ->
            assertWithMessage("parsing %s", text).that(Rate.parse(text)?.units).isEqualTo(units)
        }
    }
}
