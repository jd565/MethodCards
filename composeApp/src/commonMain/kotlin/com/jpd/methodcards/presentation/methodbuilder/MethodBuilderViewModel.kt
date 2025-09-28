package com.jpd.methodcards.presentation.methodbuilder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.jpd.methodcards.data.MethodRepository
import com.jpd.methodcards.domain.MethodWithCalls
import com.jpd.methodcards.domain.PlaceNotation
import com.jpd.methodcards.domain.toBellChar
import com.jpd.methodcards.domain.toBellDigit
import com.jpd.methodcards.presentation.methodbuilder.MethodBuilderViewModel.MethodBuilderEvent.GridCellSelected
import com.jpd.methodcards.presentation.methodbuilder.MethodBuilderViewModel.MethodBuilderEvent.NotationCellSelected
import com.jpd.methodcards.presentation.methodbuilder.MethodBuilderViewModel.MethodBuilderEvent.StageSelected
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.reflect.KClass

class MethodBuilderViewModel(
    private val methodRepository: MethodRepository = MethodRepository()
) : ViewModel() {
    private val stage = MutableStateFlow(8)
    private val selectedGrid = MutableStateFlow<Pair<Int, Int>>(Pair(0, 0))
    private val grid = MutableStateFlow<List<Array<Int?>>>(
        List(33) { idx ->
            if (idx == 0) {
                Array(stage.value) { it + 1 }
            } else {
                Array(stage.value) { null }
            }
        },
    )
    private val notation = MutableStateFlow(calculateNotationFromGrid(grid.value))

    private val _uiState = MutableStateFlow(
        MethodBuilderState(
            stage.value,
            grid.value,
            notation.value,
            selectedGrid.value,
            null,
        ),
    )
    val uiState = _uiState.asStateFlow()
    private val _methodEvent = MutableSharedFlow<MethodWithCalls>()
    val methodEvent = _methodEvent.asSharedFlow()

    init {
        combine(
            stage,
            grid,
            notation,
            selectedGrid,
        ) { stage, grid, notation, selected ->
            val selectedGrid: Pair<Int, Int>?
            val selectedNotation: Int?
            if (selected.first == stage) {
                selectedGrid = null
                selectedNotation = selected.second
            } else {
                selectedGrid = selected
                selectedNotation = null
            }
            MethodBuilderState(
                stage,
                grid,
                notation,
                selectedGrid,
                selectedNotation,
            )
        }.onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: MethodBuilderEvent) {
        when (event) {
            is StageSelected -> {
                stage.value = event.stage
                grid.value = List(event.stage * 4 + 1) { idx ->
                    if (idx == 0) {
                        Array(stage.value) { it + 1 }
                    } else {
                        Array(stage.value) { null }
                    }
                }
                notation.value = calculateNotationFromGrid(grid.value)
            }

            is MethodBuilderEvent.GridCellDelta -> {
                selectedGrid.update { current ->
                    Pair(
                        (current.first + event.delta.first).coerceIn(0, stage.value),
                        (current.second + event.delta.second).coerceIn(0, grid.value.size - 1),
                    )
                }
            }

            is GridCellSelected -> {
                selectedGrid.value = Pair(event.col, event.row)
            }

            is NotationCellSelected -> {
                selectedGrid.value = Pair(stage.value, event.row)
            }

            is MethodBuilderEvent.KeyboardEntry -> {
                val coords = selectedGrid.value
                if (coords.first < stage.value) {
                    val newDigit = event.entry?.get(0)?.toBellDigit()
                    if (newDigit != null && newDigit > stage.value) return
                    grid.update { current ->
                        val new = current.toMutableList()
                        val oldRow = new.removeAt(coords.second).copyOf()
                        oldRow[coords.first] = event.entry?.get(0)?.toBellDigit()
                        new.add(coords.second, oldRow)
                        new
                    }
                    notation.update { calculateNotationFromGrid(grid.value) }
                }
            }

            MethodBuilderEvent.Reset -> {
                grid.value = List(stage.value * 4 + 1) { idx ->
                    if (idx == 0) {
                        Array(stage.value) { it + 1 }
                    } else {
                        Array(stage.value) { null }
                    }
                }
                notation.value = calculateNotationFromGrid(grid.value)
            }
            MethodBuilderEvent.Search -> {
                viewModelScope.launch {
                    val notation = notation.value
                        .dropLastWhile { it == null }
                    val notationNotNull = notation.mapNotNull { it }
                    if (notation.size != notationNotNull.size) return@launch
                    val pn = placeNotationForFull(notationNotNull)
                    val method = methodRepository.searchByPlaceNotation(pn) ?: MethodWithCalls.fromPlaceNotation(
                            "Unnamed",
                            stage.value,
                            pn,
                        )
                    _methodEvent.emit(method)
                }
            }
        }
    }

    private fun calculateNotationFromGrid(grid: List<Array<Int?>>): List<String?> {
        return grid.windowed(2).map { (row1, row2) ->
            val notation = mutableListOf<String>()
            row1.forEachIndexed { idx, bell ->
                if (bell == null) {
                    return@map null
                }
                val newIdx = row2.indexOf(bell)
                if (newIdx < 0 || abs(idx - newIdx) > 1) {
                    return@map null
                }
                if (row2.getOrNull(idx) == bell) {
                    notation.add(idx.plus(1).toBellChar())
                }
            }
            if (notation.isEmpty()) {
                "x"
            } else {
                notation.joinToString("")
            }
        }
    }

    private fun placeNotationForFull(notation: List<String>): PlaceNotation {
        // Look for palindromic notation
        // Just look for a single bit at the start or end
        if (notation.size % 2 != 0) {
            return PlaceNotation(listOf(notation))
        }
        if (notation.dropLast(1).isPalindromic()) {
            return PlaceNotation(
                listOf(
                    notation.take(notation.size / 2),
                    notation.takeLast(1),
                )
            )
        }
        if (notation.drop(1).isPalindromic()) {
            return PlaceNotation(
                listOf(
                    notation.take(1),
                    notation.drop(1).take(notation.size / 2),
                )
            )
        }
        return PlaceNotation(listOf(notation))
    }

    private fun List<String>.isPalindromic(): Boolean {
        return this == this.asReversed()
    }

    sealed class MethodBuilderEvent {
        data class StageSelected(val stage: Int) : MethodBuilderEvent()
        data class GridCellSelected(val row: Int, val col: Int) : MethodBuilderEvent()
        data class GridCellDelta(val delta: Pair<Int, Int>) : MethodBuilderEvent()
        data class NotationCellSelected(val row: Int) : MethodBuilderEvent()
        data class KeyboardEntry(val entry: String?) : MethodBuilderEvent()
        data object Reset : MethodBuilderEvent()
        data object Search : MethodBuilderEvent()
        // data class KeyEvent(val event: com.jpd.methodcards.presentation.KeyEvent) :
        //     MethodBuilderEvent()
    }

    object Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return MethodBuilderViewModel() as T
        }
    }
}
