package dev.faizksen.kursor.model

import org.junit.Test

class CurrencyCodeTest {

    @Test(expected = IllegalArgumentException::class)
    fun `fails on blanc currency code`() {
        CurrencyCode(code = "")
    }
}