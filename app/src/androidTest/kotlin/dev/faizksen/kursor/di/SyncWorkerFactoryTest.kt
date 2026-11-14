package dev.faizksen.kursor.di

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.testing.TestListenableWorkerBuilder
import dev.faizksen.kursor.sync.FiatRatesSyncWorker
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.workmanager.factory.KoinWorkerFactory

@RunWith(AndroidJUnit4::class)
class SyncWorkerFactoryTest {

    @Test
    fun koinBuildsTheSyncWorker() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val worker = TestListenableWorkerBuilder<FiatRatesSyncWorker>(context)
            .setWorkerFactory(KoinWorkerFactory())
            .build()

        assertNotNull(worker)
    }
}
