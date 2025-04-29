package com.jpd.methodcards.presentation.methodbuilder

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jpd.methodcards.domain.MethodWithCalls
import com.jpd.methodcards.domain.Row.Companion.rounds
import com.jpd.methodcards.domain.toBellChar
import com.jpd.methodcards.presentation.KeyDirection
import com.jpd.methodcards.presentation.KeyEvent
import com.jpd.methodcards.presentation.LocalKeyEvents
import com.jpd.methodcards.presentation.methodbuilder.MethodBuilderViewModel.MethodBuilderEvent
import com.jpd.methodcards.presentation.methodbuilder.MethodBuilderViewModel.MethodBuilderEvent.GridCellSelected
import com.jpd.methodcards.presentation.methodbuilder.MethodBuilderViewModel.MethodBuilderEvent.NotationCellSelected
import kotlin.math.max

@Composable
fun MethodBuilderScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel: MethodBuilderViewModel = viewModel(factory = MethodBuilderViewModel.Factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyEvents = LocalKeyEvents.current

    DisposableEffect(keyEvents, viewModel) {
        val callback: (KeyDirection, KeyEvent) -> Boolean = { direction, event ->
            if (event == KeyEvent.Down) {
                when (direction) {
                    KeyDirection.Left -> Pair(-1, 0)
                    KeyDirection.Down -> Pair(0, 1)
                    KeyDirection.Right -> Pair(1, 0)
                    KeyDirection.Up -> Pair(0, -1)
                    else -> null
                }?.let { delta ->
                    viewModel.onEvent(MethodBuilderEvent.GridCellDelta(delta))
                }

                when (direction) {
                    KeyDirection.A -> "A"
                    KeyDirection.D -> "D"
                    is KeyDirection.Bell -> direction.bellChar
                    KeyDirection.Delete -> ""
                    else -> null
                }?.let {
                    viewModel.onEvent(MethodBuilderEvent.KeyboardEntry(it.takeIf { it.isNotEmpty() }))
                }
            }
            true
        }
        keyEvents.add(callback)
        onDispose {
            keyEvents.remove(callback)
        }
    }

    MethodBuilderView(
        state = uiState,
        modifier = modifier,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MethodBuilderView(
    state: MethodBuilderState,
    modifier: Modifier = Modifier,
    onEvent: (MethodBuilderEvent) -> Unit,
) {
    Column(modifier = modifier) {
        val scrollState = rememberScrollState()
        val density = LocalDensity.current
        LaunchedEffect(state.selectedGrid?.second, state.selectedNotation, state.grid.size, density) {
            val y = state.selectedGrid?.second ?: state.selectedNotation
            if (y != null) {
                val totalHeight = scrollState.maxValue + scrollState.viewportSize
                // Assume the rows are 32 dp high
                val rowHeight = with(density) { 32.dp.toPx() }
                val baseHeight = state.grid.size * rowHeight + with(density) { 20.dp.toPx() }
                val textFieldHeight = totalHeight - baseHeight
                val selectedRowTop = textFieldHeight + (y - 1) * rowHeight
                val selectedRowBottom = textFieldHeight + (y + 2) * rowHeight

                if (selectedRowBottom > scrollState.value + scrollState.viewportSize) {
                    scrollState.animateScrollTo(selectedRowBottom.toInt() - scrollState.viewportSize)
                } else if (selectedRowTop < scrollState.value) {
                    scrollState.animateScrollTo(selectedRowTop.toInt())
                }
            }
        }
        Column(modifier = Modifier.padding(horizontal = 20.dp).weight(1f).verticalScroll(scrollState)) {
            Spacer(modifier = Modifier.height(20.dp))
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                TextField(
                    value = "Stage: ${state.stage}",
                    {},
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    readOnly = true,
                    singleLine = true,
                )
                ExposedDropdownMenu(
                    modifier = Modifier.exposedDropdownSize(false),
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    MethodWithCalls.AllowedStages.forEach { maybeStage ->
                        DropdownMenuItem(
                            text = { Text("Stage: $maybeStage", style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                onEvent(MethodBuilderEvent.StageSelected(maybeStage))
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            MethodGrid(
                modifier = Modifier.fillMaxWidth(),
                state = state,
                onEvent = onEvent,
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
        KeyEntry(
            modifier = Modifier.fillMaxWidth(),
            state = state,
            onEvent = onEvent,
        )
    }
}

@Composable
fun MethodGrid(
    state: MethodBuilderState,
    modifier: Modifier = Modifier,
    onEvent: (MethodBuilderEvent) -> Unit,
) {
    val style = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        fontFamily = FontFamily.Monospace,
        lineHeightStyle =
            LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.Both,
            ),
    )
    val textMeasurer = rememberTextMeasurer(cacheSize = state.stage)
    val rowSize = textMeasurer.measure("1", style = style).size
    val rowDpSize: Dp = with(LocalDensity.current) {
        max(rowSize.height, rowSize.width).toDp()
            .coerceAtLeast(32.dp)
    }
    Row(modifier = modifier) {
        Column {
            state.grid.forEachIndexed { rowIdx, row ->
                Row {
                    repeat(state.stage) { colIdx ->
                        val cell = row.getOrNull(colIdx)
                        val coord = Pair(colIdx, rowIdx)
                        MethodGridCell(
                            value = cell,
                            stage = state.stage,
                            textMeasurer = textMeasurer,
                            modifier = Modifier
                                .size(rowDpSize)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) {
                                    onEvent(GridCellSelected(rowIdx, colIdx))
                                },
                            onEvent = onEvent,
                            isSelected = state.selectedGrid == coord,
                            style = style,
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.padding(top = rowDpSize.div(2))) {
            state.placeNotation.forEachIndexed { idx, notation ->
                val isSelected = state.selectedNotation == idx
                val backgroundColour = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                Box(
                    modifier = Modifier
                        .requiredHeight(rowDpSize)
                        .background(backgroundColour)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            onEvent(NotationCellSelected(idx))
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = notation,
                        style = style,
                        color = textColor,
                    )
                }
            }
        }
    }
}

@Composable
fun MethodGridCell(
    value: Int?,
    stage: Int,
    textMeasurer: TextMeasurer,
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    style: TextStyle,
    onEvent: (MethodBuilderEvent) -> Unit,
) {
    val backgroundColour = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    val result = value?.let { textMeasurer.measure(text = value.toBellChar(), style = style) }

    Canvas(modifier = modifier.background(backgroundColour)) {
        if (result != null) {
            drawText(
                result,
                color = textColor,
                topLeft = Offset((size.width - result.size.width) / 2, (size.height - result.size.height) / 2),
            )
        }
        drawPath(
            Path().apply {
                moveTo(0f, size.height.toFloat())
                lineTo(size.width.toFloat(), size.height.toFloat())
            },
            Color.Black,
            style = Stroke(width = 2.dp.toPx()),
        )
    }
}

@Composable
private fun KeyEntry(
    modifier: Modifier = Modifier,
    state: MethodBuilderState,
    onEvent: (MethodBuilderEvent) -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            rounds(state.stage).row.forEach {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(48.dp)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground), MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.background, shape = MaterialTheme.shapes.medium)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { onEvent(MethodBuilderEvent.KeyboardEntry(it.toBellChar())) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = it.toBellChar(),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(48.dp)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground), MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.background, shape = MaterialTheme.shapes.medium)
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onEvent(MethodBuilderEvent.KeyboardEntry(null)) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "\u232b",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}
