package com.jpd.methodcards.presentation.listener

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AudioFFTScreen(modifier: Modifier = Modifier) {
    val viewModel: AudioFFTViewModel = viewModel(factory = AudioFFTViewModel.Factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopRecording()
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (uiState.error != null) {
            Text("Error: ${uiState.error}")
        }
        if (uiState.hasPermission) {
            FFTGraph(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                frequencies = uiState.frequencies,
            )
            if (uiState.isRecording) {
                Button(onClick = { viewModel.stopRecording() }) {
                    Text("Stop Recording")
                }
            } else {
                Button(onClick = { viewModel.startRecording() }) {
                    Text("Start Recording")
                }
            }
        } else {
            Button(onClick = { viewModel.requestAudioPermission() }) {
                Text("Request Audio permission")
            }
        }
    }
}

@Composable
fun FFTGraph(
    frequencies: SnapshotStateList<Pair<Double, Double>>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.padding(16.dp)) {
        val width = size.width
        val height = size.height

        // Calculate max and min magnitudes for scaling
        val maxMagnitude = frequencies.maxOfOrNull { it.second } ?: 0.0
        val minMagnitude = frequencies.minOfOrNull { it.second } ?: 0.0

        val barWidth = width / frequencies.size

        // Draw bars
        frequencies.forEachIndexed { idx, (frequency, magnitude) ->
            val x = idx * barWidth
            val barHeight =
                if (maxMagnitude == minMagnitude) 0.0 else ((magnitude - minMagnitude) / (maxMagnitude - minMagnitude)) * height
            if (!barHeight.isNaN()) {
                drawRect(
                    color = Color.Blue,
                    topLeft = Offset(x, height - barHeight.toFloat()),
                    size = Size(width / frequencies.size.toFloat(), barHeight.toFloat()),
                )
            }
        }
    }
}
