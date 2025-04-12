package com.jpd.methodcards.domain

enum class MethodClassification(val part: String) {
    None(""),
    TreblePlace("Treble Place"),
    Delight("Delight"),
    Bob("Bob"),
    Jump("Jump"),
    Alliance("Alliance"),
    Hybrid("Hybrid"),
    TrebleBob("Treble Bob"),
    Place("Place"),
    Surprise("Surprise")
}

data class MethodClassDescriptor(
    val classification: MethodClassification,
    val differential: Boolean = false,
    val little: Boolean = false,
) {
    val part: String = buildList {
        if (differential) {
            add("Differential")
        }
        if (little) {
            add("Little")
        }
        if (classification != MethodClassification.None) {
            add(classification.part)
        }
    }.joinToString(" ")
}
