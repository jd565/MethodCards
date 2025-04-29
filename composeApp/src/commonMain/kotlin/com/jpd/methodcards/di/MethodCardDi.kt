package com.jpd.methodcards.di

import com.jpd.methodcards.data.MethodCardsPreferences
import com.jpd.methodcards.data.MethodDao
import com.jpd.methodcards.data.SimulatorPersistence
import com.jpd.methodcards.presentation.hearing.AudioPlayer
import com.jpd.methodcards.presentation.listener.AudioRecorder

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object MethodCardDi {
    fun getMethodCardsPreferences(): MethodCardsPreferences
    fun getMethodDao(): MethodDao
    fun getSimulatorPersistence(): SimulatorPersistence
    fun getAudioRecorder(): AudioRecorder
    fun getAudioPlayer(): AudioPlayer
}
