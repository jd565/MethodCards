package com.jpd.methodcards.presentation.listener

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.jpd.methodcards.di.MethodCardDi
import com.jpd.methodcards.presentation.permissions.PermissionBridge
import com.jpd.methodcards.presentation.permissions.PermissionResultCallback
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log10
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.seconds

class AudioFFTViewModel(
    private val audioRecorder: AudioRecorder = MethodCardDi.getAudioRecorder(),
    private val permissionBridge: PermissionBridge = PermissionBridge,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AudioFFTState())
    val uiState = _uiState.asStateFlow()

    private var recordingJob: Job? = null
    private val sampleRate = 5000
    private val fftSize = 1024
    private val window = DoubleArray(fftSize)
    private val bellFrequencyCounts = mutableMapOf<Double, Int>()
    private val lockedFrequencies = mutableSetOf<Double>()
    private val lockThreshold = 10

    init {
        // Create a Hanning window
        for (i in 0 until fftSize) {
            window[i] = 0.5 * (1 - cos(2 * PI * i / (fftSize - 1)))
        }

        _uiState.update {
            it.copy(hasPermission = permissionBridge.isMicPermissionGranted())
        }

        viewModelScope.launch {
            while (true) {
                delay(1.seconds)
                println(bellFrequencyCounts)
            }
        }
    }

    fun requestAudioPermission() {
        permissionBridge.requestMicPermission(
            object : PermissionResultCallback {
                override fun onPermissionDenied(isPermanentDenied: Boolean) {
                    println("Permission denied")
                }

                override fun onPermissionGranted() {
                    _uiState.update { it.copy(hasPermission = true) }
                }
            },
        )
    }

    fun startRecording() {
        _uiState.update { it.copy(isRecording = true) }
        recordingJob?.cancel()
        recordingJob = audioRecorder.observeAudio(sampleRate, fftSize)
            .onEach { audioData ->
                calculateFFT(audioData)
            }.launchIn(viewModelScope)
    }

    fun stopRecording() {
        _uiState.update { it.copy(isRecording = false) }
        recordingJob?.cancel()
    }

    private fun calculateFFT(audioData: DoubleArray) {
        if (audioData.size != fftSize) return

        val windowed = audioData.mapIndexed { idx, value -> value * window[idx] }.toDoubleArray()
        val fftData = fft(windowed)

        val f = _uiState.value.frequencies
        f.clear()
        for (i in 0 until fftSize / 16) {
            var magnitude = 0.0
            repeat(8) {
                magnitude += fftData[8 * i + it].magnitude()
            }

            // Convert magnitude to dB scale
            val magnitudeDB = 20 * log10(magnitude)

            // Map bin to frequency
            val frequency = i * sampleRate.toDouble() / fftSize
            f.add(Pair(frequency, magnitudeDB))

            if (isSpike(fftData, i)) {
                if (lockedFrequencies.isEmpty() || lockedFrequencies.contains(frequency)) {
                    bellFrequencyCounts[frequency] = (bellFrequencyCounts[frequency] ?: 0) + 1
                }
            }
        }
        lockOnCommonFrequencies()
    }

    private fun isSpike(fftData: Array<Complex>, index: Int): Boolean {
        val magnitude = fftData[index].magnitude()
        val threshold = 5 // Adjust this threshold as needed

        // Check if it's significantly higher than the neighbors
        val isHigherThanLeft = if (index > 0) {
            val leftMagnitude = fftData[index - 1].magnitude()
            magnitude > leftMagnitude * 2
        } else true

        val isHigherThanRight = if (index < fftSize / 2 - 1) {
            val rightMagnitude = fftData[index + 1].magnitude()
            magnitude > rightMagnitude * 2
        } else true

        return magnitude > threshold && isHigherThanLeft && isHigherThanRight
    }

    private fun lockOnCommonFrequencies() {
        val counts = bellFrequencyCounts.toList()
        counts.forEach { (frequency, count) ->
            if (count >= lockThreshold) {
                lockedFrequencies.add(frequency)
            }
        }
    }

    object Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return AudioFFTViewModel() as T
        }
    }
}
