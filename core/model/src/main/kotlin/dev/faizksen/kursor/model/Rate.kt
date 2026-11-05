package dev.faizksen.kursor.model

import kotlin.math.absoluteValue
import kotlin.math.roundToLong

@JvmInline
value class Rate(val units: Long) {

    fun toDouble(): Double = units.toDouble() / UNITS_PER_WHOLE

    fun toPlainString(): String {
        val sign = if (units < 0) "-" else ""
        val abs = units.absoluteValue
        val whole = abs / UNITS_PER_WHOLE
        val fraction = (abs % UNITS_PER_WHOLE)
            .toString()
            .padStart(SCALE, '0')
            .trimEnd('0')

        return if (fraction.isEmpty()) "$sign$whole" else "$sign$whole.$fraction"
    }

    companion object {
        const val SCALE = 8
        const val UNITS_PER_WHOLE = 100_000_000L

        private const val MAX_WHOLE = Long.MAX_VALUE / UNITS_PER_WHOLE

        fun ofDouble(value: Double) = Rate((value * UNITS_PER_WHOLE).roundToLong())

        fun parse(text: String): Rate? {
            val trimmed = text.trim()
            if (trimmed.isEmpty()) return null

            val negative = trimmed.startsWith('-')
            val digits = trimmed.removePrefix("-").removePrefix("+")
            val dot = digits.indexOf('.')
            val whole = if (dot < 0) digits else digits.substring(0, dot)
            val fraction = if (dot < 0) "" else digits.substring(dot + 1)

            if (!whole.isDecimalDigits() || !fraction.isDecimalDigits()) return null
            if (whole.isEmpty() && fraction.isEmpty()) return null

            val wholeUnits = whole.ifEmpty { "0" }.toLongOrNull() ?: return null
            if (wholeUnits >= MAX_WHOLE) return null

            val scaled = fraction.padEnd(SCALE + 1, '0')
            val fractionUnits = scaled.take(SCALE).toLong()
            val roundUp = scaled[SCALE] >= '5'

            val total = wholeUnits * UNITS_PER_WHOLE + fractionUnits + if (roundUp) 1 else 0

            return Rate(if (negative) -total else total)
        }

        private fun String.isDecimalDigits(): Boolean = all { it in '0'..'9' }
    }
}
