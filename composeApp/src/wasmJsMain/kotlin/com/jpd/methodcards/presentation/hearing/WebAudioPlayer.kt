package com.jpd.methodcards.presentation.hearing

import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.w3c.dom.Audio

class WasmAudioPlayer : AudioPlayer {
    private val audios = GlobalScope.async {
        val base = window.document.URL
        val url = buildString {
            append(base.substringBefore('#'))
            if (!base.endsWith("/")) {
                append("/")
            }
            append(
                "composeResources/methodcards.composeapp.generated.resources/files/major_scale_note_1.wav"
            )
        }
        List(8) { Audio(url.replace("1", "${it + 1}")) }
    }

    override fun playBell(bell: Int) {
        GlobalScope.launch {
            try {
                audios.await()[bell - 1].play()
            } catch (e: Exception) {
                println("Error playing audio: ${e.message}")
            }
        }
    }
}
