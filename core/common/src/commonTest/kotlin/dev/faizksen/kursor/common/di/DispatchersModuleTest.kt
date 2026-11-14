package dev.faizksen.kursor.common.di

import kotlinx.coroutines.Dispatchers
import org.koin.core.Koin
import org.koin.dsl.koinApplication
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DispatchersModuleTest {

    private val koin: Koin = koinApplication { modules(dispatchersModule) }.koin

    @AfterTest
    fun tearDown() {
        koin.close()
    }

    @Test
    fun `the io dispatcher is the one the platform declares`() {
        assertEquals(IoDispatcher(ioDispatcher), koin.get<IoDispatcher>())
    }

    @Test
    fun `the default dispatcher is the shared pool`() {
        assertEquals(DefaultDispatcher(Dispatchers.Default), koin.get<DefaultDispatcher>())
    }

    @Test
    fun `the main dispatcher is the immediate one`() {
        assertEquals(MainDispatcher(Dispatchers.Main.immediate), koin.get<MainDispatcher>())
    }
}
