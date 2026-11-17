package dev.faizksen.kursor.watchlist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Instant

class PartialUpdateTest {

    @Test
    fun `a run that missed some pairs says how many it got`() {
        assertEquals("3/4", success(updated = 3, requested = 4).partialCount())
    }

    @Test
    fun `a run that got everything says nothing`() {
        assertNull(success(updated = 4, requested = 4).partialCount())
    }

    @Test
    fun `a run that got nothing at all still says so`() {
        assertEquals("0/4", success(updated = 0, requested = 4).partialCount())
    }

    @Test
    fun `a run from before the counts were recorded says nothing`() {
        assertNull(success(updated = null, requested = null).partialCount())
        assertNull(success(updated = 3, requested = null).partialCount())
        assertNull(success(updated = null, requested = 4).partialCount())
    }

    @Test
    fun `a count above what was asked is not reported`() {
        assertNull(success(updated = 5, requested = 4).partialCount())
    }

    @Test
    fun `an empty watchlist reports nothing`() {
        assertNull(success(updated = 0, requested = 0).partialCount())
    }

    private fun success(updated: Int?, requested: Int?) = LastUpdate.Success(
        updatedAt = Instant.parse("2026-11-17T10:00:00Z"),
        updatedCount = updated,
        requestedCount = requested,
    )
}
