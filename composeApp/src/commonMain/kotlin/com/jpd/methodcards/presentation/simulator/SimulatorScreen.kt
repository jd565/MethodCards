package com.jpd.methodcards.presentation.simulator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathSegment
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jpd.methodcards.data.MethodCardsPreferences
import com.jpd.methodcards.data.MethodRepository
import com.jpd.methodcards.di.MethodCardDi.getMethodCardsPreferences
import com.jpd.methodcards.domain.CallFrequency
import com.jpd.methodcards.domain.ExtraPathType
import com.jpd.methodcards.domain.MethodWithCalls
import com.jpd.methodcards.domain.PersistedSimulatorState
import com.jpd.methodcards.domain.toBellChar
import com.jpd.methodcards.presentation.KeyDirection
import com.jpd.methodcards.presentation.KeyEvent
import com.jpd.methodcards.presentation.LocalKeyEvents
import com.jpd.methodcards.presentation.NoMethodSelectedView
import com.jpd.methodcards.presentation.blueline.BlueLineColors
import com.jpd.methodcards.presentation.blueline.BlueLineStroke
import com.jpd.methodcards.presentation.blueline.TrebleLineColor
import com.jpd.methodcards.presentation.blueline.TrebleLineStroke
import com.jpd.methodcards.presentation.icons.South
import com.jpd.methodcards.presentation.icons.SouthEast
import com.jpd.methodcards.presentation.icons.SouthWest
import com.jpd.methodcards.presentation.ui.MultiMethodTopBar
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min

private const val SimulatorExplainer = "On this screen you can select any number of methods from those currently " +
    "enabled, and then practise ringing it.\n\n" +
    "You are ringing the blue line of the method, and the buttons at the bottom of the screen move the blue line " +
    "up, down, or make it stay in the same place. If you press the correct button the blue line will progress, " +
    "otherwise it will stay in the same place."

expect val SHOW_KEY_HINT: Boolean

@Composable
fun SimulatorScreen(
    modifier: Modifier = Modifier,
    showSimulatorBottomSheet: () -> Unit,
    navigateToAppSettings: () -> Unit,
    navigateToMultiMethodSelection: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(scope) { SimulatorController(scope) }
    DisposableEffect(controller) {
        onDispose {
            controller.onCleared()
        }
    }
    val model = controller.uiState.collectAsState().value

    if (model != null) {
        SimulatorView(
            model = model,
            modifier = modifier,
            settingsClicked = showSimulatorBottomSheet,
            addMethodClicked = navigateToAppSettings,
            navigateToMultiMethodSelection = navigateToMultiMethodSelection,
        )
    } else {
        Box(modifier)
    }
}

@Composable
private fun SimulatorView(
    model: SimulatorUiModel,
    modifier: Modifier,
    settingsClicked: () -> Unit,
    addMethodClicked: () -> Unit,
    navigateToMultiMethodSelection: () -> Unit,
) {
    var showExplainer by remember { mutableStateOf(false) }
    var showFullStats by remember { mutableStateOf(false) }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        MultiMethodTopBar(
            (model as? SimulatorMethodsModel)?.selectionDescription,
            explainerClicked = { showExplainer = true },
            settingsClicked = settingsClicked,
            navigateToMultiMethodSelection = navigateToMultiMethodSelection,
        )
        if (model is SimulatorMethodsModel) {
            SimulatorLineAndInputs(model.state, seeFullStats = { showFullStats = true })
        } else {
            NoMethodSelectedView(modifier = Modifier.weight(1f), addMethodClicked = addMethodClicked)
        }
    }

    if (showExplainer) {
        AlertDialog(
            onDismissRequest = { showExplainer = false },
            confirmButton = {},
            title = {
                Text("Simulator Screen")
            },
            text = {
                Text(SimulatorExplainer)
            },
        )
    }
    if (showFullStats) {
        AlertDialog(
            onDismissRequest = { showFullStats = false },
            confirmButton = {},
            title = {
                Text("Simulator Session Stats")
            },
            text = {
                (model as? SimulatorMethodsModel)?.state?.placeMethodCounts?.let { counts ->
                    val methods = mutableMapOf<String, Pair<Int, Int>>()
                    counts.forEach { (_, count) ->
                        count.forEach { (method, c) ->
                            val current = methods.getOrElse(method.name) { 0 to 0 }
                            methods[method.name] = Pair(current.first + c.first, current.second + c.second)
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        verticalArrangement = spacedBy(2.dp),
                        horizontalArrangement = spacedBy(4.dp),
                    ) {
                        item(
                            span = { GridItemSpan(3) },
                        ) { StatsText("Method") }
                        item { StatsText("Total") }
                        item { StatsText("Errors") }
                        methods.forEach { (method, c) ->
                            item(span = { GridItemSpan(3) }) { Text(method) }
                            item { StatsText(c.first.toString()) }
                            item { StatsText(c.second.toString()) }
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun StatsText(text: String) {
    Text(text, modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
}

@Composable
private fun RowCounts(state: SimulatorState, seeFullStats: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text("Rows: ${state.rowCount}")
        Text("Errors: ${state.errorCount}")
        Button(onClick = seeFullStats) {
            Text("Full Stats")
        }
    }
}

@Composable
private fun SimulatorLineAndInputs(state: SimulatorState?, seeFullStats: () -> Unit) {
    val events = LocalKeyEvents.current
    val keyState by rememberUpdatedState(state)
    val eventCallback = remember(state?.handbellMode) { createKeyEventCallback(state) { keyState } }
    DisposableEffect(eventCallback, events) {
        events.add(eventCallback)
        onDispose { events.remove(eventCallback) }
    }
    if (state?.handbellMode == true) {
        Box {
            SimulatorLine(
                state,
                seeFullStats,
                modifier = Modifier.fillMaxSize(),
            )
            Row(modifier = Modifier.padding(8.dp).align(Alignment.BottomStart)) {
                Column(verticalArrangement = spacedBy(8.dp)) {
                    DirectionArrow(
                        direction = KeyDirection.D,
                        modifier = Modifier.size(96.dp),
                        icon = Icons.Filled.SouthEast,
                        eventCallback = eventCallback,
                        keyHint = "d",
                        tint = BlueLineColors[0],
                    )
                    DirectionArrow(
                        direction = KeyDirection.S,
                        modifier = Modifier.size(96.dp),
                        icon = Icons.Filled.South,
                        eventCallback = eventCallback,
                        keyHint = "s",
                        tint = BlueLineColors[0],
                    )
                    DirectionArrow(
                        direction = KeyDirection.A,
                        modifier = Modifier.size(96.dp),
                        icon = Icons.Filled.SouthWest,
                        eventCallback = eventCallback,
                        keyHint = "a",
                        tint = BlueLineColors[0],
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(verticalArrangement = spacedBy(8.dp)) {
                    DirectionArrow(
                        direction = KeyDirection.Right,
                        modifier = Modifier.size(96.dp),
                        icon = Icons.Filled.SouthEast,
                        eventCallback = eventCallback,
                        tint = BlueLineColors[1],
                        keyHint = "j",
                    )
                    DirectionArrow(
                        direction = KeyDirection.Down,
                        modifier = Modifier.size(96.dp),
                        icon = Icons.Filled.South,
                        eventCallback = eventCallback,
                        tint = BlueLineColors[1],
                        keyHint = "k",
                    )
                    DirectionArrow(
                        direction = KeyDirection.Left,
                        modifier = Modifier.size(96.dp),
                        icon = Icons.Filled.SouthWest,
                        eventCallback = eventCallback,
                        tint = BlueLineColors[1],
                        keyHint = "l",
                    )
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            SimulatorLine(
                state,
                seeFullStats,
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
            Row(modifier = Modifier.padding(8.dp), horizontalArrangement = spacedBy(8.dp)) {
                DirectionArrow(
                    direction = KeyDirection.Left,
                    modifier = Modifier.weight(1f).heightIn(96.dp),
                    icon = Icons.Filled.SouthWest,
                    eventCallback = eventCallback,
                )
                DirectionArrow(
                    direction = KeyDirection.Down,
                    modifier = Modifier.weight(1f).heightIn(96.dp),
                    icon = Icons.Filled.South,
                    eventCallback = eventCallback,
                )
                DirectionArrow(
                    direction = KeyDirection.Right,
                    modifier = Modifier.weight(1f).heightIn(96.dp),
                    icon = Icons.Filled.SouthEast,
                    eventCallback = eventCallback,
                )
            }
        }
    }
}

@Composable
private fun SimulatorLine(
    state: SimulatorState?,
    seeFullStats: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        if (state != null) {
            RowCounts(state, seeFullStats, modifier = Modifier.padding(start = 24.dp))
            Column(
                modifier = Modifier.padding(start = 24.dp).align(Alignment.BottomStart),
                verticalArrangement = spacedBy(8.dp),
            ) {
                if (state.callFrequency == CallFrequency.Manual) {
                    state.calls.forEach { call ->
                        val selected = state.nextCall == call
                        val color =
                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background
                        val border =
                            if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                        Surface(
                            onClick = { state.makeCall(call) },
                            border = BorderStroke(1.dp, border),
                            color = color,
                            shape = MaterialTheme.shapes.medium,
                            content = {
                                Box(
                                    modifier = Modifier.heightIn(48.dp).padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(call.name)
                                }
                            },
                        )
                    }
                }
            }
            val measurer = rememberTextMeasurer(state.stage)
            val nameMeasurer = rememberTextMeasurer()
            val style =
                MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                )
            val callStyle = MaterialTheme.typography.labelSmall
            val results =
                (1..state.stage).map { bell ->
                    measurer.measure(
                        bell.toBellChar(),
                        style = style,
                    )
                }
            val lineColor = MaterialTheme.colorScheme.onBackground
            Canvas(modifier = Modifier.fillMaxSize()) {
                val spacing = (size.width / (results.size + 1)).coerceAtMost(12.sp.toPx())
                val totalWidth = spacing * (results.size + 1)

                var startOffset = (size.width - totalWidth) / 2 + spacing
                val methodStage = state.method.stage
                results.forEachIndexed { index, textLayoutResult ->
                    val alpha = if (index < methodStage) 1f else 0.5f
                    drawText(
                        textLayoutResult,
                        topLeft =
                        Offset(
                            startOffset - textLayoutResult.size.width / 2,
                            size.height - textLayoutResult.size.height,
                        ),
                        color = lineColor,
                        alpha = alpha,
                    )
                    drawLine(
                        start = Offset(
                            startOffset - 1.dp.toPx() / 2f,
                            size.height - textLayoutResult.size.height,
                        ),
                        end = Offset(startOffset - 1.dp.toPx() / 2f, 0f),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect =
                        PathEffect.dashPathEffect(
                            floatArrayOf(3 * spacing / 5, 2 * spacing / 5),
                            4 * spacing / 5,
                        ),
                        color = lineColor,
                        alpha = alpha * 0.3f,
                    )
                    startOffset += spacing
                }
            }

            Canvas(modifier = Modifier.fillMaxSize().clipToBounds()) {
                val spacing = (size.width / (results.size + 1)).coerceAtMost(12.sp.toPx())
                val totalWidth = spacing * (results.size + 1)
                val xOffset = (size.width - totalWidth) / 2 + spacing

                var lastY = size.height - results.first().size.height + spacing / 2
                var rowIdx = state.rows.lastIndex
                var callTextTop = Float.MAX_VALUE

                val path = ExtraBellPath(ExtraPathType.Full) { placeIndex }
                val path2 = if (state.handbellMode) ExtraBellPath(ExtraPathType.Full) { place2Index } else null
                val tPath = ExtraBellPath(
                    state.showTrebleLine,
                    crossingPlaceCount = if (state.handbellMode) 2 else 1,
                    crossingPlaceSelector = if (state.handbellMode) {
                        { it[0] = placeIndex; it[1] = place2Index }
                    } else {
                        { it[0] = placeIndex }
                    },
                ) { trebleIndex }
                val c1Path = ExtraBellPath(state.showCourseBell) { courseBell1Index }
                val c2Path = ExtraBellPath(state.showCourseBell) { courseBell2Index }

                while (rowIdx >= 0 && lastY > 0) {
                    val row = state.rows[rowIdx]
                    val y = lastY - spacing

                    path.update(row, xOffset, spacing, y, rowIdx == state.rows.lastIndex)
                    path2?.update(row, xOffset, spacing, y, rowIdx == state.rows.lastIndex)
                    tPath.update(row, xOffset, spacing, y, rowIdx == state.rows.lastIndex)
                    c1Path.update(row, xOffset, spacing, y, rowIdx == state.rows.lastIndex)
                    c2Path.update(row, xOffset, spacing, y, rowIdx == state.rows.lastIndex)

                    if (row.isLeadEnd) {
                        drawLine(
                            Color.Black,
                            start = Offset(xOffset - spacing / 2, y - spacing / 2),
                            end =
                            Offset(
                                (size.width + totalWidth - spacing) / 2,
                                y - spacing / 2,
                            ),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect =
                            PathEffect.dashPathEffect(
                                floatArrayOf(
                                    3.dp.toPx(),
                                    (1.5).dp.toPx(),
                                ),
                            ),
                        )
                    }

                    row.call?.let { call ->
                        val textToDraw =
                            if (state.showLeadEndNotation && row.leadEndNotation != null) {
                                "$call (${row.leadEndNotation})"
                            } else {
                                call
                            }
                        val width = (size.width - totalWidth) / 2f
                        val callResult =
                            nameMeasurer.measure(
                                text = textToDraw,
                                style = callStyle,
                                constraints = Constraints(maxWidth = width.toInt()),
                            )
                        val maxY = min(callTextTop - callResult.size.height, size.height - callResult.size.height)
                        callTextTop = (y - callResult.size.height / 2).coerceAtMost(maxY)
                        drawText(
                            callResult,
                            color = Color.Black,
                            topLeft =
                            Offset((size.width + totalWidth) / 2, callTextTop),
                        )
                    }

                    lastY = y
                    rowIdx--
                }

                with(c1Path) { drawPath(BlueLineColors[2], TrebleLineStroke) }
                with(c2Path) { drawPath(BlueLineColors[3], TrebleLineStroke) }
                with(tPath) { drawPath(TrebleLineColor, TrebleLineStroke) }
                if (path2 != null) {
                    with(path2) { drawPath(BlueLineColors[1], BlueLineStroke) }
                }
                with(path) { drawPath(BlueLineColors[0], BlueLineStroke) }
            }
        }
    }
}

@Composable
private fun DirectionArrow(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    eventCallback: (KeyDirection, KeyEvent) -> Boolean,
    direction: KeyDirection,
    tint: Color = MaterialTheme.colorScheme.onBackground,
    keyHint: String = "",
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground), MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.background, shape = MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .indication(interactionSource, LocalIndication.current)
            .hoverable(interactionSource)
            .pointerInput(Unit) {
                val currentContext = currentCoroutineContext()
                awaitPointerEventScope {
                    while (currentContext.isActive) {
                        val change = awaitFirstDown()
                        val press = PressInteraction.Press(change.position)
                        interactionSource.tryEmit(press)
                        eventCallback(direction, KeyEvent.Down)
                        change.consume()
                        waitForUpOrCancellation()
                        interactionSource.tryEmit(PressInteraction.Release(press))
                        eventCallback(direction, KeyEvent.Up)
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (SHOW_KEY_HINT && keyHint.isNotEmpty()) {
            Text(keyHint, modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp))
        }
        Icon(
            icon,
            tint = tint,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )
    }
}

private fun createKeyEventCallback(
    state: SimulatorState?,
    keyState: () -> SimulatorState?
): (KeyDirection, KeyEvent) -> Boolean {
    val downkeys = mutableSetOf<KeyDirection>()
    return if (state?.handbellMode == true) {
        { direction, event ->
            if (event == KeyEvent.Up) {
                downkeys.remove(direction)
            } else if (event == KeyEvent.Down) {
                downkeys.add(direction)
            }
            if (downkeys.size == 2) {
                var bell1: Int? = null
                var bell2: Int? = null
                downkeys.forEach { dir ->
                    when (dir) {
                        KeyDirection.S -> bell1 = 0
                        KeyDirection.A -> bell1 = -1
                        KeyDirection.D -> bell1 = 1
                        KeyDirection.Down -> bell2 = 0
                        KeyDirection.Left -> bell2 = -1
                        KeyDirection.Right -> bell2 = 1
                    }
                }
                if (bell1 != null && bell2 != null) {
                    keyState()?.move(bell1!!, bell2)
                }
            }
            true
        }
    } else {
        { direction, event ->
            if (event == KeyEvent.Down) {
                when (direction) {
                    KeyDirection.Down, KeyDirection.S -> keyState()?.move(0, null)
                    KeyDirection.Left, KeyDirection.A -> keyState()?.move(-1, null)
                    KeyDirection.Right, KeyDirection.D -> keyState()?.move(1, null)
                }
            }
            true
        }
    }
}

private class ExtraBellPath(
    private val type: ExtraPathType,
    crossingPlaceCount: Int = 1,
    private val crossingPlaceSelector: RowInformation.(IntArray) -> Unit = { it[0] = placeIndex },
    private val selector: RowInformation.() -> Int,
) {
    private val path = Path()
    private var active = false
    private var initialPosition: Offset = Offset(-1f, -1f)
    private val crossingPlaceArray = IntArray(crossingPlaceCount)

    fun update(row: RowInformation, xOffset: Float, spacing: Float, y: Float, isLast: Boolean) {
        val idx = row.selector()
        if (idx < 0) return
        val x = xOffset + idx * spacing

        if (isLast) {
            if (type == ExtraPathType.Full) {
                initialPosition = Offset(x, y)
                path.moveTo(x, y)
            } else if (type == ExtraPathType.Crossing) {
                if (idx.doesCross(row)) {
                    initialPosition = Offset(x, y)
                    active = true
                    path.moveTo(x, y)
                }
            }
        } else {
            if (type == ExtraPathType.Full) {
                path.lineTo(x, y)
            } else if (type == ExtraPathType.Crossing) {
                if (idx.doesCross(row)) {
                    if (active) {
                        path.lineTo(x, y)
                    } else {
                        path.moveTo(x, y)
                        active = true
                    }
                } else {
                    active = false
                }
            }
        }
    }

    private fun Int.doesCross(row: RowInformation): Boolean {
        crossingPlaceSelector(row, crossingPlaceArray)
        return crossingPlaceArray.any { abs(this - it) == 1 }
    }

    fun DrawScope.drawPath(color: Color, style: Stroke) {
        var lastPoint: Offset = initialPosition
        fun drawPoint() {
            if (lastPoint.x >= 0f) {
                drawCircle(
                    color,
                    radius = if (style == BlueLineStroke) style.width else style.width / 2f,
                    center = Offset(lastPoint.x, lastPoint.y),
                )
            }
        }

        drawPoint()

        path.iterator().forEach { segment ->
            when (segment.type) {
                PathSegment.Type.Move -> {
                    drawPoint()
                    lastPoint = Offset(segment.points[0], segment.points[1])
                    drawPoint()
                }

                PathSegment.Type.Line -> lastPoint = Offset(segment.points[2], segment.points[3])
                PathSegment.Type.Done -> drawPoint()
                else -> Unit
            }
        }
        drawPath(
            path = path,
            color = color,
            style = style,
        )
    }
}

private sealed class SimulatorUiModel
private data class SimulatorMethodsModel(
    val selectionDescription: String,
    val state: SimulatorState,
) : SimulatorUiModel()

private data object SimulatorEmptyModel : SimulatorUiModel()

@OptIn(ExperimentalCoroutinesApi::class)
private class SimulatorController(
    private val scope: CoroutineScope,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val methodRepository: MethodRepository = MethodRepository(),
    methodCardsPreferences: MethodCardsPreferences = getMethodCardsPreferences(),
) {
    private val _uiState = MutableStateFlow<SimulatorUiModel?>(null)
    val uiState = _uiState.asStateFlow()

    init {
        scope.launch(defaultDispatcher) {
            val persistModel = methodRepository.getSimulatorModel()
            combine(
                methodRepository.observeSelectedMethods(),
                methodCardsPreferences.observeSimulatorUse4thsPlaceCalls(),
                methodCardsPreferences.observeSimulatorHandbellMode(),
            ) { methods, use4thsPlaceCalls, handbellMode ->
                if (methods.isEmpty()) {
                    SimulatorEmptyModel
                } else {
                    val selectedMethods =
                        methods.filter { it.enabledForMultiMethod }.takeIf { it.isNotEmpty() } ?: methods
                    val selectedName =
                        when {
                            selectedMethods.size == 1 -> selectedMethods.first().shortName(methods)
                            selectedMethods.size < methods.size -> "Some methods (${selectedMethods.size})"
                            methods.isNotEmpty() -> "All methods (${methods.size})"
                            else -> ""
                        }

                    val simulatorState = if (persistModel != null &&
                        persistModel.methodNames.size == selectedMethods.size &&
                        persistModel.use4thsPlaceCalls == use4thsPlaceCalls &&
                        persistModel.handbellMode == handbellMode &&
                        persistModel.methodNames.all { method -> selectedMethods.find { it.name == method } != null }
                    ) {
                        SimulatorState(selectedMethods, persistModel, ::updateMethodStatistics, ::persistModel)
                    } else {
                        SimulatorState(
                            selectedMethods,
                            ::updateMethodStatistics,
                            ::persistModel,
                            use4thsPlaceCalls,
                            handbellMode,
                        )
                    }

                    SimulatorMethodsModel(
                        selectionDescription = selectedName,
                        state = simulatorState,
                    )
                }
            }
                .transformLatest { model ->
                    coroutineScope {
                        if (model is SimulatorMethodsModel) {
                            methodCardsPreferences.observeSimulatorShowTreble()
                                .onEach { showTreble -> model.state.updateShowTreble(showTreble) }
                                .launchIn(this)

                            methodCardsPreferences.observeSimulatorShowCourseBell()
                                .onEach { model.state.updateShowCourseBell(it) }
                                .launchIn(this)

                            methodCardsPreferences.observeSimulatorCallFrequency()
                                .onEach { callFrequency -> model.state.updateCallFrequency(callFrequency) }
                                .launchIn(this)

                            methodCardsPreferences.observeSimulatorShowLeadEndNotation()
                                .onEach { show -> model.state.updateShowLeadEndNotation(show) }
                                .launchIn(this)

                            // methodCardsPreferences.observeSimulatorHalfLeadSplicing()
                            //     .onEach { model.generator.updateHalfLeadSplicing(it) }
                            //     .launchIn(this)

                            launch(defaultDispatcher) {
                                model.state.cacheMethods()
                            }
                        }

                        emit(model)
                    }
                }
                .collect { _uiState.value = it }
        }
    }

    fun onCleared() {
        (_uiState.value as? SimulatorMethodsModel)?.state?.persist()
            ?.let { persistModel(it) }
    }

    private fun persistModel(model: PersistedSimulatorState) {
        methodRepository.persistSimulatorModel(model)
    }

    private fun updateMethodStatistics(method: MethodWithCalls, lead: Int, error: Boolean) {
        scope.launch {
            methodRepository.incrementMethodStatistics(method, lead, error)
        }
    }
}
