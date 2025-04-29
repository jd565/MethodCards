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

@Suppress("OPT_IN_USAGE")
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
    "ArrowUp" -> KeyDirection.Up
    "a", "A" -> KeyDirection.A
    "s", "S" -> KeyDirection.S
    "d", "D" -> KeyDirection.D
    "1" -> KeyDirection.Bell("1")
    "2" -> KeyDirection.Bell("2")
    "3" -> KeyDirection.Bell("3")
    "4" -> KeyDirection.Bell("4")
    "5" -> KeyDirection.Bell("5")
    "6" -> KeyDirection.Bell("6")
    "7" -> KeyDirection.Bell("7")
    "8" -> KeyDirection.Bell("8")
    "9" -> KeyDirection.Bell("9")
    "0" -> KeyDirection.Bell("0")
    "E", "e" -> KeyDirection.Bell("E")
    "T", "t" -> KeyDirection.Bell("T")
    "B", "b" -> KeyDirection.Bell("B")
    "C", "c" -> KeyDirection.Bell("C")
    "Undo" -> KeyDirection.Undo
    "Delete", "Backspace", "Clear" -> KeyDirection.Delete
    else -> null
}
