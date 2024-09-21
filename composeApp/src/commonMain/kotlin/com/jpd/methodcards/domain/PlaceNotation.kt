package com.jpd.methodcards.domain

import kotlin.jvm.JvmInline

@JvmInline
value class PlaceNotation(
    val pn: String,
) {
    val fullNotation: FullNotation
        get() {
            val sequences =
                pn
                    .split(",")
                    .map { notationSequence ->
                        notationSequence
                            .split(".")
                            .flatMap { subsequence ->
                                subsequence
                                    .split("-", "x")
                                    .flatMap { listOfNotNull(it.ifEmpty { null }, "-") }
                                    .dropLast(1)
                            }
                    }

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

    companion object {
        fun placeNotationCharacters(stage: Int) = Row.rounds(stage).row.joinToString(
            separator = "",
            prefix = "[,.-x",
            postfix = "]",
        ) { it.toBellChar() }
    }
}

@JvmInline
value class FullNotation(val notation: List<String>) {
    fun sequence(initialRow: Row): Sequence<Row> {
        return sequence {
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
