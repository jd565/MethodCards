package com.jpd.methodcards.di

import com.jpd.methodcards.data.MethodCardsPreferences
import com.jpd.methodcards.data.MethodDao
import com.jpd.methodcards.data.SimulatorPersistence

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object MethodCardDi {
    fun getMethodCardsPreferences(): MethodCardsPreferences
    fun getMethodDao(): MethodDao
    fun getSimulatorPersistence(): SimulatorPersistence
}
