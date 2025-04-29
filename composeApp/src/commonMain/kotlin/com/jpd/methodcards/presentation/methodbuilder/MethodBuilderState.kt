package com.jpd.methodcards.presentation.methodbuilder

data class MethodBuilderState(
    val stage: Int,
    val grid: List<Array<Int?>>,
    val placeNotation: List<String>,
    val selectedGrid: Pair<Int, Int>?,
    val selectedNotation: Int?,
)
