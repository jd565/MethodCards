package com.jpd.methodcards.presentation.composer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.jpd.methodcards.data.MethodRepository
import com.jpd.methodcards.domain.MethodWithCalls
import com.jpd.methodcards.domain.Row
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Composable
fun CompositionScreen(
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(scope) { CompositionController(scope) }
    when (val model = controller.uiState.collectAsState().value) {
        is CompositionUiModel -> {
            CompositionView(
                model = model,
                modifier = modifier,
                setCourseConstruction = remember(controller) { controller::setCourseConstruction },
                setStage = remember(controller) { controller::setStage },
            )
        }

        else -> Box(modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompositionView(
    model: CompositionUiModel,
    modifier: Modifier,
    setCourseConstruction: (String) -> Unit,
    setStage: (Int) -> Unit,
) {
    Column(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(56.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // IconButton(onClick = explainerClicked) {
            //     Icon(Icons.Outlined.Info, contentDescription = "Explainer")
            // }
            Box(modifier = Modifier.weight(1f))
        }

        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                TextField(
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    value = "Stage: ${model.stage}",
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    modifier = Modifier.exposedDropdownSize(false),
                    onDismissRequest = { expanded = false },
                ) {
                    MethodWithCalls.AllowedStages.forEach { stage ->
                        DropdownMenuItem(
                            text = { Text("Stage: $stage", style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                expanded = false
                                setStage(stage)
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }
        }

        if (model is CompositionUiModel.Methods) {
            var methodMapExpanded by remember { mutableStateOf(false) }
            Row(modifier = Modifier.clickable { methodMapExpanded = !methodMapExpanded }) {
                Text("Method Keys")
                if (methodMapExpanded) {
                    Icon(Icons.Filled.ArrowDropDown, modifier = Modifier.rotate(180f), contentDescription = "Collapse")
                } else {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Expand")
                }
            }
            if (methodMapExpanded) {
                model.methodMap.forEach {
                    Text("${it.first} -> ${it.second}")
                }
            }

            ProvideTextStyle(TextStyle(fontFamily = FontFamily.Monospace)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.weight(1f))
                        Text(modifier = Modifier.weight(1f), text = model.callingPositionOrder.joinToString(" "))
                        Box(Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = model.courses.joinToString("\n") { it.courseEnd.row.joinToString("") },
                        )
                        Text(
                            modifier = Modifier.weight(1f),
                            text = model.courses.joinToString("\n") { course ->
                                model.callingPositionOrder.joinToString(" ") { course.calls[it] ?: " " }
                            }
                        )
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = model.courses.joinToString("\n") { it.methodsAndCalls },
                            onValueChange = setCourseConstruction)
                    }
                }
            }
        }
    }
}

private sealed class CompositionUiModel {
    abstract val stage: Int

    data class Methods(
        override val stage: Int,
        val methodMap: List<Pair<String, String>>,
        val callingPositionOrder: List<String>,
        val courses: List<CourseInformation>,
    ) : CompositionUiModel()

    data class Empty(override val stage: Int) : CompositionUiModel()
}

private data class CourseInformation(
    val courseEnd: Row,
    val methodsAndCalls: String,
    val calls: Map<String, String>,
)

private class CompositionController(
    private val scope: CoroutineScope,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val methodRepository: MethodRepository = MethodRepository(),
) {
    private val _uiState = MutableStateFlow<CompositionUiModel?>(null)
    val uiState: StateFlow<CompositionUiModel?> = _uiState.asStateFlow()

    private val stage = MutableStateFlow(8)
    private val courseConstructions = MutableStateFlow<List<String>>(emptyList())

    init {
        scope.launch {
            combine(
                methodRepository.observeSelectedMethods(),
                stage,
            ) { allMethods, stage ->
                val methods = allMethods.filter { it.stage == stage }

                val methodShorthand = mutableMapOf<String, MethodWithCalls>()
                methods.forEach { method ->
                    var size = 0
                    do {
                        size += 1
                        val t = method.name.substring(0, size)
                    } while (t in methodShorthand)
                    methodShorthand[method.name.substring(0, size)] = method
                }

                val methodMap = methodShorthand.map { it.key to it.value.shortName(methods) }

                Triple(methodShorthand, methodMap, stage)
            }.combine(courseConstructions) { (methodShorthand, methodMap, stage), constructions ->
                if (methodMap.isEmpty()) {
                    return@combine CompositionUiModel.Empty(stage)
                }

                val callingPositions = List(stage) { it + 1 }.associateWith {
                    when (it) {
                        stage -> "H"
                        stage - 1 -> "W"
                        stage - 2 -> "M"
                        1 -> "1"
                        2 -> "I"
                        3 -> "B"
                        4 -> "F"
                        5 -> "V"
                        6 -> "X"
                        7 -> "S"
                        8 -> "E"
                        9 -> "N"
                        10 -> "0"
                        11 -> "L"
                        12 -> "T"
                        13 -> "A"
                        14 -> "B"
                        15 -> "C"
                        16 -> "D"
                        else -> error("Too many bells")
                    }
                }

                var row = Row.rounds(stage).row
                val r = "[A-Z][^A-Z]*".toRegex()
                val usedMethods = mutableListOf<MethodWithCalls>()
                val usedCallingPositions = mutableSetOf<String>()
                val courses = constructions.map { c ->
                    val calls = mutableMapOf<String, String>()
                    r.findAll(c).forEach { match ->
                        val part = match.value
                        val bob = part.endsWith(".")
                        val single = part.endsWith(",")
                        val m = part.substring(0, part.length - if (bob || single) 1 else 0)
                        val method = methodShorthand[m] ?: methodShorthand.entries.first().value
                        usedMethods.add(method)

                        val allCalls = method.callIndexes(false).values.asSequence().flatten()
                        val ce = when {
                            bob -> allCalls.firstOrNull { it.name == "Bob" }
                            single -> allCalls.firstOrNull { it.name == "Single" }
                            else -> null
                        }

                        val le = ce?.leadEnd ?: method.leadEnd.row
                        row = le.map { row[it - 1] }
                        val call = if (bob) "-" else if (single) "s" else null
                        if (call != null) {
                            val callingPosition = callingPositions[row.indexOf(stage).plus(1)]!!
                            usedCallingPositions.add(callingPosition)
                            calls[callingPosition] = call
                        }
                    }
                    CourseInformation(Row(row), c, calls)
                }
                val callingPositionOrder = buildList {
                    val method = usedMethods.firstOrNull()
                    if (method != null) {
                        val rounds = Row.rounds(stage).row
                        row = Row.rounds(stage).row
                        val le = method.leadEnd.row
                        val be = method.callIndexes(false).values.asSequence().flatten().firstOrNull { it.name == "Bob" }?.leadEnd ?: le

                        do {
                            val bobbed = be.map { row[it - 1] }
                            row = le.map { row[it - 1] }
                            val place = bobbed.indexOf(stage) + 1
                            val cp = callingPositions[place]!!
                            if (cp in usedCallingPositions) {
                                add(cp)
                            }
                        } while (row != rounds)

                        if (remove("H")) {
                            add("H")
                        }
                    }
                }
                CompositionUiModel.Methods(
                    stage = stage,
                    methodMap = methodMap,
                    courses = courses,
                    callingPositionOrder = callingPositionOrder,
                )
            }.collect { _uiState.value = it }
        }
    }

    fun setCourseConstruction(construction: String) {
        courseConstructions.update {
            construction.split("\n")
        }
    }

    fun setStage(stage: Int) {
        this.stage.value = stage
    }
}
