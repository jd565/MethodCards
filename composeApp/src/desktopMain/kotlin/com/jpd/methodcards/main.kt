package com.jpd.methodcards

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jpd.methodcards.presentation.App
import com.jpd.methodcards.presentation.KeyDirection
import com.jpd.methodcards.presentation.KeyEvent
import com.jpd.methodcards.presentation.LocalKeyEvents
import com.jpd.methodcards.presentation.permissions.PermissionBridge
import com.jpd.methodcards.presentation.permissions.PermissionResultCallback
import com.jpd.methodcards.presentation.permissions.PermissionsBridgeListener

fun main() = application {
    // val descriptors = listOf(
    //     MethodsProto.serializer().descriptor,
    //     MethodProto.serializer().descriptor,
    //     MethodProto.CallProto.serializer().descriptor,
    //     MethodProto.StandardCalls.serializer().descriptor,
    // )
    // val schemas = ProtoBufSchemaGenerator.generateSchemaText(descriptors)
    // println(schemas)
    PermissionBridge.setListener(PermissionsBridgeListenerImpl)
    val cbs = LocalKeyEvents.current
    Window(
        onCloseRequest = ::exitApplication,
        title = "KotlinProject",
        onPreviewKeyEvent = { event ->
            when (event.key) {
                Key.DirectionDown, Key.J -> KeyDirection.Down
                Key.DirectionLeft, Key.K -> KeyDirection.Left
                Key.DirectionRight, Key.L -> KeyDirection.Right
                Key.DirectionUp -> KeyDirection.Up
                Key.A -> KeyDirection.A
                Key.S -> KeyDirection.S
                Key.D -> KeyDirection.D
                Key.One, Key.NumPad1 -> KeyDirection.Bell("1")
                Key.Two, Key.NumPad2 -> KeyDirection.Bell("2")
                Key.Three, Key.NumPad3 -> KeyDirection.Bell("3")
                Key.Four, Key.NumPad4 -> KeyDirection.Bell("4")
                Key.Five, Key.NumPad5 -> KeyDirection.Bell("5")
                Key.Six, Key.NumPad6 -> KeyDirection.Bell("6")
                Key.Seven, Key.NumPad7 -> KeyDirection.Bell("7")
                Key.Eight, Key.NumPad8 -> KeyDirection.Bell("8")
                Key.Nine, Key.NumPad9 -> KeyDirection.Bell("9")
                Key.Zero, Key.NumPad0 -> KeyDirection.Bell("0")
                Key.E -> KeyDirection.Bell("E")
                Key.T -> KeyDirection.Bell("T")
                Key.B -> KeyDirection.Bell("B")
                Key.C -> KeyDirection.Bell("C")
                Key.Back, Key.Backspace, Key.Delete -> KeyDirection.Delete
                Key.Z -> if (event.isCtrlPressed) {
                    KeyDirection.Undo
                } else { null }
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

private object PermissionsBridgeListenerImpl : PermissionsBridgeListener {
    override fun requestMicPermission(callback: PermissionResultCallback) {
        callback.onPermissionGranted()
    }

    override fun isMicPermissionGranted(): Boolean {
        return true
    }
}
