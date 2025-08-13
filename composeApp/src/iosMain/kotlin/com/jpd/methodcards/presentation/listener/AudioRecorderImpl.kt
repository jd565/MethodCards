package com.jpd.methodcards.presentation.listener

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class AudioRecorderImpl(
) : AudioRecorder {
    override fun observeAudio(sampleRate: Int, bufferSize: Int): Flow<DoubleArray> = MutableSharedFlow()
}
