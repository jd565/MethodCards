package com.jpd.methodcards.presentation.blueline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jpd.methodcards.data.MethodRepository
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

@Composable
fun BlueLineMethodListScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
) {
    val controller: BlueLineMethodListController = viewModel(
        factory = BlueLineMethodListController.Factory
    )

    val model = controller.uiState.collectAsStateWithLifecycle().value

    val selectMethod: (String) -> Unit = remember(controller, navigateBack) {
        {
            controller.selectMethod(it)
            navigateBack()
        }
    }

    if (model != null) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            model.methods.forEach { method ->
                Box(
                    modifier =
                        Modifier
                            .heightIn(32.dp)
                            .fillMaxWidth()
                            .clickable {
                                selectMethod(method.name)
                            }.padding(horizontal = 40.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(method.name)
                }
            }
        }
    } else {
        Box(modifier)
    }
}

private data class BlueLineMethodListModel(
    val methods: List<MethodWithCalls>,
    val selectedMethod: MethodWithCalls?,
)

private class BlueLineMethodListController(
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val methodRepository: MethodRepository = MethodRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow<BlueLineMethodListModel?>(null)
    val uiState = _uiState.asStateFlow()

    init {
        methodRepository.observeSelectedMethods()
            .map { methods ->
                val selectedMethod = methods.firstOrNull { it.enabledForBlueline } ?: methods.firstOrNull()
                BlueLineMethodListModel(
                    methods = methods,
                    selectedMethod = selectedMethod,
                )
            }
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope + defaultDispatcher)
    }

    fun selectMethod(method: String) {
        viewModelScope.launch {
            methodRepository.setBlueLineMethod(method)
        }
    }

    object Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return BlueLineMethodListController() as T
        }
    }
}

