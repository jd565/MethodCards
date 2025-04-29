package com.jpd.methodcards.presentation.listener

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class AudioFFTState(
    val frequencies: SnapshotStateList<Pair<Double, Double>> = mutableStateListOf(),
    val isRecording: Boolean = false,
    val hasPermission: Boolean = false,
    val error: String? = null,
)
