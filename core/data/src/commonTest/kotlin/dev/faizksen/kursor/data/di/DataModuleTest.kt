package dev.faizksen.kursor.data.di

import org.koin.core.Koin
import org.koin.dsl.koinApplication
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.time.Clock

class DataModuleTest {

    private val koin: Koin = koinApplication { modules(dataModule) }.koin

    @AfterTest
    fun tearDown() {
        koin.close()
    }

    @Test
    fun `the clock the graph hands out follows real time`() {
        assertSame(Clock.System, koin.get<Clock>())
    }
}
