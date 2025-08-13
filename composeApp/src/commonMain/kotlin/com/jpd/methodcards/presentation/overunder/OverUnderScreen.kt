package com.jpd.methodcards.presentation.overunder

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.jpd.methodcards.data.MethodRepository
import com.jpd.methodcards.domain.MethodWithCalls
import com.jpd.methodcards.domain.PlaceNotation
import com.jpd.methodcards.domain.toBellChar
import com.jpd.methodcards.presentation.MethodCardScreen
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OverUnderScreen(
    navigateToBlueLine: (MethodCardScreen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val controller: OverUnderController = viewModel(
        factory = OverUnderController.Factory,
    )

    val onNext = remember(controller) { controller::onNext }
    val onUnderSelected = remember(controller) { controller::onUnderSelected }
    val onOverSelected = remember(controller) { controller::onOverSelected }
    val onStageSelected = remember(controller) { controller::onStageSelected }

    val controllerModel = controller.uiState.collectAsState().value

    BackHandler(controllerModel != null && controllerModel is OverUnderUiModel.MethodsList) {
        controller.onBack()
    }

    if (controllerModel != null) {
        AnimatedContent(
            targetState = controllerModel,
            modifier = modifier.fillMaxSize(),
            contentKey = { controllerModel.stage },
            transitionSpec = {
                val direction = if (targetState.stage > this.initialState.stage) {
                    AnimatedContentTransitionScope.SlideDirection.Start
                } else {
                    AnimatedContentTransitionScope.SlideDirection.End
                }
                slideIntoContainer(direction)
                    .togetherWith(slideOutOfContainer(direction))
            },
        ) { model ->
            when (model) {
                is OverUnderUiModel.MethodSelection -> {
                    OverUnderMethodSelection(
                        unders = model.unders,
                        overs = model.overs,
                        stage = model.stage,
                        onNext = onNext,
                        onUnderSelected = onUnderSelected,
                        onOverSelected = onOverSelected,
                        onStageSelected = onStageSelected,
                    )
                }

                is OverUnderUiModel.MethodsList -> {
                    OverUnderMethodsList(
                        methods = model.methods,
                        onMethodSelected = {
                            val name = if (it.unnamed) {
                                buildList {
                                    add("Unnamed")
                                    add("(${it.over} over ${it.under}")
                                    it.le?.let { le -> add("le $le") }
                                    it.hl?.let { hl -> add("hl $hl") }
                                }.joinToString(" ", postfix = ")")
                            } else {
                                it.method.name
                            }
                            navigateToBlueLine(
                                MethodCardScreen.SingleMethodBlueLine(
                                    name = name,
                                    placeNotation = it.method.placeNotation.asString(),
                                    stage = it.method.stage,
                                ),
                            )
                        },
                    )
                }
            }
        }
    } else {
        Box(modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverUnderTopBar(
    backStackEntry: NavBackStackEntry,
    navigationIcon: @Composable () -> Unit,
) {
    val controller: OverUnderController = viewModel(
        viewModelStoreOwner = backStackEntry,
        factory = OverUnderController.Factory,
    )
    var expanded by remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text("Over Under Methods") },
        navigationIcon = {
            val model = controller.uiState.collectAsState().value
            if (model != null && model is OverUnderUiModel.MethodsList) {
                IconButton(onClick = controller::onBack) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                }
            } else {
                navigationIcon()
            }
        },
        actions = {
            IconButton(
                onClick = { expanded = !expanded },
            ) { Icon(Icons.Default.Settings, contentDescription = "settings") }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                val state by controller.settingsState.collectAsState()
                listOf(
                    Triple("Unnamed", state.unnamed, { controller.toggleUnnamed() }),
                    Triple("Lead end variants", state.leadEndVariants, { controller.toggleLeadEndVariants() }),
                    Triple("Half lead variants", state.halfLeadVariants, { controller.toggleHalfLeadVariants() }),
                    Triple("Differential", state.differential, { controller.toggleDifferential() }),
                ).forEach { (text, isSelected, callback) ->
                    DropdownMenuItem(
                        text = { Text(text) },
                        onClick = callback,
                        trailingIcon = {
                            if (isSelected) {
                                Icon(Icons.Default.Check, null)
                            }
                        },
                    )
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OverUnderMethodSelection(
    unders: List<Pair<String, Boolean>>,
    overs: List<Pair<String, Boolean>>,
    stage: Int,
    onNext: () -> Unit,
    onUnderSelected: (String) -> Unit,
    onOverSelected: (String) -> Unit,
    onStageSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            TextField(
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                value = "Stage: $stage",
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
                            onStageSelected(stage)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.padding(horizontal = 20.dp), horizontalArrangement = spacedBy(8.dp)) {
                SelectionColumn("Unders", unders, onUnderSelected, Modifier.weight(1f))
                SelectionColumn("Overs", overs, onOverSelected, Modifier.weight(1f))
            }
            FloatingActionButton(
                onClick = onNext,
                modifier = Modifier.align(Alignment.BottomEnd)
                    .padding(40.dp),
            ) {
                Icon(Icons.AutoMirrored.Default.ArrowForward, "Next")
            }
        }
    }
}

@Composable
private fun SelectionColumn(
    title: String,
    items: List<Pair<String, Boolean>>,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        items.forEach { (name, selected) ->
            val selectedAlpha = animateFloatAsState(if (selected) 1f else 0f)
            Row(
                modifier = Modifier.clickable { onItemSelected(name) }.heightIn(40.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(4.dp),
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer { alpha = selectedAlpha.value }
                        .size(20.dp),
                )
                Text(
                    name,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun OverUnderMethodsList(
    methods: List<OverUnderUiModel.MethodsList.OverUnderMethod>,
    onMethodSelected: (OverUnderUiModel.MethodsList.OverUnderMethod) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        methods.forEach { method ->
            Column(
                modifier = Modifier
                    .clickable { onMethodSelected(method) }
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 20.dp),
            ) {
                Text(method.name, style = MaterialTheme.typography.bodyLarge)
                Text("${method.over} over ${method.under}", style = MaterialTheme.typography.bodySmall)
                val alterations = buildList {
                    if (method.hl != null) {
                        add("Half lead: ${method.hl}")
                    }
                    if (method.le != null) {
                        add("Lead end: ${method.le}")
                    }
                }.joinToString(" ")
                if (alterations.isNotEmpty()) {
                    Text(alterations, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

private sealed class OverUnderUiModel {
    data class MethodSelection(
        val unders: List<Pair<String, Boolean>>,
        val overs: List<Pair<String, Boolean>>,
        val stage: Int,
    ) : OverUnderUiModel()

    data class MethodsList(
        val methods: List<OverUnderMethod>,
    ) : OverUnderUiModel() {
        data class OverUnderMethod(
            val method: MethodWithCalls,
            val name: String,
            val over: String,
            val under: String,
            val le: String?,
            val hl: String?,
            val unnamed: Boolean,
            val differential: Boolean,
        )
    }
}

private data class OverUnderSettingsModel(
    val unnamed: Boolean = false,
    val leadEndVariants: Boolean = false,
    val halfLeadVariants: Boolean = false,
    val differential: Boolean = false,
)

private val OverUnderUiModel.stage: Int
    get() = when (this) {
        is OverUnderUiModel.MethodSelection -> 1
        is OverUnderUiModel.MethodsList -> 2
    }

private class OverUnderController(
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val methodRepository: MethodRepository = MethodRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow<OverUnderUiModel?>(null)
    val uiState = _uiState.asStateFlow()

    private val _settingsState = MutableStateFlow(OverUnderSettingsModel())
    val settingsState = _settingsState.asStateFlow()

    private val screenStage = MutableStateFlow(1)
    private val unders = MutableStateFlow(emptySet<String>())
    private val overs = MutableStateFlow(emptySet<String>())
    private val stage = MutableStateFlow(8)

    private val unnamed = MutableStateFlow(true)
    private val leadEndVariants = MutableStateFlow(true)
    private val halfLeadVariants = MutableStateFlow(false)
    private val differential = MutableStateFlow(true)

    init {
        viewModelScope.launch(defaultDispatcher) {
            val methodsFlow = combine(
                methodRepository.observeSelectedMethods(),
                stage,
            ) { methods, stage ->
                val allMethods = methods.filter {
                    it.stage == stage &&
                        it.underOverNotation != null
                }
                allMethods.map { it to it.nameWithoutStage() }
            }
            val baseUiStateFlow =
                combine(methodsFlow, screenStage, unders, overs) { methods, screenStage, unders, overs ->
                    if (screenStage == 1) {
                        OverUnderUiModel.MethodSelection(
                            unders = methods.map { it.second to (it.second in unders) },
                            overs = methods.map { it.second to (it.second in overs) },
                            stage = methods.firstOrNull()?.first?.stage ?: 8,
                        )
                    } else {
                        val stage = methods.first().first.stage
                        val allUnders = methods.filter { it.second in unders }
                        val allOvers = methods.filter { it.second in overs }
                        val existingMethodNotations = mutableSetOf<PlaceNotation>()
                        allUnders.mapTo(existingMethodNotations) { it.first.placeNotation }
                        allOvers.mapTo(existingMethodNotations) { it.first.placeNotation }

                        val newNotations = allUnders.flatMap { u ->
                            allOvers.flatMap { o ->
                                val underNotation = u.first.underOverNotation!!.first
                                val overNotation = o.first.underOverNotation!!.second
                                val lastDigit = u.first.stage.toBellChar()
                                val minusOne = u.first.stage.minus(1).toBellChar()

                                val halfLeads = if (underNotation.sequences.size == 2) {
                                    val fs = underNotation.sequences[0].last()
                                    val otherHalfLead = if (fs == "$minusOne$lastDigit") {
                                        PlaceNotation(
                                            listOf(
                                                underNotation.sequences[0].dropLast(1).plus("1$lastDigit"),
                                                underNotation.sequences[1],
                                            ),
                                        ) to "1$lastDigit"
                                    } else if (fs == "1$lastDigit") {
                                        PlaceNotation(
                                            listOf(
                                                underNotation.sequences[0].dropLast(1).plus("$minusOne$lastDigit"),
                                                underNotation.sequences[1],
                                            ),
                                        ) to "$minusOne$lastDigit"
                                    } else {
                                        null
                                    }
                                    listOfNotNull(underNotation to null, otherHalfLead)
                                } else {
                                    listOf(underNotation to null)
                                }

                                val leadEnds = if (overNotation.sequences.size == 2) {
                                    val fs = overNotation.sequences[1]
                                    val otherLeadEnd = if (fs == listOf("12")) {
                                        PlaceNotation(
                                            listOf(
                                                overNotation.sequences[0],
                                                listOf("1$lastDigit"),
                                            ),
                                        ) to "1$lastDigit"
                                    } else if (fs == listOf("1$lastDigit")) {
                                        PlaceNotation(
                                            listOf(
                                                overNotation.sequences[0],
                                                listOf("12"),
                                            ),
                                        ) to "12"
                                    } else {
                                        null
                                    }
                                    listOfNotNull(overNotation to null, otherLeadEnd)
                                } else {
                                    listOf(overNotation to null)
                                }

                                halfLeads.flatMap { (under, hl) ->
                                    leadEnds.mapNotNull { (over, le) ->
                                        under.merge(over)?.let { pn ->
                                            println("Searching for $pn (${o.second} over ${u.second})")
                                            OverUnderVariant(pn, o.second, u.second, le, hl)
                                        }
                                    }
                                }
                            }
                        }.sortedBy { it.underMethod == it.overMethod }.filter { it.pn !in existingMethodNotations }
                            .map {
                                val existing = methodRepository.searchByPlaceNotation(it.pn)
                                val method = existing
                                    ?: MethodWithCalls.fromPlaceNotation(
                                        "Unnamed",
                                        stage,
                                        it.pn,
                                    )
                                OverUnderUiModel.MethodsList.OverUnderMethod(
                                    method,
                                    name = method.name,
                                    over = it.overMethod,
                                    under = it.underMethod,
                                    le = it.leadEnd,
                                    hl = it.halfLead,
                                    unnamed = existing == null,
                                    differential = method.leadCycles.mapTo(mutableSetOf()) { lc -> lc.size }.size > 1,
                                )
                            }
                        OverUnderUiModel.MethodsList(newNotations)
                    }
                }

            combine(
                _settingsState,
                baseUiStateFlow,
            ) { settings, base ->
                when (base) {
                    is OverUnderUiModel.MethodSelection -> base
                    is OverUnderUiModel.MethodsList -> {
                        val methods = base.methods.filter {
                            val un = settings.unnamed || !it.unnamed
                            val le = settings.leadEndVariants || it.le == null
                            val hl = settings.halfLeadVariants || it.hl == null
                            val df = settings.differential || !it.differential
                            un && le && hl && df
                        }

                        OverUnderUiModel.MethodsList(
                            methods.map {
                                it.copy(name = it.method.nameWithoutStage())
                            },
                        )
                    }
                }
            }.collect { _uiState.value = it }
        }

        viewModelScope.launch {
            combine(
                unnamed,
                leadEndVariants,
                halfLeadVariants,
                differential,
                ::OverUnderSettingsModel,
            ).collect { _settingsState.value = it }
        }
    }

    fun onNext() {
        screenStage.update { it.plus(1).coerceAtMost(2) }
    }

    fun onUnderSelected(name: String) {
        unders.update {
            if (it.contains(name)) {
                it - name
            } else {
                it + name
            }
        }
    }

    fun onStageSelected(stage: Int) {
        this.stage.value = stage
    }

    fun onOverSelected(name: String) {
        overs.update {
            if (it.contains(name)) {
                it - name
            } else {
                it + name
            }
        }
    }

    fun onBack() {
        screenStage.value = 1
    }

    fun toggleUnnamed() {
        unnamed.update { !it }
    }

    fun toggleLeadEndVariants() {
        leadEndVariants.update { !it }
    }

    fun toggleHalfLeadVariants() {
        halfLeadVariants.update { !it }
    }

    fun toggleDifferential() {
        differential.update { !it }
    }

    object Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return OverUnderController() as T
        }
    }
}

private data class OverUnderVariant(
    val pn: PlaceNotation,
    val overMethod: String,
    val underMethod: String,
    val leadEnd: String?,
    val halfLead: String?
)
