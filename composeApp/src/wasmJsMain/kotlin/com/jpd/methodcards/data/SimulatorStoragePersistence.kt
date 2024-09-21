package com.jpd.methodcards.data

import com.jpd.methodcards.domain.PersistedSimulatorState
import com.jpd.methodcards.domain.PersistedSimulatorStates
import org.w3c.dom.Storage

class SimulatorStoragePersistence(
    private val storage: Storage,
) : SimulatorPersistence {
    private var states: PersistedSimulatorStates? = null
    override suspend fun getSimulatorModel(stage: Int): PersistedSimulatorState? {
        return states?.states?.get(stage)
    }

    override suspend fun persistSimulatorModel(model: PersistedSimulatorState, stage: Int) {
        this.states = PersistedSimulatorStates(
            states = states?.states.orEmpty().plus(stage to model)
        )
    }
}
