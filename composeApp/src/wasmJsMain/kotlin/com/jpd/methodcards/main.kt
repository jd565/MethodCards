package com.jpd.methodcards

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.jpd.methodcards.data.MethodLibrary
import com.jpd.methodcards.di.MethodCardDi
import com.jpd.methodcards.presentation.App
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    MethodCardDi.init(localStorage)
    GlobalScope.launch {
        MethodLibrary().getMethods()
    }
    ComposeViewport(document.body!!) {
        App()
    }
}
