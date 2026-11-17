package dev.faizksen.kursor.domain

import dev.faizksen.kursor.model.PublishCadence
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QuietProviderTest {

    @Test
    fun aDailyIssuerIsFineWhenItPublishedToday() {
        assertFalse(quiet(rate = "2026-11-17", cadence = PublishCadence.DAILY))
    }

    @Test
    fun aDailyIssuerSurvivesALongWeekend() {
        assertFalse(quiet(rate = "2026-11-14", cadence = PublishCadence.DAILY))
    }

    @Test
    fun aDailyIssuerSilentForMoreThanFourDaysHasGoneQuiet() {
        assertTrue(quiet(rate = "2026-11-12", cadence = PublishCadence.DAILY))
    }

    @Test
    fun aWeeklyIssuerIsJudgedByItsOwnRhythm() {
        assertFalse(quiet(rate = "2026-11-10", cadence = PublishCadence.WEEKLY))
        assertTrue(quiet(rate = "2026-11-05", cadence = PublishCadence.WEEKLY))
    }

    @Test
    fun aMonthlyIssuerIsGivenAMonthAndAWeek() {
        assertFalse(quiet(rate = "2026-10-20", cadence = PublishCadence.MONTHLY))
        assertTrue(quiet(rate = "2026-10-01", cadence = PublishCadence.MONTHLY))
    }

    @Test
    fun aQuarterlyIssuerIsGivenAQuarterAndSome() {
        assertFalse(quiet(rate = "2026-09-01", cadence = PublishCadence.QUARTERLY))
        assertTrue(quiet(rate = "2026-07-01", cadence = PublishCadence.QUARTERLY))
    }

    @Test
    fun anUnknownCadenceIsTreatedAsDaily() {
        assertFalse(quiet(rate = "2026-11-14", cadence = PublishCadence.UNKNOWN))
        assertTrue(quiet(rate = "2026-11-12", cadence = PublishCadence.UNKNOWN))
    }

    @Test
    fun aPairThatNeverGotARateIsJudgedFromWhenItWasAdded() {
        assertFalse(
            quiet(rate = null, addedAt = "2026-11-15", cadence = PublishCadence.DAILY)
        )
        assertTrue(
            quiet(rate = null, addedAt = "2026-11-01", cadence = PublishCadence.DAILY)
        )
    }

    @Test
    fun aPairAddedTodayIsNeverQuietWhateverTheCadence() {
        PublishCadence.entries.forEach { cadence ->
            assertFalse(quiet(rate = null, addedAt = "2026-11-17", cadence = cadence))
        }
    }

    private fun quiet(
        rate: String?,
        cadence: PublishCadence,
        addedAt: String = "2026-01-01",
    ): Boolean = hasGoneQuiet(
        latestRate = rate?.let(LocalDate::parse),
        addedAt = LocalDate.parse(addedAt),
        cadence = cadence,
        today = TODAY,
    )

    private companion object {
        val TODAY = LocalDate.parse("2026-11-17")
    }
}
