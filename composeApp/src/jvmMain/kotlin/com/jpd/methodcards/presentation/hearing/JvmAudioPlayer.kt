package com.jpd.methodcards.presentation.hearing

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import methodcards.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.DataLine
import javax.sound.sampled.FloatControl

class JvmAudioPlayer : AudioPlayer {

    private var currentClip: Clip? = null

    @OptIn(ExperimentalResourceApi::class)
    private val bellAudio: Deferred<List<ByteArray>> = GlobalScope.async {
        List(8) {
            Res.readBytes("files/major_scale_note_${it + 1}.wav")
        }
    }

    override fun playBell(bell: Int) {
        GlobalScope.launch {
            try {
                val audioInputStream: AudioInputStream =
                    AudioSystem.getAudioInputStream(bellAudio.await()[bell - 1].inputStream())
                val format: AudioFormat = audioInputStream.format
                val info = DataLine.Info(Clip::class.java, format)
                val clip = AudioSystem.getLine(info) as Clip

                clip.addLineListener { event ->
                    if (event.type == javax.sound.sampled.LineEvent.Type.STOP) {
                        // Ensure the clip is reset or closed when it stops
                        clip.close()
                    }
                }

                clip.open(audioInputStream)
                // Adjust volume (optional)
                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
                    val minGain = gainControl.minimum
                    val maxGain = gainControl.maximum
                    val targetGain = (maxGain - minGain) / 2 + minGain
                    gainControl.value = targetGain
                }

                clip.start()
            } catch (e: Exception) {
                println("Error playing audio: ${e.message}")
            }
        }
    }
}
