package com.jpd.methodcards.presentation.settings

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Create
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.jpd.methodcards.data.MethodRepository
import com.jpd.methodcards.domain.MethodCollection
import com.jpd.methodcards.domain.MethodSelection
import com.jpd.methodcards.domain.MethodWithCalls
import com.jpd.methodcards.presentation.MethodCardScreen
import com.jpd.methodcards.presentation.icons.FilterList
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
            selectCollection = remember(controller) { controller::collectionSelected },
            setSearchTerm = remember(controller) { controller::setSearchTerm },
            openBlueline = { name, stage ->
                navigateToBlueline(
                    MethodCardScreen.SingleMethodBlueLine(
                        name,
                        "",
                        stage,
                    ),
                )
            },
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
    val controller: SettingsController = viewModel(
        viewModelStoreOwner = backStackEntry,
        factory = SettingsController.Factory,
    )
    val saveCollection = remember { controller::saveCollection }
    var showExplainer by remember { mutableStateOf(false) }
    var showSaveCollection by remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text("Methods Selection") },
        navigationIcon = navigationIcon,
        actions = {
            IconButton(onClick = { showSaveCollection = true }) {
                Icon(Icons.Outlined.Create, contentDescription = "Save Collection")
            }
            IconButton(onClick = { showExplainer = true }) {
                Icon(Icons.Outlined.Info, contentDescription = "Explainer")
            }
            IconButton(onClick = navigateToAddMethod) {
                Icon(Icons.Default.Add, contentDescription = "Add Method")
            }
        },
    )

    if (showSaveCollection) {
        var value by remember { mutableStateOf(TextFieldValue()) }
        AlertDialog(
            onDismissRequest = { showSaveCollection = false },
            confirmButton = {
                Text(
                    "Save",
                    modifier = Modifier.padding(8.dp).clickable {
                        saveCollection(value.text)
                        showSaveCollection = false
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            title = {
                Text("Save Collection")
            },
            text = {
                TextField(
                    value,
                    onValueChange = { value = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    label = { Text("Collection Name") },
                )
            },
        )
    }

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
    setStage: (Int?) -> Unit,
    selectMethod: (String) -> Unit,
    selectCollection: (String) -> Unit,
    setSearchTerm: (String) -> Unit,
    openBlueline: (String, Int) -> Unit,
) {
    Column(modifier) {
        var searchField by rememberSaveable { mutableStateOf("") }
        LaunchedEffect(Unit) {
            setSearchTerm(searchField)
        }
        var showFilters by remember { mutableStateOf(false) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp),
        ) {
            OutlinedTextField(
                value = searchField,
                onValueChange = { searchField = it; setSearchTerm(it) },
                label = { Text("Search") },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchField.isNotEmpty()) {
                        IconButton(onClick = { searchField = ""; setSearchTerm("") }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
            )
            IconButton(
                onClick = { showFilters = true },
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter")
            }
        }

        if (showFilters) {
            AlertDialog(
                onDismissRequest = { showFilters = false },
                confirmButton = {},
                title = { Text("Filter Methods") },
                text = {
                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                        ) {
                            TextField(
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                value = "Stage: ${model.stage ?: "All"}",
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
                                DropdownMenuItem(
                                    text = { Text("Stage: All", style = MaterialTheme.typography.bodyLarge) },
                                    onClick = {
                                        expanded = false
                                        setStage(null)
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                )
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
                })
        }

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
                ),
        ) {
            if (model.collections.isNotEmpty()) {
                stickyHeader {
                    Text(
                        modifier = Modifier.background(MaterialTheme.colorScheme.background).fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        text = "Collections",
                    )
                }
                items(model.collections, key = { "Collection_${it.first}" }) { (name, selected) ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(48.dp)
                                .clickable { selectCollection(name) }
                                .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(name, modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.alpha(if (selected) 1f else 0f),
                        )
                    }
                }
                stickyHeader {
                    Text(
                        modifier = Modifier.background(MaterialTheme.colorScheme.background).fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        text = "Methods",
                    )
                }
            }
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
                        onClick = { openBlueline(method.name, method.stage) },
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
    val stage: Int?,
    val methods: List<MethodSelection>,
    val collections: List<Pair<String, Boolean>>,
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
private class SettingsController(
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val methodRepository: MethodRepository = MethodRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow<SettingsUiModel?>(null)
    val uiState: StateFlow<SettingsUiModel?> = _uiState.asStateFlow()

    private val stage = MutableStateFlow<Int?>(null)
    private val searchTerm = MutableStateFlow("")
    private val collections = MutableStateFlow<List<MethodCollection>>(emptyList())

    init {
        methodRepository.getCollections()
            .onEach { collections.value = it }
            .launchIn(viewModelScope + defaultDispatcher)
        combine(
            methodRepository.getMethods(),
            searchTerm,
            stage,
            collections,
        ) { allMethods, search, stage, collections ->

            val pnTerm = search.replace('-', 'x')
            val methods = when {
                search.isBlank() && stage == null -> allMethods
                search.isBlank() && stage != null -> allMethods.filter { it.stage == stage }
                search.isNotBlank() && stage == null -> allMethods.filter {
                    it.name.contains(search, ignoreCase = true) ||
                        it.placeNotation.asString().contains(pnTerm, ignoreCase = true)
                }.sortedByDescending { it.name.startsWith(search, ignoreCase = true) }

                else -> allMethods.filter {
                    it.stage == stage &&
                        (it.name.contains(search, ignoreCase = true) ||
                            it.placeNotation.asString().contains(pnTerm, ignoreCase = true))
                }.sortedByDescending { it.name.startsWith(search, ignoreCase = true) }
            }

            val selectedCollections = if (search.isNotBlank()) {
                emptyList()
            } else {
                val selectedMethods = allMethods.mapNotNullTo(mutableSetOf()) { method -> method.name.takeIf { method.selected } }
                collections.map {
                    val methodSet = it.methods.toSet()
                    Pair(it.name, methodSet == selectedMethods)
                }
            }

            SettingsUiModel(
                stage = stage,
                methods = methods,
                collections = selectedCollections,
            )
        }
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope + defaultDispatcher)
    }

    fun setStage(stage: Int?) {
        this.stage.value = stage
    }

    fun methodSelected(method: String) {
        viewModelScope.launch {
            methodRepository.selectOrDeselectMethod(method)
        }
    }

    fun collectionSelected(collection: String) {
        viewModelScope.launch {
            methodRepository.selectCollection(collection)
        }
    }

    fun setSearchTerm(term: String) {
        searchTerm.value = term
    }

    fun saveCollection(name: String) {
        viewModelScope.launch {
            methodRepository.saveCollection(name)
        }
    }

    object Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return SettingsController() as T
        }
    }
}
