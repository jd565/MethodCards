package com.jpd.methodcards.data

import androidx.datastore.core.DataStore
import com.jpd.methodcards.domain.PersistedSimulatorState
import kotlinx.coroutines.flow.firstOrNull

class SimulatorDataStorePersistence(
    private val store: DataStore<PersistedSimulatorState>
) : SimulatorPersistence{
    override suspend fun getSimulatorModel(stage: Int): PersistedSimulatorState? {
        return store.data.firstOrNull()
    }

    override suspend fun persistSimulatorModel(model: PersistedSimulatorState) {
        store.updateData { model }
    }
}
