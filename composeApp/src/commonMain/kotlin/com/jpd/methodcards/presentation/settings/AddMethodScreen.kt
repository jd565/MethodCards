package com.jpd.methodcards.presentation.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jpd.methodcards.data.MethodCardsPreferences
import com.jpd.methodcards.data.MethodRepository
import com.jpd.methodcards.di.MethodCardDi.getMethodCardsPreferences
import com.jpd.methodcards.domain.MethodWithCalls
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus

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
            addMethodClicked = remember(controller) { controller::addMethod },
        )
    } else {
        Box(modifier)
    }
}

@Composable
fun AddMethodView(
    model: AddMethodUiModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    addMethodClicked: () -> Unit,
) {
    Column(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(56.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Explainer")
            }
            Box(modifier = Modifier.weight(1f))
            IconButton(onClick = addMethodClicked) {
                Icon(Icons.Default.Add, contentDescription = "Add Method")
            }
        }
    }
}

data class AddMethodUiModel(
    val stage: Int,
    val stageName: String,
)

class AddMethodController(
    private val scope: CoroutineScope,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val methodRepository: MethodRepository = MethodRepository(),
    private val preferences: MethodCardsPreferences = getMethodCardsPreferences(),
) {
    private val _uiState = MutableStateFlow<AddMethodUiModel?>(null)
    val uiState: StateFlow<AddMethodUiModel?> = _uiState.asStateFlow()

    init {
        preferences.observeStage()
            .map {
                AddMethodUiModel(
                    stage = it,
                    stageName = MethodWithCalls.stageName(it),
                )
            }
            .onEach { _uiState.value = it }
            .launchIn(scope + defaultDispatcher)
    }

    fun addMethod() {
        TODO("Not yet implemented")
    }
}
