package dev.faizksen.kursor.domain

import dev.faizksen.kursor.model.PublishCadence
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

internal fun hasGoneQuiet(
    latestRate: LocalDate?,
    addedAt: LocalDate,
    cadence: PublishCadence,
    today: LocalDate,
): Boolean = (latestRate ?: addedAt) < today.minus(DatePeriod(days = cadence.toleranceDays))

internal fun Instant.toLocalDate(zone: TimeZone): LocalDate = toLocalDateTime(zone).date

private val PublishCadence.toleranceDays: Int
    get() = when (this) {
        PublishCadence.DAILY, PublishCadence.UNKNOWN -> DAILY_TOLERANCE_DAYS
        PublishCadence.WEEKLY -> WEEKLY_TOLERANCE_DAYS
        PublishCadence.MONTHLY -> MONTHLY_TOLERANCE_DAYS
        PublishCadence.QUARTERLY -> QUARTERLY_TOLERANCE_DAYS
    }

private const val DAILY_TOLERANCE_DAYS = 4
private const val WEEKLY_TOLERANCE_DAYS = 9
private const val MONTHLY_TOLERANCE_DAYS = 35
private const val QUARTERLY_TOLERANCE_DAYS = 100
