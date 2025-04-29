package com.jpd.methodcards.presentation.listener

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

class AudioRecorderImpl(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AudioRecorder {
    @SuppressLint("MissingPermission")
    override fun observeAudio(sampleRate: Int, bufferSize: Int): Flow<DoubleArray> = callbackFlow {
        val internalBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_FLOAT,
        )
        require(internalBufferSize != AudioRecord.ERROR_BAD_VALUE)
        require(internalBufferSize != AudioRecord.ERROR)
        val audioRecord =
            AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                        .build(),
                )
                .setBufferSizeInBytes(internalBufferSize)
                .build().apply { startRecording() }
        val floatBuffer = FloatArray(bufferSize / 4)
        try {
            while (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord.read(floatBuffer, 0, floatBuffer.size, AudioRecord.READ_BLOCKING)
                trySend(floatBuffer.toDoubleArray())
            }
        } catch(e : Exception){
            println(e.message)
        }
        audioRecord.release()
        awaitClose {
            println("Closing audio recorder")
            audioRecord.stop()
        }
    }.flowOn(ioDispatcher)
}

fun FloatArray.toDoubleArray(): DoubleArray {
    val doubleArray = DoubleArray(size)
    for (i in indices) {
        doubleArray[i] = this[i].toDouble()
    }
    return doubleArray
}
