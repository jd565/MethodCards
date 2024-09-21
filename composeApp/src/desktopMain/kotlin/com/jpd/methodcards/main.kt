package com.jpd.methodcards

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jpd.methodcards.presentation.App

fun main() = application {
    // val descriptors = listOf(
    //     MethodsProto.serializer().descriptor,
    //     MethodProto.serializer().descriptor,
    //     MethodProto.CallProto.serializer().descriptor,
    // )
    // val schemas = ProtoBufSchemaGenerator.generateSchemaText(descriptors)
    // println(schemas)
    Window(
        onCloseRequest = ::exitApplication,
        title = "KotlinProject",
    ) {
        App()
    }
}
