package com.jpd.methodcards.data

import androidx.datastore.core.DataStore
import com.jpd.methodcards.domain.PersistedSimulatorState
import com.jpd.methodcards.domain.PersistedSimulatorStates
import kotlinx.coroutines.flow.firstOrNull

class SimulatorDataStorePersistence(
    private val store: DataStore<PersistedSimulatorStates>
) : SimulatorPersistence{
    override suspend fun getSimulatorModel(stage: Int): PersistedSimulatorState? {
        return store.data.firstOrNull()?.states?.get(stage)
    }

    override suspend fun persistSimulatorModel(model: PersistedSimulatorState, stage: Int) {
        store.updateData { current ->
            PersistedSimulatorStates(states = current.states + (stage to model))
        }
    }
}
