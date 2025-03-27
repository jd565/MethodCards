package com.jd565.methods.generate

import com.jpd.MethodProto
import com.jpd.MethodsProto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.File

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    val service = CccbrService()
    runBlocking(Dispatchers.IO) {
        val methodsD = async { service.getAllMethods() }
        val performances = async {
            service.getBellboardPerformances(pages = 40)
        }.await()

        val methods = methodsD.await()
        println("Fetched methods and performances")

        val methodsByStage = (1..30).associateWith { mutableListOf<Pair<XmlMethod, XmlMethodSetProperties>>() }
        methods.sets.forEach { set ->
            set.methods.forEach { method ->
                (method.stage?.value ?: set.properties.stage?.value)?.toIntOrNull()?.let { stage ->
                    methodsByStage[stage]!!.add(method to set.properties)
                }
            }
        }

        val methodNameSet = methods.sets.flatMapTo(mutableSetOf()) { it.methods.mapNotNull { m -> m.title?.value } }
        println("Created methods structures")

        val popularMethods = BellBoardPerformanceParser.parse(performances, methodNameSet)
        println("Parsed performances")
        var count = 0
        val protoMethods = mutableListOf<MethodProto>()
        methodsByStage.forEach { (stage, list) ->
            println("Most popular methods for stage $stage: ${popularMethods[stage]?.take(20)?.joinToString()}")
            val top = (topMethods[stage] ?: emptyList())
            val pop = (popularMethods[stage] ?: emptyList())
            val topPop = top + pop.filter { it !in top }
            val others = list.mapNotNull { (it, _) ->
                it.title?.value?.let { title ->
                    if (title in topPop) {
                        null
                    } else {
                        title
                    }
                }
            }.sorted()
            val new = list.convertToProto(count, topPop + others)
            count += new.size
            protoMethods.addAll(new.sortedBy { it.magic })
        }
        val outFile = File("composeApp/src/commonMain/composeResources/files/methods.pb")
        ProtoBuf.encodeToByteArray(MethodsProto(protoMethods)).let { outFile.writeBytes(it) }
    }
}
