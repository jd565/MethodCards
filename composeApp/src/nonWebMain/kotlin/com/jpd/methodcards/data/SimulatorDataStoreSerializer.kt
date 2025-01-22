package com.jpd.methodcards.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.okio.OkioSerializer
import com.jpd.methodcards.domain.PersistedSimulatorState
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import okio.BufferedSink
import okio.BufferedSource

@OptIn(ExperimentalSerializationApi::class)
class SimulatorDataStoreSerializer : OkioSerializer<PersistedSimulatorState> {
    override val defaultValue: PersistedSimulatorState
        get() = PersistedSimulatorState()

    override suspend fun readFrom(source: BufferedSource): PersistedSimulatorState {
        return try {
            ProtoBuf.decodeFromByteArray(source.readByteArray())
        } catch (e: Exception) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: PersistedSimulatorState, sink: BufferedSink) {
        sink.write(ProtoBuf.encodeToByteArray(t))
    }
}
