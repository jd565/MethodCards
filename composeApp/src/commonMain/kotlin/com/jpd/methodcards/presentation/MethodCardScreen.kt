package com.jpd.methodcards.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.jpd.methodcards.presentation.icons.Blueline
import com.jpd.methodcards.presentation.icons.Flashcard
import com.jpd.methodcards.presentation.icons.Simulator
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Immutable
sealed interface MethodCardScreen {
    @Immutable
    sealed interface TopLevel : MethodCardScreen {
        companion object {
            val entries: List<TopLevel>
                get() = listOf(
                    BlueLine,
                    // OverUnder,
                    FlashCard,
                    Simulator,
                    Compose,
                    Settings,
                )
        }
    }

    @Serializable
    data object BlueLine : TopLevel
    @Serializable
    data object FlashCard : TopLevel
    @Serializable
    data object Simulator : TopLevel
    @Serializable
    data object Settings : TopLevel
    @Serializable
    data object Compose : TopLevel
    @Serializable
    data object OverUnder : TopLevel
    @Serializable
    data object BlueLineMethodList : MethodCardScreen
    @Serializable
    data object MultiMethodSelection : MethodCardScreen
    @Serializable
    data object AddMethod : MethodCardScreen
    @Serializable
    data class SingleMethodBlueLine(val methodName: String) : MethodCardScreen

    companion object {
        val entries: List<KClass<out MethodCardScreen>>
            get() = TopLevel.entries
                .map { it::class }
                .plus(
                    listOf(
                        BlueLineMethodList::class,
                        MultiMethodSelection::class,
                        AddMethod::class,
                        SingleMethodBlueLine::class,
                    ),
                )
    }
}

val KClass<out MethodCardScreen>?.title: String
    get() = when (this) {
        MethodCardScreen.BlueLine::class -> "Blue Line"
        MethodCardScreen.Compose::class -> "Composition"
        MethodCardScreen.FlashCard::class -> "Flash Card"
        MethodCardScreen.OverUnder::class -> "Over Under"
        MethodCardScreen.Settings::class -> "Method Selection"
        MethodCardScreen.Simulator::class -> "Simulator"
        MethodCardScreen.AddMethod::class -> "Add Method"
        MethodCardScreen.BlueLineMethodList::class -> "Select Blue Line Method"
        MethodCardScreen.MultiMethodSelection::class -> "Select Methods"
        MethodCardScreen.SingleMethodBlueLine::class -> "Blue Line"
        else -> "Unknown"
    }

val MethodCardScreen.TopLevel.icon: ImageVector get() = when (this) {
    MethodCardScreen.BlueLine -> Icons.Filled.Blueline
    MethodCardScreen.Compose -> Icons.Filled.Build
    MethodCardScreen.FlashCard -> Icons.Filled.Flashcard
    MethodCardScreen.OverUnder -> Icons.Filled.ShoppingCart
    MethodCardScreen.Settings -> Icons.Filled.Settings
    MethodCardScreen.Simulator -> Icons.Filled.Simulator
}
