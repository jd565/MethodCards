package com.jpd.methodcards.data

import com.jpd.methodcards.di.MethodCardDi.getMethodDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber
import methodcards.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.time.measureTime

const val MethodLibraryVersion = 3

class MethodLibrary(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val dao: MethodDao = getMethodDao(),
) {
    @OptIn(ExperimentalResourceApi::class, ExperimentalSerializationApi::class)
    suspend fun getMethods() = withContext(dispatcher) {
        val methods: List<MethodProto>
        val t = measureTime {
            println("Generating methods proto")
            val path = "files/methods.pb"
            val pb = Res.readBytes(path)
            methods = ProtoBuf.decodeFromByteArray<MethodsProto>(pb).methods
        }
        val insertT = measureTime {
            dao.insert(methods)
        }
        println("Generated methods proto (${t.inWholeMilliseconds}), (${insertT.inWholeMilliseconds})")
    }
}

@Serializable
data class MethodsProto(
    @SerialName("methods") @ProtoNumber(1) val methods: List<MethodProto> = emptyList()
)

@Serializable
data class MethodProto(
    @SerialName("name") @ProtoNumber(1) val name: String,
    @SerialName("notation") @ProtoNumber(2) val notation: String,
    @SerialName("stage") @ProtoNumber(3) val stage: Int,
    @SerialName("ruleoffsFrom") @ProtoNumber(4) val ruleoffsFrom: Int,
    @SerialName("ruleoffsEvery") @ProtoNumber(5) val ruleoffsEvery: Int,
    @SerialName("calls") @ProtoNumber(6) val calls: Map<String, CallProto> = emptyMap(),
    @SerialName("magic") @ProtoNumber(7) val magic: Int,
    @SerialName("classification") @ProtoNumber(8) val classification: String,
) {
    @Serializable
    data class CallProto(
        @SerialName("symbol") @ProtoNumber(1) val symbol: String,
        @SerialName("notation") @ProtoNumber(2) val notation: String,
        @SerialName("from_") @ProtoNumber(3) val from: Int,
        @SerialName("every") @ProtoNumber(4) val every: Int,
        @SerialName("cover") @ProtoNumber(5) val cover: Int,
    )
}
