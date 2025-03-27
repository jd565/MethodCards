package com.jpd.methodcards.domain

val bellChars = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "E", "T", "A", "B", "C", "D")
fun Number.toBellChar(): String = bellChars[toInt() - 1]

fun Number.stageName(): String = when (toInt()) {
    3 -> "Singles"
    4 -> "Minimus"
    5 -> "Doubles"
    6 -> "Minor"
    7 -> "Triples"
    8 -> "Major"
    9 -> "Caters"
    10 -> "Royal"
    11 -> "Cinques"
    12 -> "Maximus"
    13 -> "Sextuples"
    14 -> "Fourteen"
    15 -> "Septuples"
    16 -> "Sixteen"
    else -> error("Unknown stage $this")
}

fun String.stageNameToInt(): Int = when (this) {
    "Two" -> 2
    "Singles" -> 3
    "Minimus" -> 4
    "Doubles" -> 5
    "Minor" -> 6
    "Triples" -> 7
    "Major" -> 8
    "Caters" -> 9
    "Royal" -> 10
    "Cinques" -> 11
    "Maximus" -> 12
    "Sextuples" -> 13
    "Fourteen" -> 14
    "Septuples" -> 15
    "Sixteen" -> 16
    "Octuples" -> 17
    "Eighteen" -> 18
    "Twenty" -> 20
    "Twenty-Two" -> 22
    else -> error("Unknown stage $this")
}
