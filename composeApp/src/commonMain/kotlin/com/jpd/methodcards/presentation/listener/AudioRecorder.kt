package com.jpd.methodcards.presentation.listener

import kotlinx.coroutines.flow.Flow

interface AudioRecorder {
    fun observeAudio(sampleRate: Int, bufferSize: Int = 1024): Flow<DoubleArray>
}
