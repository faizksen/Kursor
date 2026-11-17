package dev.faizksen.kursor.sync

import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.workDataOf
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.SyncTrigger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PairSyncRequestTest {

    @Test
    fun aPairSyncCarriesThePairItWasAskedFor() {
        val request = pairSyncRequest(EUR_PLN)

        assertEquals("EUR", request.workSpec.input.getString(FiatRatesSyncWorker.KEY_BASE))
        assertEquals("PLN", request.workSpec.input.getString(FiatRatesSyncWorker.KEY_QUOTE))
    }

    @Test
    fun aPairSyncIsRecordedUnderItsOwnTrigger() {
        val request = pairSyncRequest(EUR_PLN)

        assertEquals(SyncTrigger.PairAdded, request.workSpec.input.syncTrigger())
    }

    @Test
    fun aPairSyncWaitsForAnyNetworkIncludingRoaming() {
        val request = pairSyncRequest(EUR_PLN)

        assertEquals(NetworkType.CONNECTED, request.workSpec.constraints.requiredNetworkType)
    }

    @Test
    fun eachPairQueuesUnderItsOwnName() {
        assertEquals("fiat_rates_pair_sync_EUR_PLN", EUR_PLN.uniqueWorkName())
        assertEquals(
            "fiat_rates_pair_sync_EUR_USD",
            CurrencyPair(CurrencyCode("EUR"), CurrencyCode("USD")).uniqueWorkName(),
        )
    }

    @Test
    fun theWorkerReadsThePairBackFromItsInput() {
        val input = workDataOf(
            FiatRatesSyncWorker.KEY_BASE to "EUR",
            FiatRatesSyncWorker.KEY_QUOTE to "PLN",
        )

        assertEquals(EUR_PLN, input.syncPair())
    }

    @Test
    fun aSyncWithoutAPairRefreshesEverything() {
        assertNull(Data.EMPTY.syncPair())
        assertNull(immediateSyncRequest().workSpec.input.syncPair())
        assertNull(periodicSyncRequest().workSpec.input.syncPair())
    }

    @Test
    fun aHalfFilledPairIsIgnoredRatherThanGuessed() {
        assertNull(workDataOf(FiatRatesSyncWorker.KEY_BASE to "EUR").syncPair())
        assertNull(workDataOf(FiatRatesSyncWorker.KEY_QUOTE to "PLN").syncPair())
        assertNull(
            workDataOf(
                FiatRatesSyncWorker.KEY_BASE to "",
                FiatRatesSyncWorker.KEY_QUOTE to "PLN",
            ).syncPair()
        )
    }

    private companion object {
        val EUR_PLN = CurrencyPair(base = CurrencyCode("EUR"), quote = CurrencyCode("PLN"))
    }
}
