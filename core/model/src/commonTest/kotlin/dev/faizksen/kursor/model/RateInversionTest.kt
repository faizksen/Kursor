package dev.faizksen.kursor.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RateInversionTest {

    @Test
    fun aRateAboveOneBecomesOneBelow() {
        assertEquals("0.22710756", invert("4.4032"))
    }

    @Test
    fun aRateFarBelowOneKeepsItsPrecisionWhenFlipped() {
        assertEquals("63.69426752", invert("0.0157"))
    }

    @Test
    fun flippingTwiceComesBackToWhereItStarted() {
        val original = Rate.parse("1.0989")!!

        assertEquals(original, original.inverted()?.inverted())
    }

    @Test
    fun oneIsItsOwnInverse() {
        assertEquals("1", invert("1"))
    }

    @Test
    fun aLargeRateSurvivesTheFlip() {
        assertEquals("0.01096554", invert("91.1948"))
    }

    @Test
    fun theSignIsKept() {
        assertEquals("-0.5", invert("-2"))
    }

    @Test
    fun thereIsNoInverseOfZero() {
        assertNull(Rate(0).inverted())
    }

    private fun invert(text: String): String? =
        Rate.parse(text)?.inverted()?.toPlainString()
}
