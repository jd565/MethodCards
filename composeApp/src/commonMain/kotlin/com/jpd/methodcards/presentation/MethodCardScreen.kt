package com.jpd.methodcards.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.jpd.methodcards.presentation.icons.Blueline
import com.jpd.methodcards.presentation.icons.Flashcard
import com.jpd.methodcards.presentation.icons.Hearing
import com.jpd.methodcards.presentation.icons.Simulator
import kotlinx.serialization.SerialName
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
                    OverUnder,
                    FlashCard,
                    Simulator,
                    Compose,
                    Settings,
                    MethodBuilder,
                    // RingingListener,
                    HearingTrainer,
                    Configuration,
                )
        }
    }

    @Serializable @SerialName("BlueLine")
    data object BlueLine : TopLevel
    @Serializable @SerialName("FlashCard")
    data object FlashCard : TopLevel
    @Serializable @SerialName("Simulator")
    data object Simulator : TopLevel
    @Serializable @SerialName("MethodSelection")
    data object Settings : TopLevel
    @Serializable @SerialName("Compose")
    data object Compose : TopLevel
    @Serializable @SerialName("OverUnder")
    data object OverUnder : TopLevel
    @Serializable @SerialName("MethodBuilder")
    data object MethodBuilder : TopLevel
    @Serializable @SerialName("RingingListener")
    data object RingingListener : TopLevel
    @Serializable @SerialName("HearingTrainer")
    data object HearingTrainer : TopLevel
    @Serializable @SerialName("BlueLineMethodList")
    data object BlueLineMethodList : MethodCardScreen
    @Serializable @SerialName("MultiMethodSelection")
    data object MultiMethodSelection : MethodCardScreen
    @Serializable @SerialName("AddMethod")
    data object AddMethod : MethodCardScreen
    @Serializable @SerialName("SingleBlueLine")
    data class SingleMethodBlueLine(
        val name: String,
        val placeNotation: String,
        val stage: Int,
    ) : MethodCardScreen
    @Serializable @SerialName("SingleSimulator")
    data class SingleMethodSimulator(val methodName: String, val placeNotation: String, val stage: Int) : MethodCardScreen
    @Serializable @SerialName("Configuration")
    data object Configuration : TopLevel

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
                        SingleMethodSimulator::class,
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
        MethodCardScreen.SingleMethodSimulator::class -> "Simulator"
        MethodCardScreen.MethodBuilder::class -> "Method Builder"
        MethodCardScreen.RingingListener::class -> "Ringing Listener"
        MethodCardScreen.HearingTrainer::class -> "Hearing Trainer"
        MethodCardScreen.Configuration::class -> "Configuration"
        else -> "Unknown"
    }

val MethodCardScreen.TopLevel.icon: ImageVector
    get() = when (this) {
        MethodCardScreen.BlueLine -> Icons.Filled.Blueline
        MethodCardScreen.Compose -> Icons.Filled.Build
        MethodCardScreen.FlashCard -> Icons.Filled.Flashcard
        MethodCardScreen.OverUnder -> Icons.Filled.ShoppingCart
        MethodCardScreen.Settings -> Icons.Filled.Menu
        MethodCardScreen.Simulator -> Icons.Filled.Simulator
        MethodCardScreen.MethodBuilder -> Icons.Filled.AddCircle
        MethodCardScreen.RingingListener -> Icons.Filled.Hearing
        MethodCardScreen.HearingTrainer -> Icons.Filled.Hearing
        MethodCardScreen.Configuration -> Icons.Filled.Settings
    }
