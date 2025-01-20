package com.jpd.methodcards

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.jpd.methodcards.data.MethodLibrary
import com.jpd.methodcards.presentation.App
import com.jpd.methodcards.presentation.KeyDirection
import com.jpd.methodcards.presentation.LocalKeyEvents
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    GlobalScope.launch {
        MethodLibrary().getMethods()
    }
    ComposeViewport(document.body!!) {
        val cbs = LocalKeyEvents.current
        DisposableEffect(cbs) {
            document.onkeydown = { event ->
                when (event.key) {
                    "ArrowLeft" -> KeyDirection.Left
                    "ArrowDown" -> KeyDirection.Down
                    "ArrowRight" -> KeyDirection.Right
                    else -> null
                }?.let { dir ->
                    cbs.asReversed().forEach {
                        it.invoke(dir)
                    }
                }
            }
            onDispose {
                document.onkeydown = null
            }
        }
        App()
    }
}
