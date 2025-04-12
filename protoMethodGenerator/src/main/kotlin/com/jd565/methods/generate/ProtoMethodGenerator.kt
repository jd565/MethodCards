package com.jd565.methods.generate

import com.jpd.MethodProto
import com.jpd.methodcards.domain.PlaceNotation
import com.jpd.methodcards.domain.stageName
import com.jpd.methodcards.domain.toBellChar

fun List<Pair<XmlMethod, XmlMethodSetProperties>>.convertToProto(
    magicOffset: Int,
    sortOrder: List<String>,
): List<MethodProto> {
    return this.mapNotNull { (method, properties) ->
        val notation = method.notation?.value ?: return@mapNotNull null
        val xmlClassification = method.classification ?: properties.classification ?: return@mapNotNull null
        val classification = xmlClassification.classification ?: return@mapNotNull null
        var isLittle = xmlClassification.little ?: false
        val title = method.title?.value ?: return@mapNotNull null
        val stage = (method.stage?.value ?: properties.stage?.value)?.toIntOrNull() ?: return@mapNotNull null
        if (stage < 3 || stage > 16) return@mapNotNull null
        var name = title
        var nameHasClassification = false
        name = name.removeSuffix(stage.stageName()).trim()
        if (name.endsWith(classification)) {
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

    if (!isDifferential && stage > 4) {
        val le = pn.notation.last()
        val postLe = pn.notation.first()
        val leadHeadCode = leadHeadCode?.value ?: properties.leadHeadCode?.value
        val n = stage.toBellChar()
        val numberOfHunts = (numberOfHunts?.value ?: properties.numberOfHunts?.value)?.toIntOrNull() ?: return null
        when (numberOfHunts) {
            0 -> {
                if (stage % 2 == 0) {
                    if (le == "1$n") {
                        return CallDetails.Standard
                    }
                }
            }

            1 -> {
                if (stage % 2 == 0) {
                    if (le == "12") {
                        return CallDetails.Standard
                    } else if (le == "1$n") {
                        return if (leadHeadCode == "m" && stage > 6) {
                            CallDetails.BobAndSingle("14", "1234")
                        } else {
                            CallDetails.Standard
                        }
                    } else if (le == "14" && stage == 6) {
                        return CallDetails.BobAndSingle("16", "156")
                    }
                } else {
                    if (le == "12$n" || le == "1") {
                        return CallDetails.BobAndSingle("14$n", if (stage < 6) "123" else "1234$n")
                    } else if (le == "123") {
                        return CallDetails.BobAndSingle("12$n", null)
                    }
                }
            }

            2 -> {
                if (stage % 2 == 0) {
                    if (le == "1$n" && postLe == "3$n") {
                        return CallDetails.BobAndSingle("3$n.1$n", "3$n.123$n", from = -1)
                    }
                } else {
                    if (le == "1" && (postLe == "3" || postLe == n)) {
                        return CallDetails.BobAndSingle("3.1", "3.123", from = -1)
                    }
                }
            }
        }
    }
    return null
}

private fun XmlMethod.magic(magicOffset: Int, sortOrder: List<String>): Int {
    val title = title?.value!!

    val idx = sortOrder.indexOf(title)
    assert(idx >= 0) { "No entry found for $title" }

    return magicOffset + idx
}
