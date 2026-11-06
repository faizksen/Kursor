package dev.faizksen.kursor.model

import kotlin.test.Test
import kotlin.test.assertFailsWith

class CurrencyCodeTest {

    @Test
    fun `fails on blanc currency code`() {
        assertFailsWith<IllegalArgumentException> {
            CurrencyCode(code = "")
        }
    }
}
