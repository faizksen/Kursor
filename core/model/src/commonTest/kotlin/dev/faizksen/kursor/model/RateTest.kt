package dev.faizksen.kursor.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RateTest {

    @Test
    fun `a rate survives the round trip through text`() {
        assertEquals("4.2718", Rate.parse("4.2718")?.toPlainString())
    }

    @Test
    fun `a whole number keeps no decimal point`() {
        assertEquals("4", Rate.parse("4")?.toPlainString())
    }

    @Test
    fun `zero is written as a single digit`() {
        assertEquals("0", Rate.parse("0")?.toPlainString())
        assertEquals("0", Rate.parse("0.000")?.toPlainString())
    }

    @Test
    fun `trailing zeroes are dropped`() {
        assertEquals("4.2718", Rate.parse("4.27180000")?.toPlainString())
    }

    @Test
    fun `the whole precision is kept`() {
        assertEquals(1L, Rate.parse("0.00000001")?.units)
        assertEquals("0.00000001", Rate.parse("0.00000001")?.toPlainString())
    }

    @Test
    fun `a ninth digit of five rounds up`() {
        assertEquals(112_345_679L, Rate.parse("1.123456785")?.units)
    }

    @Test
    fun `a ninth digit below five is dropped`() {
        assertEquals(112_345_678L, Rate.parse("1.123456784")?.units)
    }

    @Test
    fun `digits past the ninth do not carry`() {
        assertEquals(112_345_678L, Rate.parse("1.1234567849999")?.units)
    }

    @Test
    fun `a sign is read and written back`() {
        assertEquals(-427_180_000L, Rate.parse("-4.2718")?.units)
        assertEquals("-4.2718", Rate.parse("-4.2718")?.toPlainString())
        assertEquals(Rate.parse("4.2718"), Rate.parse("+4.2718"))
    }

    @Test
    fun `surrounding spaces are ignored`() {
        assertEquals(Rate.parse("4.2718"), Rate.parse("  4.2718  "))
    }

    @Test
    fun `a missing part on either side of the point is allowed`() {
        assertEquals(50_000_000L, Rate.parse(".5")?.units)
        assertEquals(500_000_000L, Rate.parse("5.")?.units)
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
            assertNull(Rate.parse(text), "parsing $text")
        }
    }

    @Test
    fun `a value too large to hold is refused`() {
        assertNull(Rate.parse("99999999999999999999"))
        assertNull(Rate.parse("92233720368"))
        assertNotNull(Rate.parse("92233720367"))
    }

    @Test
    fun `a double is taken without a floating point artefact`() {
        assertEquals("1.0845", Rate.ofDouble(1.0845).toPlainString())
        assertEquals("4.2718", Rate.ofDouble(4.2718).toPlainString())
    }

    @Test
    fun `the smallest step survives a double`() {
        assertEquals(1L, Rate.ofDouble(0.00000001).units)
    }

    @Test
    fun `a double round trips back to itself`() {
        assertEquals(1.0845, Rate.ofDouble(1.0845).toDouble())
    }

    @Test
    fun `two equal rates are equal whatever text they came from`() {
        assertEquals(Rate.parse("4.2700"), Rate.parse("4.27"))
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
            assertEquals(units, Rate.parse(text)?.units, "parsing $text")
        }
    }
}
