package com.jpd.methodcards.data

import com.jpd.methodcards.domain.PersistedSimulatorState

interface SimulatorPersistence {
    suspend fun getSimulatorModel(): PersistedSimulatorState?
    suspend fun persistSimulatorModel(model: PersistedSimulatorState)
}
