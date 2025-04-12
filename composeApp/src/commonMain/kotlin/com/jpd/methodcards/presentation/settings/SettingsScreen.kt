package com.jpd.methodcards.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.jpd.methodcards.data.MethodRepository
import com.jpd.methodcards.domain.MethodSelection
import com.jpd.methodcards.domain.MethodWithCalls
import com.jpd.methodcards.presentation.MethodCardScreen
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.reflect.KClass

private const val SettingsExplainer =
    "On this screen you can select how many bells (stage), and which methods should " +
        "be enabled. Once enabled, methods can be selected for use in individual screens without needing to change them here."

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navigateToBlueline: (MethodCardScreen.SingleMethodBlueLine) -> Unit,
) {
    val controller: SettingsController = viewModel(factory = SettingsController.Factory)
    val model = controller.uiState.collectAsState().value

    if (model != null) {
        SettingsView(
            model = model,
            modifier = modifier,
            setStage = remember(controller) { controller::setStage },
            selectMethod = remember(controller) { controller::methodSelected },
            setSearchTerm = remember(controller) { controller::setSearchTerm },
            openBlueline = { navigateToBlueline(MethodCardScreen.SingleMethodBlueLine(it, "", model.stage))},
        )
    } else {
        Box(modifier)
    }
}

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(
    backStackEntry: NavBackStackEntry,
    navigateToAddMethod: () -> Unit,
    navigationIcon: @Composable () -> Unit,
) {
    var showExplainer by remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text("Methods Selection") },
        navigationIcon = navigationIcon,
        actions = {
            IconButton(onClick = { showExplainer = true }) {
                Icon(Icons.Outlined.Info, contentDescription = "Explainer")
            }
            IconButton(onClick = navigateToAddMethod) {
                Icon(Icons.Default.Add, contentDescription = "Add Method")
            }
        }
    )

    if (showExplainer) {
        AlertDialog(
            onDismissRequest = { showExplainer = false },
            confirmButton = {},
            title = {
                Text("Settings Screen")
            },
            text = {
                Text(SettingsExplainer)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsView(
    model: SettingsUiModel,
    modifier: Modifier = Modifier,
    setStage: (Int) -> Unit,
    selectMethod: (String) -> Unit,
    setSearchTerm: (String) -> Unit,
    openBlueline: (String) -> Unit,
) {
    Column(modifier) {
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
        var searchField by rememberSaveable { mutableStateOf("") }
        LaunchedEffect(Unit) {
            setSearchTerm(searchField)
        }
        OutlinedTextField(
            value = searchField,
            onValueChange = { searchField = it; setSearchTerm(it) },
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchField.isNotEmpty()) {
                    IconButton(onClick = { searchField = ""; setSearchTerm("") }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
            },
        )
        val scrollState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        LazyColumn(
            state = scrollState,
            modifier = Modifier.weight(1f).fillMaxWidth()
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            scrollState.scrollBy(-delta)
                        }
                    },
                )
            ,
        ) {
            items(
                model.methods,
                key = { it.name },
            ) { method ->
                Row(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(48.dp)
                        .clickable { selectMethod(method.name) }
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(method.name, modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { openBlueline(method.name) },
                        content = {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Blueline",
                            )
                        },
                    )
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.alpha(if (method.selected) 1f else 0f),
                    )
                }
            }
        }
    }
}

private data class SettingsUiModel(
    val stage: Int,
    val methods: List<MethodSelection>,
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
private class SettingsController(
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val methodRepository: MethodRepository = MethodRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow<SettingsUiModel?>(null)
    val uiState: StateFlow<SettingsUiModel?> = _uiState.asStateFlow()

    private val searchTerm = MutableStateFlow("")

    init {
        combine(
            methodRepository.getMethods(),
            searchTerm,
        ) { (stage, methods), search ->
            if (search.isBlank()) {
                SettingsUiModel(
                    stage = stage,
                    methods = methods,
                )
            } else {
                val pnTerm = search.replace('-', 'x')
                SettingsUiModel(
                    stage = stage,
                    methods = methods.filter {
                        it.name.contains(search, ignoreCase = true) ||
                            it.placeNotation.asString().contains(pnTerm, ignoreCase = true)
                    },
                )
            }
        }
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope + defaultDispatcher)
    }

    fun setStage(stage: Int) {
        viewModelScope.launch {
            methodRepository.setStage(stage)
        }
    }

    fun methodSelected(method: String) {
        viewModelScope.launch {
            methodRepository.selectOrDeselectMethod(method)
        }
    }

    fun setSearchTerm(term: String) {
        searchTerm.value = term
    }

    object Factory: ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return SettingsController() as T
        }
    }
}
