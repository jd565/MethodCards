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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jpd.methodcards.data.MethodCardsPreferences
import com.jpd.methodcards.data.MethodRepository
import com.jpd.methodcards.di.MethodCardDi.getMethodCardsPreferences
import com.jpd.methodcards.domain.CallDetails
import com.jpd.methodcards.domain.MethodClassification
import com.jpd.methodcards.domain.MethodFrequency
import com.jpd.methodcards.domain.MethodWithCalls
import com.jpd.methodcards.domain.PlaceNotation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

@Composable
fun AddMethodScreen(modifier: Modifier) {
    val controller: AddMethodController = viewModel(factory = AddMethodController.Factory)
    val model = controller.uiState.collectAsState().value

    if (model != null) {
        AddMethodView(
            model = model,
            modifier = modifier,
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
private fun AddMethodView(
    model: AddMethodUiModel,
    modifier: Modifier = Modifier,
    stageUpdated: (Int) -> Unit,
    nameUpdated: (String) -> Unit,
    placeNotationUpdated: (String) -> Unit,
    addMethodClicked: (NewMethodDetails) -> Unit,
) {
    Column(modifier = modifier.padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
        var name by remember { mutableStateOf("") }
        var placeNotation by remember { mutableStateOf("") }
        var stage by remember { mutableStateOf(model.stage) }
        var classificationState by remember { mutableStateOf(MethodClassification.None) }
        var ruleoffsEvery by remember { mutableStateOf("0") }
        var ruleoffsFrom by remember { mutableStateOf("0") }

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
            name,
            {
                name = it
                nameUpdated(name)
            },
            label = { Text("Name") }, modifier = Modifier.fillMaxWidth(),
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
            {
                placeNotation = it
                placeNotationUpdated(it)
            },
            label = { Text("Place Notation") },
            modifier = Modifier.fillMaxWidth(),
            isError = !model.placeNotationError.isNullOrEmpty(),
            supportingText = model.placeNotationError?.let { { Text(it) } },
        )
        Spacer(Modifier.height(4.dp))
        Row {
            OutlinedTextField(
                ruleoffsFrom,
                { ruleoffsFrom = it },
                label = { Text("Ruleoffs From") },
                isError = ruleoffsFrom.toIntOrNull() == null,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                ruleoffsEvery,
                { ruleoffsEvery = it },
                label = { Text("Ruleoffs Every (leave as 0 for length of lead)") },
                isError = ruleoffsEvery.toIntOrNull() == null,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(4.dp))
        val calls = remember {
            mutableStateListOf(
                AddCallInfo(
                    name = mutableStateOf("Bob"),
                    symbol = mutableStateOf("-"),
                    notation = mutableStateOf("14"),
                ),
                AddCallInfo(
                    name = mutableStateOf("Single"),
                    symbol = mutableStateOf("s"),
                    notation = mutableStateOf("1234"),
                ),
            )
        }
        Row {
            Text("Calls")
            Spacer(Modifier.width(4.dp))
            IconButton(
                onClick = { calls.add(AddCallInfo()) },
            ) {
                Icon(Icons.Default.Add, "Add call")
            }
        }
        calls.forEachIndexed { idx, call ->
            Row {
                listOf(
                    call.name to "Name",
                    call.symbol to "Symbol",
                    call.notation to "Notation",
                ).forEach { (field, text) ->
                    OutlinedTextField(
                        field.value,
                        { field.value = it },
                        label = { Text(text) },
                        isError = field.value.isBlank(),
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(2.dp))
                }
                listOf(
                    call.from to "From",
                    call.every to "Every (0 is lead length)",
                ).forEach { (field, text) ->
                    OutlinedTextField(
                        field.value,
                        { field.value = it },
                        label = { Text(text) },
                        isError = field.value.toIntOrNull() == null,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(2.dp))
                }
                IconButton(
                    onClick = { calls.removeAt(idx) },
                ) {
                    Icon(Icons.Default.Delete, "Remove call")
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                addMethodClicked(NewMethodDetails(name, stage, classificationState, placeNotation, ruleoffsEvery, ruleoffsFrom, calls))
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = model.nameError == null &&
                model.placeNotationError == null &&
                ruleoffsEvery.toIntOrNull() != null &&
                ruleoffsFrom.toIntOrNull() != null &&
                calls.all { it.isValid() },
        ) {
            Text("Save")
        }
    }
}

@Stable
private class AddCallInfo(
    val name: MutableState<String> = mutableStateOf(""),
    val symbol: MutableState<String> = mutableStateOf(""),
    val notation: MutableState<String> = mutableStateOf(""),
    val from: MutableState<String> = mutableStateOf("0"),
    val every: MutableState<String> = mutableStateOf("0"),
) {
    fun isValid(): Boolean {
        return name.value.isNotBlank() && symbol.value.isNotBlank() && notation.value.isNotBlank() &&
            from.value.toIntOrNull() != null && every.value.toIntOrNull() != null
    }
}

private val MethodClassification.display: String
    get() = when (this) {
        MethodClassification.None -> "None"
        else -> this.part
    }

private data class AddMethodUiModel(
    val stage: Int,
    val nameError: String?,
    val placeNotationError: String?,
)

private data class NewMethodDetails(
    val name: String,
    val stage: Int,
    val classification: MethodClassification,
    val placeNotation: String,
    val ruleoffsEvery: String,
    val ruleoffsFrom: String,
    val calls: List<AddCallInfo>,
)

private class AddMethodController(
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val methodRepository: MethodRepository = MethodRepository(),
    private val preferences: MethodCardsPreferences = getMethodCardsPreferences(),
) : ViewModel() {
    private val _uiState = MutableStateFlow<AddMethodUiModel?>(null)
    val uiState: StateFlow<AddMethodUiModel?> = _uiState.asStateFlow()

    private val stageFlow = MutableStateFlow(-1)
    private val nameFlow = MutableStateFlow("")
    private val placeNotation = MutableStateFlow("")

    init {
        viewModelScope.launch(defaultDispatcher) {
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
        methodRepository.searchByPlaceNotation(pn)?.let { existing ->
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
        details: NewMethodDetails,
    ) {
        viewModelScope.launch {
            val lengthOfLead = PlaceNotation(details.placeNotation)
                .fullNotation(details.stage)
                .notation
                .size
            val ruleoffsEvery = details.ruleoffsEvery.toIntOrNull()?.takeIf { it > 0 } ?: lengthOfLead
            val method = MethodWithCalls(
                name = details.name,
                placeNotation = PlaceNotation(details.placeNotation),
                stage = details.stage,
                ruleoffsEvery = ruleoffsEvery,
                ruleoffsFrom = details.ruleoffsFrom.toIntOrNull() ?: 0,
                classification = details.classification,
                calls = details.calls.map { call ->
                    val every = call.every.value.toIntOrNull()?.takeIf { it > 0 } ?: lengthOfLead
                    CallDetails(
                        methodName = details.name,
                        name = call.name.value,
                        symbol = call.symbol.value,
                        notation = PlaceNotation(call.notation.value),
                        from = call.from.value.toIntOrNull() ?: 0,
                        every = every,
                    )
                },
                enabledForMultiMethod = false,
                multiMethodFrequency = MethodFrequency.Regular,
                enabledForBlueline = false,
                customMethod = true,
            )
            methodRepository.addMethod(method)
        }
    }

    object Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return AddMethodController() as T
        }
    }
}
