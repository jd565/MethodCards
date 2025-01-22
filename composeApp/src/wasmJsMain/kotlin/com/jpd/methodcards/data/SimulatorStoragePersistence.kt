package com.jpd.methodcards.data

import com.jpd.methodcards.domain.PersistedSimulatorState
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
    private var state: PersistedSimulatorState? = storage[SIMULATOR_STORAGE_KEY]?.let {
        try {
            ProtoBuf.decodeFromHexString(it)
        } catch (e: Exception) {
            null
        }
    }
    override suspend fun getSimulatorModel(stage: Int): PersistedSimulatorState? {
        return state
    }

    override suspend fun persistSimulatorModel(model: PersistedSimulatorState) {
        this.state = model
        storage[SIMULATOR_STORAGE_KEY] = ProtoBuf.encodeToHexString(state)
    }

    companion object {
        private const val SIMULATOR_STORAGE_KEY = "simulatorStorage"
    }
}
