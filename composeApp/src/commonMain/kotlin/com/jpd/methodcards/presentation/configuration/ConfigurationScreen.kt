package com.jpd.methodcards.presentation.configuration

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.jpd.methodcards.di.MethodCardDi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.reflect.KClass

@Composable
fun ConfigurationScreen(
    modifier: Modifier = Modifier,
) {
    val controller: ConfigurationController = viewModel(factory = ConfigurationController.Factory)
    val model = controller.uiState.collectAsState().value

    if (model != null) {
        ConfigurationView(
            model = model,
            modifier = modifier,
            onDarkModeChange = remember(controller) { controller::onDarkModeChange },
        )
    } else {
        Box(modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigurationView(
    model: ConfigurationUiModel,
    modifier: Modifier = Modifier,
    onDarkModeChange: (Boolean?) -> Unit,
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
                    value = "Dark mode: ${model.currentSelection}",
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
                    listOf(
                        "Light" to false,
                        "Dark" to true,
                        "System Default" to null,
                    ).forEach { (label, value) ->
                        DropdownMenuItem(
                            text = { Text(label, style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                expanded = false
                                onDarkModeChange(value)
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }
        }
    }
}

private data class ConfigurationUiModel(
    val currentSelection: String,
)

private class ConfigurationController(
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val methodPreferences: MethodCardsPreferences = MethodCardDi.getMethodCardsPreferences(),
) : ViewModel() {
    private val _uiState = MutableStateFlow<ConfigurationUiModel?>(null)
    val uiState: StateFlow<ConfigurationUiModel?> = _uiState.asStateFlow()

    init {
        methodPreferences.observeDarkModePreference()
            .onEach { preference ->
                _uiState.value = ConfigurationUiModel(
                    currentSelection = when (preference) {
                        true -> "Dark"
                        false -> "Light"
                        null -> "System Default"
                    }
                )
            }.launchIn(viewModelScope + defaultDispatcher)
    }

    fun onDarkModeChange(enabled: Boolean?) {
        viewModelScope.launch(defaultDispatcher) {
            methodPreferences.setDarkModePreference(enabled)
        }
    }

    object Factory: ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return ConfigurationController() as T
        }
    }
}
