@file:OptIn(ExperimentalSerializationApi::class)

package com.jpd

import com.jpd.methodcards.domain.PlaceNotation
import com.jpd.methodcards.domain.stageName
import com.jpd.methodcards.domain.toBellChar
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kotlinx.serialization.protobuf.ProtoPacked

@Serializable
data class MethodsProto(
    @SerialName("methods") @ProtoPacked @ProtoNumber(1) val methods: List<MethodProto> = emptyList()
)

@Serializable
data class MethodProto(
    @SerialName("name") @ProtoNumber(1) private val shortName: String,
    @SerialName("notation") @ProtoNumber(2) val notation: String,
    @SerialName("stage") @ProtoNumber(3) val stage: Int,
    @SerialName("lengthOfLead") @ProtoNumber(4) val lengthOfLead: Int,
    @SerialName("ruleoffsFrom") @ProtoNumber(5) val ruleoffsFrom: Int = 0,
    // If set to 0 then will use lengthOfLead
    @SerialName("ruleoffsEvery") @ProtoNumber(6) val ruleoffsEveryCompressed: Int = 0,
    // If this is true then a 14 bob is used on a 12 method and a 16 bob is used on a 18 method etc.
    @SerialName("standardCalls") @ProtoNumber(7) private val standardCalls: Boolean = true,
    @SerialName("bobAndSingleCalls") @ProtoNumber(8) private val bobAndSingleCalls: BobAndSingleCalls? = null,
    @SerialName("customCalls") @ProtoPacked @ProtoNumber(9) private val customCalls: List<CallProto> = emptyList(),
    @SerialName("magic") @ProtoNumber(10) val magic: Int,
    @SerialName("classification") @ProtoNumber(11) val classification: MethodClassificationProto,
    @SerialName("littleClassification") @ProtoNumber(12) val littleClassification: Boolean = false,
    @SerialName("nameHasClassification") @ProtoNumber(13) val nameHasClassification: Boolean = true,
) {
    @Serializable
    data class CallProto(
        @SerialName("name") @ProtoNumber(1) val name: String,
        @SerialName("symbol") @ProtoNumber(2) val symbol: String,
        @SerialName("notation") @ProtoNumber(3) val notation: String,
        @SerialName("from_") @ProtoNumber(4) val from: Int = 0,
        // If 0 uses lengthOfLead
        @SerialName("every") @ProtoNumber(5) val everyCompressed: Int = 0,
    ) {
        fun every(lengthOfLead: Int): Int = if (everyCompressed == 0) lengthOfLead else everyCompressed
    }

    @Serializable
    data class BobAndSingleCalls(
        @SerialName("bobNotation") @ProtoNumber(1) val bobNotation: String?,
        @SerialName("singleNotation") @ProtoNumber(2) val singleNotation: String?,
        // If 0 uses lengthOfLead
        @SerialName("every") @ProtoNumber(3) val everyCompressed: Int = 0,
        @SerialName("from_") @ProtoNumber(4) val from: Int = 0,
    ) {
        fun every(lengthOfLead: Int): Int = if (everyCompressed == 0) lengthOfLead else everyCompressed
    }

    val calls: List<CallProto> by lazy {
        if (standardCalls) {
            val le = PlaceNotation(notation).sequences.last().first()
            val bob: String
            val single: String
            if (le == "12") {
                bob = "14"
                single = "1234"
            } else {
                val last = stage.toBellChar()
                val nm1 = (stage - 1).toBellChar()
                val nm2 = (stage - 2).toBellChar()
                bob = "1$nm2"
                single = "1$nm2$nm1$last"
            }
            listOf(
                CallProto("Bob", "-", bob, 0, lengthOfLead),
                CallProto("Single", "s", single, 0, lengthOfLead),
            )
        } else if (bobAndSingleCalls != null) {
            val every = bobAndSingleCalls.every(lengthOfLead)
            listOfNotNull(
                bobAndSingleCalls.bobNotation?.let { CallProto("Bob", "-", it, 0, every) },
                bobAndSingleCalls.singleNotation?.let { CallProto("Single", "s", it, 0, every) },
            )
        } else {
            customCalls
        }
    }

    val ruleoffsEvery: Int get() = if (ruleoffsEveryCompressed == 0) lengthOfLead else ruleoffsEveryCompressed

    val name by lazy {
        buildList {
            if (shortName.isNotEmpty()) add(shortName)
            if (littleClassification) add ("Little")
            if (classification != MethodClassificationProto.None && nameHasClassification) add(classification.part)
            add(stage.stageName())
        }.joinToString(" ")
    }

    @Serializable
    enum class MethodClassificationProto(val part: String) {
        None(""),
        TreblePlace("Treble Place"),
        Delight("Delight"),
        Bob("Bob"),
        Jump("Jump"),
        Alliance("Alliance"),
        Hybrid("Hybrid"),
        TrebleBob("Treble Bob"),
        Place("Place"),
        Surprise("Surprise");
    }
}
