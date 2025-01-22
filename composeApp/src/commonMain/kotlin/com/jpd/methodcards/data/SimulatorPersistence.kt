package com.jpd.methodcards.data

import com.jpd.methodcards.domain.PersistedSimulatorState

interface SimulatorPersistence {
    suspend fun getSimulatorModel(stage: Int): PersistedSimulatorState?
    suspend fun persistSimulatorModel(model: PersistedSimulatorState)
}
