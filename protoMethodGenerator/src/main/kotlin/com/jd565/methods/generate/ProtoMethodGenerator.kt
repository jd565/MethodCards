package com.jd565.methods.generate

import com.jpd.MethodProto
import com.jpd.methodcards.domain.PlaceNotation
import com.jpd.methodcards.domain.getCalls
import com.jpd.methodcards.domain.stageName
import com.jpd.methodcards.domain.toBellChar

fun List<Pair<XmlMethod, XmlMethodSetProperties>>.convertToProto(
    magicOffset: Int,
    sortOrder: List<String>,
): List<MethodProto> {
    return this.mapNotNull { (method, properties) ->
        val notation = method.notation?.value ?: return@mapNotNull null
        val xmlClassification = method.classification ?: properties.classification ?: return@mapNotNull null
        val classification = xmlClassification.classification
        var isLittle = xmlClassification.little ?: false
        val title = method.title?.value ?: return@mapNotNull null
        val stage = (method.stage?.value ?: properties.stage?.value)?.toIntOrNull() ?: return@mapNotNull null
        if (stage < 3 || stage > 16) return@mapNotNull null
        var name = title
        var nameHasClassification = false
        name = name.removeSuffix(stage.stageName()).trim()
        if (classification != null && name.endsWith(classification)) {
            name = name.removeSuffix(classification).trim()
            nameHasClassification = true
        }
        if (isLittle && name.endsWith("Little")) {
            name = name.removeSuffix("Little").trim()
        } else {
            isLittle = false
        }

        val lengthOfLead =
            (method.lengthOfLead?.value ?: properties.lengthOfLead?.value)?.toIntOrNull() ?: return@mapNotNull null
        val protoClassification = MethodProto.MethodClassificationProto.entries.find { it.part == classification }
            ?: MethodProto.MethodClassificationProto.None

        val customData = customCalls[title]
        val callDetails = customData ?: method.getCalls(properties)
        val ruleoffsFrom = customData?.ruleoffsFrom?.takeIf { it != 0 } ?: 0
        val ruleoffsEvery = customData?.ruleoffsEvery?.takeIf { it != lengthOfLead } ?: 0
        val magic = method.magic(magicOffset, sortOrder)

        val proto = MethodProto(
            shortName = name,
            notation = notation,
            stage = stage,
            lengthOfLead = lengthOfLead,
            standardCalls = callDetails is CallDetails.Standard,
            bobAndSingleCalls = (callDetails as? CallDetails.BobAndSingle)?.let { cd ->
                MethodProto.BobAndSingleCalls(
                    cd.bob,
                    cd.single,
                    cd.every ?: 0,
                    cd.from ?: 0,
                )
            },
            customCalls = (callDetails as? CallDetails.Custom)?.calls ?: emptyList(),
            ruleoffsEveryCompressed = ruleoffsEvery,
            ruleoffsFrom = ruleoffsFrom,
            classification = protoClassification,
            littleClassification = isLittle,
            nameHasClassification = nameHasClassification,
            magic = magic,
        )

        assert(proto.name == title)

        proto
    }
}

sealed class CallDetails {
    data object Standard : CallDetails()
    data class BobAndSingle(
        val bob: String?,
        val single: String?,
        val every: Int? = null,
        val from: Int? = null,
    ) : CallDetails()

    data class Custom(val calls: List<MethodProto.CallProto>) : CallDetails()
}

private fun XmlMethod.getCalls(properties: XmlMethodSetProperties): CallDetails? {
    val stage = (stage?.value ?: properties.stage?.value)?.toIntOrNull() ?: return null
    val notation = notation?.value ?: return null
    val pn = PlaceNotation(notation).fullNotation(stage)

    val classification = (classification ?: properties.classification) ?: return null
    val isDifferential = classification.differential ?: false
    val leadHeadCode = leadHeadCode?.value ?: properties.leadHeadCode?.value
    val numberOfHunts = (numberOfHunts?.value ?: properties.numberOfHunts?.value)?.toIntOrNull() ?: return null

    val calls = pn.getCalls(isDifferential, leadHeadCode, numberOfHunts)

    return if (calls.size == 2 && calls[0].name == "Bob" && calls[1].name == "Single" &&
        calls[0].everyCompressed == calls[1].everyCompressed &&
        calls[0].from == calls[1].from) {
        val le = pn.notation.last()
        val n = stage.toBellChar()
        val nm1 = (stage - 1).toBellChar()
        val nm2 = (stage - 2).toBellChar()
        if (le == "12" && calls[0].notation == "14" && calls[1].notation == "1234") {
            CallDetails.Standard
        } else if (le == "1$n" && calls[0].notation == "1$nm2" && calls[1].notation == "1$nm2$nm1$n") {
            CallDetails.Standard
        } else {
            CallDetails.BobAndSingle(
                bob = calls[0].notation,
                single = calls[1].notation,
                every = calls[0].everyCompressed,
                from = calls[0].from,
            )
        }
    } else if (calls.size == 1 && calls[0].name == "Bob") {
        CallDetails.BobAndSingle(
            bob = calls[0].notation,
            single = null,
            every = calls[0].everyCompressed,
            from = calls[0].from,
        )
    } else if (calls.isNotEmpty()) {
        CallDetails.Custom(calls)
    } else {
        null
    }
}

private fun XmlMethod.magic(magicOffset: Int, sortOrder: List<String>): Int {
    val title = title?.value!!

    val idx = sortOrder.indexOf(title)
    assert(idx >= 0) { "No entry found for $title" }

    return magicOffset + idx
}
