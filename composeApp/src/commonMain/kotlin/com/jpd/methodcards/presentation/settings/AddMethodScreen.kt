package com.jpd.methodcards.presentation.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jpd.methodcards.data.MethodCardsPreferences
import com.jpd.methodcards.data.MethodRepository
import com.jpd.methodcards.di.MethodCardDi.getMethodCardsPreferences
import com.jpd.methodcards.domain.MethodClassification
import com.jpd.methodcards.domain.MethodFrequency
import com.jpd.methodcards.domain.MethodWithCalls
import com.jpd.methodcards.domain.PlaceNotation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun AddMethodScreen(
    modifier: Modifier,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(scope) {
        AddMethodController(scope)
    }
    val model = controller.uiState.collectAsState().value

    if (model != null) {
        AddMethodView(
            model = model,
            modifier = modifier,
            onBack = onBack,
            stageUpdated = remember(controller) { controller::stageUpdated },
            nameUpdated = remember(controller) { controller::nameUpdated },
            placeNotationUpdated = remember(controller) { controller::placeNotationUpdated },
            addMethodClicked = remember(controller) { controller::addMethod },
        )
    } else {
        Box(modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMethodView(
    model: AddMethodUiModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    stageUpdated: (Int) -> Unit,
    nameUpdated: (String) -> Unit,
    placeNotationUpdated: (String) -> Unit,
    addMethodClicked: (
        stage: Int, name: String, classification: MethodClassification, placeNotation: String, ruleoffsEvery: Int, ruleoffsFrom: Int,
    ) -> Unit,
) {
    Column(modifier = modifier.padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(56.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // IconButton(onClick = onBack) {
            //     Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
            // }
            Box(modifier = Modifier.weight(1f))
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        var name by remember { mutableStateOf("") }
        var placeNotation by remember { mutableStateOf("") }
        var stage by remember { mutableStateOf(model.stage) }
        var classificationState by remember { mutableStateOf(MethodClassification.None) }
        var ruleoffsEvery by remember { mutableStateOf(0) }
        var ruleoffsFrom by remember { mutableStateOf(0) }

        var stageExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = stageExpanded, onExpandedChange = { stageExpanded = it }) {
            OutlinedTextField(
                value = stage.toString(),
                {},
                label = { Text("Stage") },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = stageExpanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            )
            ExposedDropdownMenu(
                modifier = Modifier.heightIn(max = 280.dp),
                expanded = stageExpanded,
                onDismissRequest = { stageExpanded = false },
            ) {
                MethodWithCalls.AllowedStages.forEach { maybeStage ->
                    DropdownMenuItem(
                        text = { Text(maybeStage.toString()) },
                        onClick = {
                            stage = maybeStage
                            stageUpdated(stage)
                            stageExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            name, { name = it
                  nameUpdated(name)}, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(),
            isError = !model.nameError.isNullOrEmpty(), supportingText = model.nameError?.let { { Text(it) } },
        )
        Spacer(Modifier.height(4.dp))
        var classificationExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = classificationExpanded, onExpandedChange = { classificationExpanded = it }) {
            OutlinedTextField(
                value = classificationState.display,
                {},
                label = { Text("Classification") },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = classificationExpanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            )
            ExposedDropdownMenu(
                modifier = Modifier.heightIn(max = 280.dp),
                expanded = classificationExpanded,
                onDismissRequest = { classificationExpanded = false },
            ) {
                MethodClassification.entries.forEach { classification ->
                    DropdownMenuItem(
                        text = { Text(classification.display) },
                        onClick = {
                            classificationState = classification
                            classificationExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            placeNotation,
            { placeNotation = it
            placeNotationUpdated(it) },
            label = { Text("Place Notation") },
            modifier = Modifier.fillMaxWidth(),
            isError = !model.placeNotationError.isNullOrEmpty(), supportingText = model.placeNotationError?.let { { Text(it) } },
        )
        Spacer(Modifier.height(4.dp))
        Row {
            OutlinedTextField(
                ruleoffsEvery.toString(),
                { it.toIntOrNull()?.let { ruleoffsEvery = it } },
                label = { Text("Ruleoffs Every") },
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                ruleoffsFrom.toString(),
                { it.toIntOrNull()?.let { ruleoffsFrom = it } },
                label = { Text("Ruleoffs From") },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(4.dp))

        Button(
            onClick = {
                addMethodClicked(stage, name, classificationState, placeNotation, ruleoffsEvery, ruleoffsFrom)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = model.nameError == null && model.placeNotationError == null,
        ) {
            Text("Add Method")
        }
    }
}

private val MethodClassification.display: String get() = when (this) {
    MethodClassification.None -> "None"
    else -> this.part
}

data class AddMethodUiModel(
    val stage: Int,
    val nameError: String?,
    val placeNotationError: String?,
)

class AddMethodController(
    private val scope: CoroutineScope,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val methodRepository: MethodRepository = MethodRepository(),
    private val preferences: MethodCardsPreferences = getMethodCardsPreferences(),
) {
    private val _uiState = MutableStateFlow<AddMethodUiModel?>(null)
    val uiState: StateFlow<AddMethodUiModel?> = _uiState.asStateFlow()

    private val stageFlow = MutableStateFlow(-1)
    private val nameFlow = MutableStateFlow("")
    private val placeNotation = MutableStateFlow("")

    init {
        scope.launch(defaultDispatcher) {
            stageFlow.value = preferences.observeStage().first()

            val nameError = combine(stageFlow, nameFlow) { stage, name ->
                getNameError(name, stage)
            }

            val placeNotationError = combine(stageFlow, placeNotation) { stage, placeNotation ->
                getPlaceNotationError(placeNotation, stage)
            }

            combine(stageFlow, nameError, placeNotationError) { a, b, c ->
                AddMethodUiModel(a, b, c)
            }.collect {
                _uiState.value = it
            }
        }
    }

    private suspend fun getNameError(name: String, stage: Int): String? {
        val stageName = MethodWithCalls.stageName(stage)
        return if (name.isNotEmpty() && !name.endsWith(stageName)) {
            "Name should end with $stageName"
        } else {
            methodRepository.observeMethod(name).first()?.let {
                "Method with name $name already exists"
            }
        }
    }

    private suspend fun getPlaceNotationError(placeNotation: String, stage: Int): String? {
        val validChars = PlaceNotation.placeNotationCharacters(stage)
        val matcher = Regex("^$validChars+$")
        if (!matcher.matches(placeNotation)) {
            return "Invalid place notation"
        }

        val pn = PlaceNotation(placeNotation)
        try {
            pn.asString()
        } catch (e: Exception) {
            return "Invalid place notation"
        }
        methodRepository.searchByPlaceNotations(listOf(pn)).firstOrNull()?.let { existing ->
            if (existing.stage == stage)
                return "Method with notation already exists (${existing.name})"
        }
        return null
    }

    fun stageUpdated(stage: Int) {
        stageFlow.value = stage
    }

    fun nameUpdated(name: String) {
        nameFlow.value = name
    }

    fun placeNotationUpdated(pn: String) {
        placeNotation.value = pn
    }

    fun addMethod(
        stage: Int,
        name: String,
        classification: MethodClassification,
        placeNotation: String,
        ruleoffsEvery: Int,
        ruleoffsFrom: Int,
    ) {
        scope.launch {
            val method = MethodWithCalls(
                name,
                PlaceNotation(placeNotation),
                stage,
                ruleoffsEvery,
                ruleoffsFrom,
                classification,
                emptyList(),
                false,
                MethodFrequency.Regular,
                false,
            )
            methodRepository.addMethod(method)
        }
    }
}
