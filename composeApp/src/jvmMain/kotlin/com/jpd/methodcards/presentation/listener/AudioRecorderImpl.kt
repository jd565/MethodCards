package com.jpd.methodcards.presentation.listener

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine

class AudioRecorderImpl(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AudioRecorder {
    override fun observeAudio(sampleRate: Int, bufferSize: Int): Flow<DoubleArray> = flow {
        val format = AudioFormat(
            sampleRate.toFloat(), // sampleRate
            16, // sampleSizeInBits
            1, // channels (mono)
            true, // signed
            false, // bigEndian
        )
        val info = DataLine.Info(TargetDataLine::class.java, format)
        if (!AudioSystem.isLineSupported(info)) {
            throw Exception("Unsupported audio format or no available lines")
        }

        val line = AudioSystem.getLine(info) as TargetDataLine
        line.open(format)
        line.start()

        val internalBuffer = DoubleArray(bufferSize)
        var internalBufferIndex = 0
        val lineBufferSize = line.bufferSize
        val buffer = ByteArray(lineBufferSize)

        try {
            while (line.isOpen) {
                val bytesRead = line.read(buffer, 0, buffer.size)
                if (bytesRead > 0) {
                    val doubleArray = convertByteArrayToDoubleArray(buffer, bytesRead)
                    for (sample in doubleArray) {
                        internalBuffer[internalBufferIndex++] = sample
                        if (internalBufferIndex == bufferSize) {
                            emit(internalBuffer.copyOf())
                            internalBufferIndex = 0
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Error reading audio data: ${e.message}")
        } finally {
            println("Recording terminated")
            line.stop()
            line.close()
        }
    }.flowOn(ioDispatcher)

    private fun convertByteArrayToDoubleArray(byteArray: ByteArray, bytesRead: Int): DoubleArray {
        val doubleArray = DoubleArray(bytesRead / 2) // 16-bit audio (2 bytes per sample)
        var sampleIndex = 0
        for (i in 0 until bytesRead step 2) {
            // Convert two bytes to a 16-bit signed integer
            val sample = (byteArray[i + 1].toInt() shl 8) or (byteArray[i].toInt() and 0xFF)
            // Normalize to a range of -1.0 to 1.0
            doubleArray[sampleIndex++] = sample / 32768.0
        }
        return doubleArray
    }
}
