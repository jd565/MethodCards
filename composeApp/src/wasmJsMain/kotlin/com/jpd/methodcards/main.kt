package com.jpd.methodcards

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.jpd.methodcards.data.library.MethodLibrary
import com.jpd.methodcards.presentation.App
import com.jpd.methodcards.presentation.KeyDirection
import com.jpd.methodcards.presentation.KeyEvent
import com.jpd.methodcards.presentation.LocalKeyEvents
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.events.KeyboardEvent

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    GlobalScope.launch {
        MethodLibrary().getMethods()
    }
    ComposeViewport(document.body!!) {
        val cbs = LocalKeyEvents.current
        DisposableEffect(cbs) {
            document.onkeydown = { event ->
                event.keyDirection?.let { dir ->
                    cbs.asReversed().forEach {
                        it.invoke(dir, KeyEvent.Down)
                    }
                }
            }
            document.onkeyup = { event ->
                event.keyDirection?.let { dir ->
                    cbs.asReversed().forEach {
                        it.invoke(dir, KeyEvent.Up)
                    }
                }
            }
            onDispose {
                document.onkeydown = null
                document.onkeyup = null
            }
        }
        App()
    }
}

private val KeyboardEvent.keyDirection: KeyDirection? get() = when (key) {
    "ArrowLeft", "j", "J" -> KeyDirection.Left
    "ArrowDown", "k", "K" -> KeyDirection.Down
    "ArrowRight", "l", "L" -> KeyDirection.Right
    "a", "A" -> KeyDirection.A
    "s", "S" -> KeyDirection.S
    "d", "D" -> KeyDirection.D
    else -> null
}
