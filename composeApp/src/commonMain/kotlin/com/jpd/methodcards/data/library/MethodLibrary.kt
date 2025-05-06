package com.jpd.methodcards.data.library

import com.jpd.MethodProto
import com.jpd.MethodsProto
import com.jpd.methodcards.data.MethodDao
import com.jpd.methodcards.di.MethodCardDi.getMethodDao
import com.jpd.methodcards.domain.MethodClassification
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import methodcards.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.time.measureTime

const val MethodLibraryVersion = 10

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

fun MethodProto.MethodClassificationProto.toDomain(): MethodClassification = when (this) {
    MethodProto.MethodClassificationProto.None -> MethodClassification.None
    MethodProto.MethodClassificationProto.TreblePlace -> MethodClassification.TreblePlace
    MethodProto.MethodClassificationProto.Delight -> MethodClassification.Delight
    MethodProto.MethodClassificationProto.Bob -> MethodClassification.Bob
    MethodProto.MethodClassificationProto.Jump -> MethodClassification.Jump
    MethodProto.MethodClassificationProto.Alliance -> MethodClassification.Alliance
    MethodProto.MethodClassificationProto.Hybrid -> MethodClassification.Hybrid
    MethodProto.MethodClassificationProto.TrebleBob -> MethodClassification.TrebleBob
    MethodProto.MethodClassificationProto.Place -> MethodClassification.Place
    MethodProto.MethodClassificationProto.Surprise -> MethodClassification.Surprise
}
