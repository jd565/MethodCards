package com.jpd.methodcards.presentation.flashcard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.jpd.methodcards.data.MethodRepository
import com.jpd.methodcards.domain.MethodWithCalls
import com.jpd.methodcards.domain.toBellChar
import com.jpd.methodcards.presentation.NoMethodSelectedView
import com.jpd.methodcards.presentation.blueline.blueLineDetails
import com.jpd.methodcards.presentation.blueline.calculateBlueLineStyle
import com.jpd.methodcards.presentation.blueline.drawLeadIndicators
import com.jpd.methodcards.presentation.blueline.drawRows
import com.jpd.methodcards.presentation.ui.MultiMethodTopBar
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import kotlin.reflect.KClass

private const val FlashCardExplainer = "On this screen you can select a number of methods and it will prompt you " +
    "with a method and place bell. You can think about what that place bell does and then reveal the answer to check."

@Composable
fun FlashCardScreen(
    modifier: Modifier = Modifier,
    navigateToAppSettings: () -> Unit,
) {
    val controller: FlashCardController = viewModel(factory = FlashCardController.Factory)
    val model = controller.uiState.collectAsState().value

    if (model != null) {
        FlashCardView(
            model = model,
            modifier = modifier,
            nextCard = remember(controller) { controller::nextCard },
            addMethodClicked = navigateToAppSettings,
        )
    } else {
        Box(modifier)
    }
}

@Composable
fun FlashCardTopBar(
    backStackEntry: NavBackStackEntry,
    navigateToMultiMethodSelection: () -> Unit,
    navigationIcon: @Composable () -> Unit,
) {
    val controller: FlashCardController = viewModel(
        viewModelStoreOwner = backStackEntry,
        factory = FlashCardController.Factory
    )

    val model = controller.uiState.collectAsState().value
    var showExplainer by remember { mutableStateOf(false) }
    MultiMethodTopBar(
        (model as? FlashCardMethodModel)?.selectionDescription,
        explainerClicked = { showExplainer = true },
        settingsClicked = {},
        navigateToMultiMethodSelection = navigateToMultiMethodSelection,
        navigationIcon = navigationIcon,
    )

    if (showExplainer) {
        AlertDialog(
            onDismissRequest = { showExplainer = false },
            confirmButton = {},
            title = {
                Text("Flash Card Screen")
            },
            text = {
                Text(FlashCardExplainer)
            },
        )
    }
}

@Composable
private fun FlashCardView(
    model: FlashCardUiModel,
    modifier: Modifier,
    nextCard: () -> Unit,
    addMethodClicked: () -> Unit,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (model is FlashCardMethodModel) {
            FlashCardContent(model, nextCard, modifier = Modifier.weight(1f))
        } else {
            NoMethodSelectedView(
                modifier = Modifier.weight(1f),
                addMethodClicked = addMethodClicked,
            )
        }
    }
}

@Composable
private fun FlashCardContent(
    model: FlashCardMethodModel,
    nextCard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val number =
        when (model.place) {
            1, 21, 31 -> "${model.place}st"
            2, 22, 32 -> "${model.place}nd"
            3, 23 -> "${model.place}rd"
            else -> "${model.place}th"
        }
    val title = "$number Place Bell ${model.method.shortName(model.methods)}"

    val revealed =
        remember(model.method.name, model.place) {
            mutableStateOf(false)
        }

    Text(
        title,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 24.dp),
    )
    Spacer(Modifier.height(8.dp))
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = if (revealed.value) 1f else 0f
                    },
            contentAlignment = Alignment.Center,
        ) {
            val placeMeasurer = rememberTextMeasurer()
            val measurer = rememberTextMeasurer(model.method.stage)
            val lead = model.method.leads[0].lead
            val textColor = LocalContentColor.current
            Canvas(modifier = Modifier.fillMaxSize()) {
                val style =
                    calculateBlueLineStyle(
                        measurer = measurer,
                        maxHeight = size.height - 8.dp.toPx(),
                        numberOfRows = lead.size,
                    )

                val results =
                    (1..model.method.stage).map { bell ->
                        measurer.measure(
                            bell.toBellChar(),
                            style = style,
                        )
                    }

                val spacing = results[0].size.height.toFloat()
                val totalWidth = spacing * model.method.stage
                val totalHeight = spacing * lead.size
                val startX = (size.width - totalWidth) / 2
                val startY = (size.height - totalHeight) / 2

                val rowSize =
                    drawRows(
                        rows = lead,
                        placeCharResults = results,
                        topLeft = Offset(startX, startY),
                        blueLines =
                            blueLineDetails(
                                listOf(model.place),
                                model.method.huntBells,
                            ),
                        ruleoffsEvery = model.method.ruleoffsEvery,
                        ruleoffsFrom = model.method.ruleoffsFrom,
                        textColor = textColor,
                    )

                val startPlace = model.place
                val endPlace =
                    lead
                        .last()
                        .row
                        .indexOf(startPlace)
                        .plus(1)

                val leadIndicatorSize =
                    drawLeadIndicators(
                        measurer = placeMeasurer,
                        blueLineStyle = style,
                        bells = listOf(startPlace),
                        topLeft = Offset(startX + rowSize.width + 4.dp.toPx(), startY),
                        textColor = textColor,
                    )

                drawLeadIndicators(
                    measurer = placeMeasurer,
                    blueLineStyle = style,
                    bells = listOf(endPlace),
                    topLeft = Offset(
                        startX + rowSize.width + 4.dp.toPx(),
                        startY + rowSize.height - leadIndicatorSize.height,
                    ),
                    textColor = textColor,
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Button(
        onClick = {
            if (!revealed.value) {
                revealed.value = true
            } else {
                nextCard()
            }
        },
    ) {
        Text(if (revealed.value) "Next" else "Reveal")
    }
}

private sealed class FlashCardUiModel
private data class FlashCardMethodModel(
    val method: MethodWithCalls,
    val place: Int,
    val methods: List<MethodWithCalls>,
    val selectionDescription: String,
) : FlashCardUiModel()

private data object FlashCardEmptyModel : FlashCardUiModel()

@OptIn(ExperimentalCoroutinesApi::class)
private class FlashCardController(
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    methodRepository: MethodRepository = MethodRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow<FlashCardUiModel?>(null)
    val uiState = _uiState.asStateFlow()

    private val place =
        MutableStateFlow<Pair<MethodWithCalls, Int>?>(null)

    private var methodIterator: Iterator<Pair<MethodWithCalls, Int>> = iterator {}

    init {
        methodRepository.observeSelectedMethods()
            .map { methods ->
                Pair(
                    methods,
                    methods.filter { it.enabledForMultiMethod }
                        .takeIf { it.isNotEmpty() } ?: methods,
                )
            }
            .flatMapLatest { (methods, selectedMethods) ->
                if (methods.isEmpty()) {
                    flowOf(FlashCardEmptyModel)
                } else {
                    methodIterator = buildIterator(selectedMethods)
                    place.value = methodIterator.next()
                    place.filterNotNull().map { (method, place) ->
                        val selectedName =
                            when {
                                selectedMethods.size == 1 -> selectedMethods.first().shortName(methods)
                                selectedMethods.size < methods.size -> "Some methods (${selectedMethods.size})"
                                else -> "All methods (${methods.size})"
                            }

                        FlashCardMethodModel(
                            method = method,
                            place = place,
                            methods = methods,
                            selectionDescription = selectedName,
                        )
                    }
                }
            }.onEach { _uiState.value = it }
            .launchIn(viewModelScope + defaultDispatcher)
    }

    fun nextCard() {
        place.value = methodIterator.next()
    }

    private fun buildIterator(selectedMethods: List<MethodWithCalls>): Iterator<Pair<MethodWithCalls, Int>> = iterator {
        var last: Pair<MethodWithCalls, Int>? = null
        val allMethodPlaces = selectedMethods.flatMapTo(mutableListOf()) { method ->
            val places = method.leadCycles.flatten().map { method to it }
            List(method.multiMethodFrequency.frequency) { places }.flatten()
        }
        while (true) {
            allMethodPlaces.shuffle()
            while (last == allMethodPlaces.first()) {
                allMethodPlaces.shuffle()
            }
            last = allMethodPlaces.last()
            yieldAll(allMethodPlaces)
        }
    }

    object Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return FlashCardController() as T
        }
    }
}
