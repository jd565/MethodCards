package com.jpd.methodcards.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.jpd.methodcards.domain.MethodFrequency
import com.jpd.methodcards.domain.MethodWithCalls
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiMethodSelectionScreen(
    modifier: Modifier = Modifier,
) {
    val controller: MultiMethodSelectionController = viewModel(
        factory = MultiMethodSelectionController.Factory,
    )

    val model = controller.uiState.collectAsState().value

    if (model != null) {
        MultiMethodSelectionView(
            model = model,
            modifier = modifier,
            selectMethod = remember(controller) { controller::selectMethod },
            setMethodFrequency = remember(controller) { controller::setMethodFrequency },
        )
    } else {
        Box(modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiMethodSelectionTopBar(
    backStackEntry: NavBackStackEntry,
    navigationIcon: @Composable () -> Unit,
) {
    val controller: MultiMethodSelectionController = viewModel(
        viewModelStoreOwner = backStackEntry,
        factory = MultiMethodSelectionController.Factory,
    )
    TopAppBar(
        title = { Text("Select Methods") },
        navigationIcon = navigationIcon,
        actions = {
            IconButton(onClick = { controller.deselectAllMethods() }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear")
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MultiMethodSelectionView(
    model: MultiMethodSelectionUiModel,
    modifier: Modifier,
    selectMethod: (String) -> Unit,
    setMethodFrequency: (String, MethodFrequency) -> Unit,
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        model.methods.forEach { method ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                    .clickable { selectMethod(method.name) }
                    .padding(
                        horizontal = 16.dp,
                        vertical = 4.dp,
                    ),
            ) {
                Text(method.shortName(model.methods), modifier = Modifier.weight(1f))
                var frequencyExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = frequencyExpanded,
                    onExpandedChange = { frequencyExpanded = it },
                    modifier = Modifier.weight(1f),
                ) {
                    TextField(
                        modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
                        value = method.multiMethodFrequency.name,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                frequencyExpanded,
                            )
                        },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    )
                    ExposedDropdownMenu(
                        expanded = frequencyExpanded,
                        modifier = Modifier.exposedDropdownSize(false),
                        onDismissRequest = { frequencyExpanded = false },
                    ) {
                        MethodFrequency.entries.forEach { f ->
                            DropdownMenuItem(
                                text = { Text(f.name) },
                                onClick = {
                                    frequencyExpanded = false
                                    setMethodFrequency(method.name, f)
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }

                val isSelected = method.enabledForMultiMethod
                Icon(
                    Icons.Default.Check,
                    contentDescription = if (isSelected) "selected" else null,
                    modifier = Modifier.alpha(if (isSelected) 1f else 0f),
                )
            }

        }
    }
}

@Immutable
private data class MultiMethodSelectionUiModel(
    val methods: List<MethodWithCalls>,
)

private class MultiMethodSelectionController(
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val methodRepository: MethodRepository = MethodRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow<MultiMethodSelectionUiModel?>(null)
    val uiState = _uiState.asStateFlow()

    init {
        methodRepository.observeSelectedMethods()
            .map { methods ->
                MultiMethodSelectionUiModel(methods)
            }
            .onEach { uiModel ->
                _uiState.value = uiModel
            }
            .launchIn(viewModelScope + defaultDispatcher)
    }

    fun deselectAllMethods() {
        viewModelScope.launch {
            methodRepository.deselectAllMultiMethod()
        }
    }

    fun selectMethod(method: String) {
        viewModelScope.launch {
            methodRepository.selectOrDeselectMultiMethod(method)
        }
    }

    fun setMethodFrequency(method: String, frequency: MethodFrequency) {
        viewModelScope.launch {
            methodRepository.setMultiMethodFrequency(method, frequency)
        }
    }

    object Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return MultiMethodSelectionController() as T
        }
    }
}
