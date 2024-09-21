package com.jpd.methodcards.data

import androidx.datastore.core.okio.OkioSerializer
import com.jpd.methodcards.domain.PersistedSimulatorStates
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import okio.BufferedSink
import okio.BufferedSource

@OptIn(ExperimentalSerializationApi::class)
class SimulatorDataStoreSerializer : OkioSerializer<PersistedSimulatorStates> {
    override val defaultValue: PersistedSimulatorStates
        get() = PersistedSimulatorStates()

    override suspend fun readFrom(source: BufferedSource): PersistedSimulatorStates {
        return ProtoBuf.decodeFromByteArray(source.readByteArray())
    }

    override suspend fun writeTo(t: PersistedSimulatorStates, sink: BufferedSink) {
        sink.write(ProtoBuf.encodeToByteArray(t))
    }
}
