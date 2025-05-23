package com.jpd.methodcards.di

import com.jpd.methodcards.data.MethodCardsPreferences
import com.jpd.methodcards.data.MethodCardsStoragePreferences
import com.jpd.methodcards.data.MethodDao
import com.jpd.methodcards.data.SimulatorPersistence
import com.jpd.methodcards.data.SimulatorStoragePersistence
import com.jpd.methodcards.data.StorageMethodDao
import com.jpd.methodcards.presentation.hearing.AudioPlayer
import com.jpd.methodcards.presentation.hearing.WasmAudioPlayer
import com.jpd.methodcards.presentation.listener.AudioRecorder

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object MethodCardDi {
    private val preferences by lazy {
        MethodCardsStoragePreferences()
    }

    actual fun getMethodCardsPreferences(): MethodCardsPreferences = preferences

    private val dao by lazy {
        StorageMethodDao()
    }

    actual fun getMethodDao(): MethodDao = dao

    private val simulatorPersistence by lazy {
        SimulatorStoragePersistence()
    }

    actual fun getSimulatorPersistence(): SimulatorPersistence {
        return simulatorPersistence
    }
    actual fun getAudioRecorder(): AudioRecorder = TODO()
    actual fun getAudioPlayer(): AudioPlayer = WasmAudioPlayer()
}
