package com.jpd.methodcards.domain

import kotlin.jvm.JvmInline

@JvmInline
value class PlaceNotation(
    val sequences: List<List<String>>,
) {
    constructor(pn: String) : this(notationSequences(pn))
    val fullNotation: FullNotation
        get() {
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
            )
        }

    fun sequence(initialRow: Row): Sequence<Row> {
        return fullNotation.sequence(initialRow)
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

@JvmInline
value class FullNotation(val notation: List<String>) {
    fun sequence(initialRow: Row): Sequence<Row> {
        return kotlin.sequences.sequence {
            yield(initialRow)
            var row = initialRow
            notation.forEach { notation ->
                row = row.nextRow(notation)
                yield(row)
            }
        }
    }

    fun withCall(idx: Int, callNotation: PlaceNotation): FullNotation {
        val new = notation.toMutableList()
        callNotation.fullNotation.notation.forEachIndexed { index, s ->
            new[idx + index] = s
        }
        return FullNotation(new)
    }
}
