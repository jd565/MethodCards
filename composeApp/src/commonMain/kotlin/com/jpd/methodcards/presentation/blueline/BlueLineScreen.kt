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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
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
import com.jpd.methodcards.data.MethodRepository
import com.jpd.methodcards.domain.MethodWithCalls
import com.jpd.methodcards.domain.toBellChar
import com.jpd.methodcards.presentation.NoMethodSelectedView
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

private const val BlueLineExplainer = "On this screen you can select an individual method from the enabled methods " +
    "to display the whole blue line for this method.\n\n" +
    "You can use the settings to change the style of the blue line used."

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun BlueLineScreen(
    modifier: Modifier = Modifier,
    showBlueLineBottomSheet: () -> Unit,
    navigateToAppSettings: () -> Unit,
    navigateBack: () -> Unit,
    method: String? = null,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(scope, method) { BlueLineController(scope, method) }
    val model = controller.uiState.collectAsState().value

    if (model != null) {
        val navigator = rememberSupportingPaneScaffoldNavigator()
        // BackHandler(navigator.canNavigateBack()) {
        //     navigator.navigateBack()
        // }

        SupportingPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            mainPane = {
                AnimatedPane {
                    BlueLineView(
                        model = model,
                        modifier = modifier,
                        selectMethod = { navigator.navigateTo(ThreePaneScaffoldRole.Secondary) },
                        settingsClicked = showBlueLineBottomSheet,
                        addMethodClicked = navigateToAppSettings,
                        onClose = navigateBack,
                        canSelectMethod = navigator.scaffoldValue.secondary == PaneAdaptedValue.Hidden,
                    )
                }
            },
            supportingPane = {
                AnimatedPane {
                    BlueLineMethodList(
                        model = model,
                        selectMethod = remember(controller, navigator) {
                            {
                                controller.selectMethod(it)
                                navigator.navigateBack()
                            }
                                                                       },
                    )
                }
            },
        )
    } else {
        Box(modifier)
    }
}

@Composable
private fun BlueLineView(
    model: BlueLineUiModel,
    modifier: Modifier,
    selectMethod: () -> Unit,
    settingsClicked: () -> Unit,
    addMethodClicked: () -> Unit,
    onClose: () -> Unit,
    canSelectMethod: Boolean,
) {

    var showExplainer by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        BlueLineTopRow(
            model = model,
            explainerClicked = { showExplainer = true },
            settingsClicked = settingsClicked,
            onClose = onClose,
            selectMethod = selectMethod,
            canSelectMethod = canSelectMethod,
        )
        if (model is BlueLineMethodsModel) {
            BlueLinePager(model.selectedMethod, modifier = Modifier.weight(1f))
        } else if (model is SingleBlueLineModel) {
            BlueLinePager(model.method, modifier = Modifier.weight(1f))
        } else {
            NoMethodSelectedView(
                modifier = Modifier.weight(1f),
                addMethodClicked = addMethodClicked,
            )
        }
    }

    if (showExplainer) {
        AlertDialog(
            onDismissRequest = { showExplainer = false },
            confirmButton = {},
            title = {
                Text("BlueLine Screen")
            },
            text = {
                Text(BlueLineExplainer)
            },
        )
    }
}

@Composable
private fun BlueLineMethodList(
    model: BlueLineUiModel,
    selectMethod: (String) -> Unit,
) {
    if (model is BlueLineMethodsModel) {
        Column {
            model.methods.forEach { method ->
                Box(
                    modifier =
                    Modifier
                        .heightIn(32.dp)
                        .fillMaxWidth()
                        .clickable {
                            selectMethod(method.name)
                        }.padding(horizontal = 40.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(method.name)
                }
            }
        }
    }
}

@Composable
private fun BlueLineTopRow(
    model: BlueLineUiModel,
    explainerClicked: () -> Unit,
    settingsClicked: () -> Unit,
    selectMethod: () -> Unit,
    onClose: () -> Unit,
    canSelectMethod: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(56.dp).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (model !is SingleBlueLineModel) {
            IconButton(onClick = explainerClicked) {
                Icon(Icons.Outlined.Info, contentDescription = "Blue Line Explainer")
            }
        } else {
            IconButton(onClick = onClose) {
                Icon(Icons.Outlined.Close, contentDescription = "Close")
            }
        }
        when (model) {
            is BlueLineMethodsModel -> {
                val modifier = if (canSelectMethod) {
                    Modifier.clickable(onClick = selectMethod)
                } else {
                    Modifier
                }
                Box(
                    modifier = Modifier.weight(1f).then(modifier),
                ) {
                    Row(
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(48.dp)
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(model.selectedMethod.name)
                        if (canSelectMethod) {
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Default.KeyboardArrowRight, contentDescription = null)
                        }
                    }
                }
            }

            is SingleBlueLineModel -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(model.method.name)
                }
            }

            else -> {
                Box(modifier = Modifier.weight(1f))
            }
        }
        IconButton(onClick = settingsClicked) {
            Icon(Icons.Outlined.Settings, contentDescription = "Blue Line Settings")
        }
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

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
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
                            topLeft =
                            Offset(
                                x = startX + rowSize.width + 4.dp.toPx(),
                                y = startY,
                            ),
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
                            topLeft =
                            Offset(
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
                            color = Color.Black,
                            topLeft =
                            Offset(
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
    private val scope: CoroutineScope,
    private val method: String? = null,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val methodRepository: MethodRepository = MethodRepository(),
) {
    private val _uiState = MutableStateFlow<BlueLineUiModel?>(null)
    val uiState = _uiState.asStateFlow()

    init {
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
                .launchIn(scope + defaultDispatcher)
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
                .launchIn(scope + defaultDispatcher)
        }
    }

    fun selectMethod(method: String) {
        scope.launch {
            methodRepository.setBlueLineMethod(method)
        }
    }
}
