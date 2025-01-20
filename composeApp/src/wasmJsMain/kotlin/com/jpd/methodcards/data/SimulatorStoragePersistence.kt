package com.jpd.methodcards.data

import com.jpd.methodcards.domain.PersistedSimulatorState
import com.jpd.methodcards.domain.PersistedSimulatorStates
import kotlinx.browser.localStorage
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.protobuf.ProtoBuf
import org.w3c.dom.Storage
import org.w3c.dom.get
import org.w3c.dom.set

@OptIn(ExperimentalSerializationApi::class)
class SimulatorStoragePersistence(
    private val storage: Storage = localStorage,
) : SimulatorPersistence {
    private var states: PersistedSimulatorStates? = storage[SIMULATOR_STORAGE_KEY]?.let {
        try {
            ProtoBuf.decodeFromHexString(it)
        } catch (e: Exception) {
            null
        }
    }
    override suspend fun getSimulatorModel(stage: Int): PersistedSimulatorState? {
        return states?.states?.get(stage)
    }

    override suspend fun persistSimulatorModel(model: PersistedSimulatorState, stage: Int) {
        this.states = PersistedSimulatorStates(
            states = states?.states.orEmpty().plus(stage to model)
        )
        storage[SIMULATOR_STORAGE_KEY] = ProtoBuf.encodeToHexString(states)
    }

    companion object {
        private const val SIMULATOR_STORAGE_KEY = "simulatorStorage"
    }
}
