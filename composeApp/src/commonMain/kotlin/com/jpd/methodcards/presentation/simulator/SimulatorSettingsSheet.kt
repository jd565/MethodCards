package com.jpd.methodcards.presentation.simulator

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.jpd.methodcards.data.MethodCardsPreferences
import com.jpd.methodcards.di.MethodCardDi.getMethodCardsPreferences
import com.jpd.methodcards.domain.CallFrequency
import com.jpd.methodcards.domain.ExtraPathType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

@Composable
fun SimulatorSettingsSheet() {
    val scope = rememberCoroutineScope()
    val controller = remember(scope) { SimulatorSettingsController(scope) }
    val model = controller.uiState.collectAsState().value

    if (model != null) {
        SimulatorSettingsView(
            model = model,
            setShowTrebleLine = remember(controller) { controller::setShowTrebleLine },
            setHalfLeadSplicing = remember(controller) { controller::setHalfLeadSplicing },
            setCallFrequency = remember(controller) { controller::setCallFrequency },
            setShowLeadEndNotation = remember(controller) { controller::setShowLeadEndNotation },
            setShowCourseBell = remember(controller) { controller::setShowCourseBells },
            setUse4thsPlaceCalls = remember(controller) { controller::setUse4thsPlaceCalls },
            setHandbellMode = remember(controller) { controller::setHandbellMode },
        )
    } else {
        Box(Modifier.fillMaxWidth())
    }
}

@Composable
private fun SimulatorSettingsView(
    model: SimulatorSettingsState,
    setShowTrebleLine: (ExtraPathType) -> Unit,
    setShowCourseBell: (ExtraPathType) -> Unit,
    setHalfLeadSplicing: (Boolean) -> Unit,
    setShowLeadEndNotation: (Boolean) -> Unit,
    setUse4thsPlaceCalls: (Boolean) -> Unit,
    setCallFrequency: (CallFrequency) -> Unit,
    setHandbellMode: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Text("Show treble line")
        Row(horizontalArrangement = spacedBy(8.dp)) {
            ExtraPathType.entries.forEach { f ->
                if (model.showTrebleLine == f) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {},
                    ) {
                        Text(f.name)
                    }
                } else {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { setShowTrebleLine(f) },
                    ) {
                        Text(f.name)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Show course bells", modifier = Modifier.alpha(if (model.showCourseBellsEnabled) 1f else 0.5f))
        Row(horizontalArrangement = spacedBy(8.dp)) {
            ExtraPathType.entries.forEach { f ->
                if (model.showCourseBells == f) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {},
                        enabled = model.showCourseBellsEnabled,
                    ) {
                        Text(f.name)
                    }
                } else {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { setShowCourseBell(f) },
                        enabled = model.showCourseBellsEnabled,
                    ) {
                        Text(f.name)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Show lead end notation", modifier = Modifier.weight(1f))
            Checkbox(
                checked = model.showLeadEndNotation,
                onCheckedChange = { setShowLeadEndNotation(it) },
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Always use 4th place calls", modifier = Modifier.weight(1f))
            Checkbox(
                checked = model.use4thsPlaceCalls,
                onCheckedChange = { setUse4thsPlaceCalls(it) },
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Handbell simulation", modifier = Modifier.weight(1f))
            Checkbox(
                checked = model.handbellMode,
                onCheckedChange = { setHandbellMode(it) },
            )
        }
        // Row(
        //     modifier = Modifier.fillMaxWidth(),
        //     verticalAlignment = Alignment.CenterVertically,
        // ) {
        //     Text("Half lead splicing", modifier = Modifier.weight(1f))
        //     Checkbox(
        //         checked = model.halfLeadSplicing,
        //         onCheckedChange = { setHalfLeadSplicing(it) },
        //     )
        // }
        Spacer(Modifier.height(8.dp))
        Text("Call frequency")
        Row(horizontalArrangement = spacedBy(8.dp)) {
            CallFrequency.entries.forEach { f ->
                if (model.callFrequency == f) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {},
                    ) {
                        Text(f.name)
                    }
                } else {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { setCallFrequency(f) },
                    ) {
                        Text(f.name)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

private data class SimulatorSettingsState(
    val showTrebleLine: ExtraPathType,
    val showCourseBells: ExtraPathType,
    val showCourseBellsEnabled: Boolean,
    val showLeadEndNotation: Boolean,
    val callFrequency: CallFrequency,
    val halfLeadSplicing: Boolean,
    val use4thsPlaceCalls: Boolean,
    val handbellMode: Boolean,
)

private class SimulatorSettingsController(
    private val scope: CoroutineScope,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val methodCardsPreferences: MethodCardsPreferences = getMethodCardsPreferences(),
) {
    private val _uiState = MutableStateFlow<SimulatorSettingsState?>(null)
    val uiState = _uiState.asStateFlow()

    init {
        combine(
            methodCardsPreferences.observeSimulatorShowTreble(),
            methodCardsPreferences.observeSimulatorShowCourseBell(),
            methodCardsPreferences.observeSimulatorShowLeadEndNotation(),
            methodCardsPreferences.observeSimulatorCallFrequency(),
            methodCardsPreferences.observeSimulatorHalfLeadSplicing(),
            methodCardsPreferences.observeSimulatorUse4thsPlaceCalls(),
            methodCardsPreferences.observeSimulatorHandbellMode(),
        ) { arr ->
            val handbellMode = arr[6] as Boolean
            val showCourseBells = if (handbellMode) ExtraPathType.None else arr[1] as ExtraPathType
            SimulatorSettingsState(
                showTrebleLine = arr[0] as ExtraPathType,
                showCourseBells = showCourseBells,
                showCourseBellsEnabled = !handbellMode,
                showLeadEndNotation = arr[2] as Boolean,
                callFrequency = arr[3] as CallFrequency,
                halfLeadSplicing = arr[4] as Boolean,
                use4thsPlaceCalls = arr[5] as Boolean,
                handbellMode = handbellMode,
        ) }.onEach { _uiState.value = it }
            .launchIn(scope + dispatcher)
    }

    fun setShowTrebleLine(show: ExtraPathType) {
        scope.launch {
            methodCardsPreferences.setSimulatorShowTreble(show)
        }
    }

    fun setShowCourseBells(show: ExtraPathType) {
        scope.launch {
            methodCardsPreferences.setSimulatorShowCourseBell(show)
        }
    }

    fun setUse4thsPlaceCalls(use: Boolean) {
        scope.launch {
            methodCardsPreferences.setSimulatorUse4thsPlaceCalls(use)
        }
    }

    fun setShowLeadEndNotation(show: Boolean) {
        scope.launch {
            methodCardsPreferences.setSimulatorShowLeadEndNotation(show)
        }
    }

    fun setCallFrequency(frequency: CallFrequency) {
        scope.launch {
            methodCardsPreferences.setSimulatorCallFrequency(frequency)
        }
    }

    fun setHalfLeadSplicing(enabled: Boolean) {
        scope.launch {
            methodCardsPreferences.setSimulatorHalfLeadSplicing(enabled)
        }
    }

    fun setHandbellMode(enabled: Boolean) {
        scope.launch {
            methodCardsPreferences.setSimulatorHandbellMode(enabled)
        }
    }
}
