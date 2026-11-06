package dev.faizksen.kursor

import android.content.Context
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.ktor.client.engine.HttpClientEngine
import dev.faizksen.kursor.di.appModule
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

class KoinGraphTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `every dependency the modules declare can be resolved`() {
        val provided = listOf(
            Context::class,
            WorkManager::class,
            WorkerParameters::class,
            HttpClientEngine::class,
        )

        appModule.verify(extraTypes = provided)
    }
}
