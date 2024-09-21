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
data class MethodsProto(val methods: List<MethodProto> = emptyList())

@Serializable
data class MethodProto(
    val name: String,
    val notation: String,
    val stage: Int,
    val ruleoffsFrom: Int,
    val ruleoffsEvery: Int,
    val calls: Map<String, CallProto> = emptyMap(),
    val magic: Int,
    val classification: String,
) {
    @Serializable
    data class CallProto(
        val symbol: String,
        val notation: String,
        @SerialName("from_") val from: Int,
        val every: Int,
        val cover: Int,
    )
}
