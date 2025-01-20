package com.jpd.methodcards

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jpd.methodcards.presentation.App
import com.jpd.methodcards.presentation.KeyDirection
import com.jpd.methodcards.presentation.LocalKeyEvents

fun main() = application {
    // val descriptors = listOf(
    //     MethodsProto.serializer().descriptor,
    //     MethodProto.serializer().descriptor,
    //     MethodProto.CallProto.serializer().descriptor,
    // )
    // val schemas = ProtoBufSchemaGenerator.generateSchemaText(descriptors)
    // println(schemas)
    val cbs = LocalKeyEvents.current
    Window(
        onCloseRequest = ::exitApplication,
        title = "KotlinProject",
        onPreviewKeyEvent = { event ->
            when (event.key) {
                Key.DirectionDown -> KeyDirection.Down
                Key.DirectionLeft -> KeyDirection.Left
                Key.DirectionRight -> KeyDirection.Right
                else -> null
            }?.let { dir ->
                if (event.type == KeyEventType.KeyDown) {
                    cbs.asReversed().fold(false) { acc, cb ->
                        if (!acc) {
                            cb.invoke(dir)
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
