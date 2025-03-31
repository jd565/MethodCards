package com.jpd.methodcards.presentation.blueline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.jpd.methodcards.data.MethodRepository
import com.jpd.methodcards.domain.MethodWithCalls
import com.jpd.methodcards.domain.toBellChar
import com.jpd.methodcards.presentation.NoMethodSelectedView
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.reflect.KClass

private const val BlueLineExplainer = "On this screen you can select an individual method from the enabled methods " +
    "to display the whole blue line for this method.\n\n" +
    "You can use the settings to change the style of the blue line used."

@Composable
fun BlueLineScreen(
    modifier: Modifier = Modifier,
    navigateToAppSettings: () -> Unit,
) {
    val controller: BlueLineController = viewModel(factory = BlueLineController.Factory)
    val model = controller.uiState.collectAsState().value

    if (model != null) {
        BlueLineView(
            model = model,
            modifier = modifier,
            addMethodClicked = navigateToAppSettings,
        )
    } else {
        Box(modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlueLineTopBar(
    backStackEntry: NavBackStackEntry,
    selectMethod: () -> Unit,
    navigationIcon: @Composable () -> Unit,
) {
    val controller: BlueLineController = viewModel(
        viewModelStoreOwner = backStackEntry,
        factory = BlueLineController.Factory,
    )
    val model = controller.uiState.collectAsStateWithLifecycle().value
    var showExplainer by remember { mutableStateOf(false) }
    TopAppBar(
        title = {
            when (model) {
                is BlueLineMethodsModel -> {
                    Box(
                        modifier = Modifier.clickable(onClick = selectMethod),
                    ) {
                        Row(
                            modifier = Modifier.heightIn(48.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(model.selectedMethod.name)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Default.KeyboardArrowRight, contentDescription = null)
                        }
                    }
                }

                is SingleBlueLineModel -> {
                    Text(model.method.name)
                }

                else -> Unit
            }
        },
        actions = {
            IconButton(onClick = { showExplainer = true }) {
                Icon(Icons.Outlined.Info, contentDescription = "Explainer")
            }
        },
        navigationIcon = navigationIcon,
    )

    if (showExplainer) {
        AlertDialog(
            onDismissRequest = { showExplainer = false },
            confirmButton = {},
            title = {
                Text("Blue Line Screen")
            },
            text = {
                Text(BlueLineExplainer)
            },
        )
    }
}

@Composable
private fun BlueLineView(
    model: BlueLineUiModel,
    modifier: Modifier,
    addMethodClicked: () -> Unit,
) {
    when (model) {
        is BlueLineMethodsModel -> BlueLinePager(model.selectedMethod, modifier = modifier)
        is SingleBlueLineModel -> BlueLinePager(model.method, modifier = modifier)
        else -> NoMethodSelectedView(modifier = modifier, addMethodClicked = addMethodClicked)
    }
}

@Composable
private fun BlueLinePager(
    method: MethodWithCalls,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        val pagerState = rememberPagerState { 2 }
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.Transparent,
            tabs = {
                val scope = rememberCoroutineScope()
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text("Leads") },
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text("Grid") },
                )
            },
        )
        HorizontalPager(
            pagerState,
            modifier = Modifier.fillMaxWidth().weight(1f),
            userScrollEnabled = false,
        ) { page ->
            if (page == 0) {
                BlueLine(method, modifier = Modifier.fillMaxWidth())
            } else {
                BlueLineGrid(method, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun BlueLine(
    method: MethodWithCalls,
    modifier: Modifier = Modifier,
) {
    val placeMeasurer = rememberTextMeasurer()
    val measurer = rememberTextMeasurer(method.stage)
    val leads = method.leads
    val l = leads[0].lead
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = modifier.horizontalScroll(scrollState)
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    coroutineScope.launch {
                        scrollState.scrollBy(-delta)
                    }
                },
            ),
    ) {
        with(LocalDensity.current) {
            val maxHeight = constraints.maxHeight - 8.dp.toPx()
            val style =
                calculateBlueLineStyle(
                    measurer,
                    maxHeight,
                    l.size,
                )

            val results =
                (1..method.stage).map { bell ->
                    measurer.measure(
                        bell.toBellChar(),
                        style = style,
                    )
                }

            val totalHeight = results[0].size.height * l.size

            val spacing = results[0].size.height.toFloat()
            val startY = (constraints.maxHeight - totalHeight).div(2f).coerceAtMost(4.dp.toPx())
            val totalWidth = spacing * method.stage

            val placeStyle = style.copy(fontSize = style.fontSize * 2)
            val startPlaceResult = placeMeasurer.measure("1", style = placeStyle).size.height * method.leadCycles.size +
                (4.dp.toPx() * (method.leadCycles.size - 1))

            val plainLeadColumns = leads.size
            val callIndexes = method.callIndexes(false)
            val callColumns = callIndexes.map { it.value.size }.sum()

            val width =
                (totalWidth + 4.dp.toPx() + startPlaceResult) * plainLeadColumns +
                    totalWidth * callColumns +
                    16.dp.toPx() * 2 +
                    8.dp.toPx() * (plainLeadColumns + callColumns)

            val textColor = LocalContentColor.current
            Canvas(modifier = Modifier.width(width.toDp()).fillMaxHeight()) {
                var startX = 16.dp.toPx()
                val places = method.leadCycles.map { it.first() }

                leads.forEachIndexed { index, leadLead ->
                    val rowSize =
                        drawRows(
                            rows = leadLead.lead,
                            placeCharResults = results,
                            topLeft = Offset(x = startX, y = startY),
                            blueLines =
                                blueLineDetails(
                                    places,
                                    method.huntBells,
                                ),
                            ruleoffsEvery = method.ruleoffsEvery,
                            ruleoffsFrom = method.ruleoffsFrom,
                            textColor = textColor,
                        )

                    val startPlaces = places.map { place ->
                        leadLead.lead
                            .first()
                            .row
                            .indexOf(place)
                            .plus(1)
                    }

                    val leadSize =
                        drawLeadIndicators(
                            measurer = placeMeasurer,
                            blueLineStyle = style,
                            bells = startPlaces,
                            topLeft = Offset(
                                x = startX + rowSize.width + 4.dp.toPx(),
                                y = startY,
                            ),
                            textColor = textColor,
                        )

                    startX += rowSize.width + 4.dp.toPx() + leadSize.width + 8.dp.toPx()
                }

                callIndexes.flatMap { (idx, calls) -> calls.map { idx to it } }
                    .forEach { (idx, fullCall) ->
                        val title = if (callIndexes.size == 1) {
                            "${fullCall.name}:"
                        } else {
                            "${fullCall.name} @ $idx:"
                        }
                        val nameResult = measurer.measure(title, style = style)
                        drawText(
                            nameResult,
                            color = Color.Black,
                            topLeft = Offset(
                                x = startX + (spacing - results[0].size.width) / 2,
                                y = startY,
                            ),
                        )

                        val yOffset = startY + spacing + 4.dp.toPx()

                        val rowSize =
                            drawRows(
                                rows = fullCall.rows,
                                placeCharResults = results,
                                topLeft = Offset(x = startX, y = yOffset),
                                blueLines =
                                    blueLineDetails(
                                        fullCall.affectedBells,
                                        method.huntBells,
                                    ),
                                ruleoffsEvery = fullCall.ruleoffsEvery,
                                ruleoffsFrom = fullCall.rowRuleoffsFrom,
                                textColor = textColor,
                            )

                        startX += rowSize.width + 8.dp.toPx()
                    }
            }
        }
    }
}

@Composable
private fun BlueLineGrid(
    method: MethodWithCalls,
    modifier: Modifier = Modifier,
) {
    val measurer = rememberTextMeasurer(method.stage)
    val leads = method.leads
    val l = leads[0].lead
    val places = List(method.stage) { it + 1 }.filter { it !in method.huntBells }
    BoxWithConstraints(modifier = modifier.horizontalScroll(rememberScrollState())) {
        with(LocalDensity.current) {
            val maxHeight = constraints.maxHeight - 8.dp.toPx()
            val style =
                calculateBlueLineStyle(
                    measurer,
                    maxHeight,
                    l.size,
                )

            val results =
                (1..method.stage).map { bell ->
                    measurer.measure(
                        bell.toBellChar(),
                        style = style,
                    )
                }

            val placeNotations = method.fullNotation.notation
                .map { measurer.measure(it.replace('-', 'x'), style = style) }
            val placeNotationWidth = placeNotations.maxOf { it.size.width }

            val totalHeight = results[0].size.height * l.size

            val spacing = results[0].size.height.toFloat()
            val startY = (constraints.maxHeight - totalHeight).div(2f).coerceAtMost(4.dp.toPx())
            val totalWidth = spacing * method.stage

            val callIndexes = method.callIndexes(false)
            val callColumns = callIndexes.map { it.value.size }.sum()

            val width =
                totalWidth +
                    totalWidth * callColumns +
                    placeNotationWidth + 2.dp.toPx() +
                    16.dp.toPx() * 2 +
                    8.dp.toPx() * (callColumns)

            val textColor = LocalContentColor.current
            Canvas(modifier = Modifier.width(width.toDp()).fillMaxHeight()) {
                var startX = 16.dp.toPx()

                placeNotations.forEachIndexed { index, layout ->
                    drawText(
                        layout,
                        color = Color.Black,
                        topLeft = Offset(
                            x = startX + placeNotationWidth - layout.size.width,
                            y = startY + spacing * (index + 0.5f),
                        ),
                    )
                }

                startX += placeNotationWidth + 2.dp.toPx()

                val leadLead = leads.first()

                val rowSize =
                    drawRows(
                        rows = leadLead.lead,
                        placeCharResults = results,
                        topLeft = Offset(x = startX, y = startY),
                        blueLines = blueLineDetails(
                            places,
                            method.huntBells,
                        ),
                        ruleoffsEvery = method.ruleoffsEvery,
                        ruleoffsFrom = method.ruleoffsFrom,
                        textColor = textColor,
                    )

                startX += rowSize.width + 8.dp.toPx()

                callIndexes.flatMap { (idx, calls) -> calls.map { idx to it } }
                    .forEach { (idx, fullCall) ->
                        val title = if (callIndexes.size == 1) {
                            "${fullCall.name}:"
                        } else {
                            "${fullCall.name} @ $idx:"
                        }
                        val nameResult = measurer.measure(title, style = style)
                        drawText(
                            nameResult,
                            color = textColor,
                            topLeft = Offset(
                                x = startX + (spacing - results[0].size.width) / 2,
                                y = startY,
                            ),
                        )

                        val yOffset = startY + spacing + 4.dp.toPx()

                        val callRowSize = drawRows(
                            rows = fullCall.rows,
                            placeCharResults = results,
                            topLeft = Offset(x = startX, y = yOffset),
                            blueLines =
                                blueLineDetails(
                                    fullCall.affectedBells,
                                    method.huntBells,
                                ),
                            ruleoffsEvery = fullCall.ruleoffsEvery,
                            ruleoffsFrom = fullCall.rowRuleoffsFrom,
                            textColor = textColor,
                        )

                        startX += callRowSize.width + 8.dp.toPx()
                    }
            }
        }
    }
}

private sealed class BlueLineUiModel
private data class BlueLineMethodsModel(
    val methods: List<MethodWithCalls>,
    val selectedMethod: MethodWithCalls,
) : BlueLineUiModel()

private data class SingleBlueLineModel(
    val method: MethodWithCalls,
) : BlueLineUiModel()

private data object BlueLineEmptyModel : BlueLineUiModel()

private class BlueLineController(
    private val method: String? = null,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val methodRepository: MethodRepository = MethodRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow<BlueLineUiModel?>(null)
    val uiState = _uiState.asStateFlow()

    init {
        println("Creating BlueLineController")
        if (method == null) {
            methodRepository.observeSelectedMethods()
                .map { methods ->
                    val selectedMethod = methods.firstOrNull { it.enabledForBlueline } ?: methods.firstOrNull()
                    if (selectedMethod == null) {
                        BlueLineEmptyModel
                    } else {
                        BlueLineMethodsModel(
                            methods = methods,
                            selectedMethod = selectedMethod,
                        )
                    }
                }
                .onEach { _uiState.value = it }
                .launchIn(viewModelScope + defaultDispatcher)
        } else {
            methodRepository.observeMethod(method)
                .map { method ->
                    if (method == null) {
                        BlueLineEmptyModel
                    } else {
                        SingleBlueLineModel(method = method)
                    }
                }
                .onEach { _uiState.value = it }
                .launchIn(viewModelScope + defaultDispatcher)
        }
    }

    object Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            // This needs to match the name of the field in the nav graph
            val methodName = extras[DEFAULT_ARGS_KEY]?.getString("methodName")
            return BlueLineController(methodName) as T
        }
    }
}
