package com.jpd.methodcards.domain

data class CallDetails(
    val methodName: String,
    val name: String,
    val symbol: String,
    val notation: PlaceNotation,
    val from: Int,
    val every: Int,
) {
    val cover: Int by lazy {
        notation.fullNotation.notation.size
    }
}
