package com.jpd.methodcards.domain

import kotlin.jvm.JvmInline

@JvmInline
value class Row(
    val row: IntArray,
) {
    constructor(stage: Int, init: (Int) -> Int) : this(IntArray(stage) { init(it) })
    fun nextRow(notation: String): Row {
        val r = if (notation == "-") {
            // full swap
            IntArray(row.size) { idx ->
                if (idx % 2 == 0) {
                    row[idx + 1]
                } else {
                    row[idx - 1]
                }
            }
        } else {
            val digits = notation.map { it.toBellDigit() }

            var highIdx = 0
            var high: Int = digits[highIdx]
            var finished = false

            IntArray(row.size) { idx ->
                if (finished) {
                    check(idx >= high)
                    if ((idx - high) % 2 == 0) {
                        row[idx + 1]
                    } else {
                        row[idx - 1]
                    }
                } else {
                    check(idx < high)
                    if (idx + 1 == high) {
                        highIdx++
                        finished = highIdx >= digits.size
                        high = digits.getOrNull(highIdx) ?: digits.last()
                        row[idx]
                    } else {
                        if ((high - idx) % 2 == 0) {
                            row[idx - 1]
                        } else {
                            row[idx + 1]
                        }
                    }
                }
            }
        }
        return Row(r)
    }

    operator fun get(idx: Int): Int = row[idx]

    fun map(transform: (Int) -> Int): Row {
        val array = IntArray(row.size) {
            transform(row[it])
        }
        return Row(array)
    }

    fun mapIndexed(transform: (Int, Int) -> Int): Row {
        val array = IntArray(row.size) {
            transform(it, row[it])
        }
        return Row(array)
    }

    fun indexOf(bell: Int): Int = row.indexOf(bell)

    fun isRounds(): Boolean {
        row.forEachIndexed { index, i ->
            if (i != index + 1) {
                return false
            }
        }
        return true
    }

    companion object {
        fun rounds(stage: Int): Row = Row(IntArray(stage) { it + 1 })
    }
}

private fun Char.toBellDigit(): Int = when (this) {
    '1' -> 1
    '2' -> 2
    '3' -> 3
    '4' -> 4
    '5' -> 5
    '6' -> 6
    '7' -> 7
    '8' -> 8
    '9' -> 9
    '0' -> 10
    'e', 'E' -> 11
    't', 'T' -> 12
    'a', 'A' -> 13
    'b', 'B' -> 14
    'c', 'C' -> 15
    'd', 'D' -> 16
    else -> error("Invalid bell notation: $this")
}

@JvmInline
value class Lead(
    val lead: List<Row>,
)

