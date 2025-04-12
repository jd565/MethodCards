package com.jpd.methodcards.domain

import kotlin.jvm.JvmInline

@JvmInline
value class PlaceNotation(
    val sequences: List<List<String>>,
) {
    constructor(pn: String) : this(notationSequences(pn))

    fun fullNotation(stage: Int): FullNotation {
        return FullNotation(
            if (sequences.size == 1) {
                sequences[0]
            } else {
                sequences.flatMap { subsequence ->
                    subsequence.plus(
                        subsequence.reversed().drop(1),
                    )
                }
            },
            stage,
        )
    }

    fun merge(over: PlaceNotation): PlaceNotation? {
        val underSequences = sequences
        val overSequences = over.sequences
        if (underSequences.size != overSequences.size) return null

        val combined = underSequences.zip(overSequences) { uSeq, oSeq ->
            if (uSeq.size != oSeq.size) return null
            uSeq.zip(oSeq) { u, o ->
                when {
                    u == "x" && o == "x" -> u
                    u == "x" -> o
                    o == "x" -> u
                    else -> u + o
                }
            }
        }

        return PlaceNotation(combined)
    }

    fun asString(): String {
        return sequences.joinToString(",") { subsequence ->
            buildString {
                subsequence.zipWithNext().forEach {
                    append(it.first)
                    if (!(it.first == "x").xor(it.second == "x")) {
                        append(".")
                    }
                }
                append(subsequence.last())
            }
        }
    }

    companion object {
        fun placeNotationCharacters(stage: Int) = Row.rounds(stage).row.joinToString(
            separator = "",
            prefix = "[-,.x",
            postfix = "]",
        ) { it.toBellChar() }

        fun notationSequences(pn: String): List<List<String>> {
            return pn
                .split(",")
                .map { notationSequence ->
                    notationSequence
                        .split(".")
                        .flatMap { subsequence ->
                            subsequence
                                .split("-", "x")
                                .flatMap { listOfNotNull(it.ifEmpty { null }, "x") }
                                .dropLast(1)
                        }
                }
        }
    }
}

data class FullNotation(
    val notation: List<String>,
    val stage: Int,
) {
    fun sequence(initialRow: Row): Sequence<Row> {
        return sequence {
            yield(initialRow)
            var row = initialRow
            notation.forEach { notation ->
                row = row.nextRow(notation, stage)
                yield(row)
            }
        }
    }

    fun withCall(idx: Int, callNotation: PlaceNotation): FullNotation {
        val new = notation.toMutableList()
        callNotation.fullNotation(stage).notation.forEachIndexed { index, s ->
            new[idx + index] = s
        }
        return FullNotation(new, stage)
    }

    val leads: List<Lead> by lazy {
        generateLeads(stage)
    }
    val leadEnd: Row by lazy {
        leads.first().lead.last()
    }
    val changesInLead: Int by lazy {
        leads.first().lead.size - 1
    }
    val leadEndNotation: String by lazy {
        notation.last()
    }
    val huntBells: List<Int> by lazy {
        buildList {
            leadEnd.row.forEachIndexed { idx, bell ->
                if (bell == idx + 1) {
                    add(idx + 1)
                }
            }
        }
    }
    val leadCycles: List<List<Int>> by lazy {
        val handled = mutableSetOf<Int>()
        val hunts = huntBells
        val cycles = mutableListOf<List<Int>>()
        handled.addAll(hunts)
        val transpose = leadEnd.row
        1.rangeTo(stage).forEach { bell ->
            if (bell !in handled) {
                var b = bell
                val cycle = mutableListOf<Int>()
                do {
                    cycle.add(b)
                    b = transpose.indexOf(b) + 1
                    require(b > 0)
                    require(b !in handled)
                } while (b != bell)
                handled.addAll(cycle)
                cycles.add(cycle)
            }
        }
        cycles
    }

    val classification: MethodClassDescriptor by lazy {
        classifyMethod()
    }
}

private fun FullNotation.generateLeads(stage: Int): List<Lead> {
    val full = this

    var row = Row.rounds(stage)
    val leads = mutableListOf<Lead>()

    do {
        val lead = full.sequence(row).toList()
        leads.add(Lead(lead))
        row = lead.last()
    } while (!row.isRounds())

    return leads
}

private fun FullNotation.classifyMethod(): MethodClassDescriptor {
    val differential = leadCycles.mapTo(mutableSetOf()) { it.size }.size > 1
    if (huntBells.isEmpty()) {
        return MethodClassDescriptor(MethodClassification.None, differential)
    }
    val descriptors = huntBells.map { classifyMethodForHunt(it) }
    if (descriptors.size == 1) {
        return descriptors.first()
    } else {
        listOf(
            listOf(MethodClassification.Place, MethodClassification.Bob),
            listOf(
                MethodClassification.TrebleBob,
                MethodClassification.Surprise,
                MethodClassification.Delight,
            ),
            listOf(MethodClassification.TreblePlace),
            listOf(MethodClassification.Alliance),
            listOf(MethodClassification.Hybrid),
        ).forEachIndexed { index, classifications ->
            val filtered = descriptors.filter { it.classification in classifications }
            if (filtered.isNotEmpty()) {
                val allLittle = filtered.all { it.little }
                val cls = if (index == 1) {
                    filtered.first().classification
                } else {
                    val tdClassifications = filtered.mapNotNullTo(mutableSetOf()) {
                        when {
                            allLittle || !it.little -> it.classification
                            else -> null
                        }
                    }
                    if (tdClassifications.size == 1) {
                        tdClassifications.first()
                    } else {
                        MethodClassification.Delight
                    }
                }
                return MethodClassDescriptor(
                    cls,
                    filtered.first().differential,
                    allLittle,
                )
            }
        }
        return MethodClassDescriptor(MethodClassification.None, differential)
    }
}

private fun FullNotation.classifyMethodForHunt(huntBell: Int): MethodClassDescriptor {
    val differential = leadCycles.mapTo(mutableSetOf()) { it.size }.size > 1
    val lead = leads.first().lead.dropLast(1)
    val huntBellPositions = lead.map { it.indexOf(huntBell) + 1 }
    val pathPositionCounts = buildMap {
        huntBellPositions.forEach { position ->
            this[position] = (this[position] ?: 0) + 1
        }
    }

    val little = pathPositionCounts.size < stage

    val huntBellIsNotStationary = pathPositionCounts.size > 1
    val methodIsNotJump = true

    // Check for plain method
    // A Hunter in which:
    // a) The Hunt Bell rings exactly twice in each Place of the Path during a Plain Lead;
    // b) The Hunt Bell is not a Stationary Bell; and
    // c) The Method does not use Jump Changes.
    val isPlainA = pathPositionCounts.all { it.value == 2 }
    if (isPlainA && huntBellIsNotStationary && methodIsNotJump) {
        // A Plain Method in which the Paths of all the bells consist only of Hunting and Making Places,
        // and in which a change in the direction of Hunting is separated by Making one or more Places.
        val twoLeads = buildList {
            addAll(leads.first().lead.dropLast(1))
            addAll(leads[1].lead)
        }
        val isPlace = Row.rounds(stage).row.all { bell ->
            var currentIdx = -1
            var prevIdx = -1
            var sPrevIdx = -1
            twoLeads.all { row ->
                sPrevIdx = prevIdx
                prevIdx = currentIdx
                currentIdx = row.indexOf(bell)
                if (sPrevIdx >= 0) {
                    if (currentIdx == prevIdx || prevIdx == sPrevIdx) {
                        // There is a place being made so this sequence is ok
                        true
                    } else {
                        // There is no place - either a point or hunting
                        // If a point then the first and last places are the same
                        currentIdx != sPrevIdx
                    }
                } else {
                    true
                }
            }
        }
        return if (isPlace) {
            MethodClassDescriptor(MethodClassification.Place, differential, little)
        } else {
            MethodClassDescriptor(MethodClassification.Bob, differential, little)
        }
    }

    // Not a plain method
    // Check for Treble Dodging method
    // A Hunter in which:
    // a) The Hunt Bell rings more than twice in each Place of the Path during a Plain Lead;
    // b) The Hunt Bell rings the same number of times in each Place of the Path during a Plain Lead;
    // c) The Hunt Bell Makes a Place exactly twice during a Plain Lead;
    // d) The Path of the Hunt Bell is the same if it is rung backwards;
    // e) The Hunt Bell is not a Stationary Bell; and
    // f) The Method does not use Jump Changes.
    val isTdA = pathPositionCounts.all { it.value > 2 }
    val huntRingsSameNumberOfTimesInPlace = pathPositionCounts.values.toSet().size == 1
    val numberOfHuntBellPlaces = huntBellPositions.plus(huntBellPositions[0]).zipWithNext().count { it.first == it.second }
    val isTdC = numberOfHuntBellPlaces == 2
    val reversedPathsSequence = sequence {
        var reversed = huntBellPositions.asReversed().toMutableList()
        var offset = 0
        while (offset < huntBellPositions.size) {
            yield(reversed)
            val end = reversed.removeAt(0)
            reversed.add(end)
            offset++
        }
    }
    val pathIsSameBackwards = reversedPathsSequence.any { it == huntBellPositions }

    if (isTdA && huntRingsSameNumberOfTimesInPlace && isTdC && pathIsSameBackwards && huntBellIsNotStationary && methodIsNotJump) {
        val crossSectionIndexes = huntBellPositions.zipWithNext().mapIndexedNotNull { index, (a, b) ->
            // Either moving from e.g. 2 to 3 or 3 to 2
            if ((a % 2 == 0 && b == a + 1) || (a % 2 == 1 && b == a - 1)) {
                index
            } else {
                null
            }
        }
        val internalPlacesAtCrosses = crossSectionIndexes.map { idx ->
            notation[idx].isInternalPlace(stage)
        }
        val classification = when {
            internalPlacesAtCrosses.all { !it } -> MethodClassification.TrebleBob
            internalPlacesAtCrosses.all { it } -> MethodClassification.Surprise
            else -> MethodClassification.Delight
        }
        return MethodClassDescriptor(classification, differential, little)
    }

    // Not a plain method or Treble Dodging method
    // Check for Treble Place method
    // A Hunter in which:
    // a) The Hunt Bell rings the same number of times in each Place of the Path during a Plain Lead;
    // b) The Hunt Bell Makes a Place more than twice during a Plain Lead;
    // c) The Path of the Hunt Bell is the same if it is rung backwards; and
    // d) The Method does not use Jump Changes;
    // Or:
    // a) The Hunt Bell is a Stationary Bell; and
    // b) The Method does not use Jump Changes.
    val isTp1B = numberOfHuntBellPlaces > 2
    if ((huntRingsSameNumberOfTimesInPlace && isTp1B && pathIsSameBackwards && methodIsNotJump) || (!huntBellIsNotStationary && methodIsNotJump)) {
        return MethodClassDescriptor(MethodClassification.TreblePlace, differential, little)
    }

    // Check for Alliance
    // A Hunter in which:
    // a) The Hunt Bell does not ring the same number of times in each Place of the Path during a Plain Lead;
    // b) The Path of the Hunt Bell is the same if it is rung backwards;
    // c) The Hunt Bell is not a Stationary Bell; and
    // d) The Method does not use Jump Changes.
    if (!huntRingsSameNumberOfTimesInPlace && pathIsSameBackwards && huntBellIsNotStationary && methodIsNotJump) {
        return MethodClassDescriptor(MethodClassification.Alliance, differential, little)
    }

    // Check for Hybrid
    // A Hunter in which:
    // a) The Path of the Hunt Bell is not Plain, Treble Dodging, Treble Place or Alliance; and
    // b) The Method does not use Jump Changes.
    // Condition A would have been found above
    if (methodIsNotJump) {
        return MethodClassDescriptor(MethodClassification.Hybrid, differential, little)
    }

    return MethodClassDescriptor(MethodClassification.None, differential)
}

private fun String.isInternalPlace(stage: Int): Boolean {
    return this != "1${stage.toBellChar()}"
}
