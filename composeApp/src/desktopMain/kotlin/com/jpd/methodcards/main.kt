package com.jpd.methodcards

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jpd.methodcards.presentation.App
import com.jpd.methodcards.presentation.KeyDirection
import com.jpd.methodcards.presentation.KeyEvent
import com.jpd.methodcards.presentation.LocalKeyEvents

fun main() = application {
    // val descriptors = listOf(
    //     MethodsProto.serializer().descriptor,
    //     MethodProto.serializer().descriptor,
    //     MethodProto.CallProto.serializer().descriptor,
    //     MethodProto.StandardCalls.serializer().descriptor,
    // )
    // val schemas = ProtoBufSchemaGenerator.generateSchemaText(descriptors)
    // println(schemas)
    val cbs = LocalKeyEvents.current
    Window(
        onCloseRequest = ::exitApplication,
        title = "KotlinProject",
        onPreviewKeyEvent = { event ->
            when (event.key) {
                Key.DirectionDown, Key.J -> KeyDirection.Down
                Key.DirectionLeft, Key.K -> KeyDirection.Left
                Key.DirectionRight, Key.L -> KeyDirection.Right
                Key.A -> KeyDirection.A
                Key.S -> KeyDirection.S
                Key.D -> KeyDirection.D
                else -> null
            }?.let { dir ->
                val t = when (event.type) {
                    KeyEventType.KeyDown -> KeyEvent.Down
                    KeyEventType.KeyUp -> KeyEvent.Up
                    else -> null
                }
                if (t != null) {
                    cbs.asReversed().fold(false) { acc, cb ->
                        if (!acc) {
                            cb.invoke(dir, t)
                        } else {
                            true
                        }
                    }
                } else {
                    false
                }
            } ?: false
        }
    ) {
        App()
    }
}
