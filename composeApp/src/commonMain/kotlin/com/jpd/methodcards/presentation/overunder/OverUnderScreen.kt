package com.jpd.methodcards.presentation.overunder

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jpd.methodcards.data.MethodCardsPreferences
import com.jpd.methodcards.data.MethodRepository
import com.jpd.methodcards.di.MethodCardDi.getMethodCardsPreferences
import com.jpd.methodcards.domain.PlaceNotation
import com.jpd.methodcards.domain.toBellChar
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Composable
fun OverUnderScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(scope, onBack) { OverUnderController(scope, onBack) }
    DisposableEffect(controller) {
        onDispose {
            controller.onCleared()
        }
    }

    val onNext = remember(controller) { controller::onNext }
    val onUnderSelected = remember(controller) { controller::onUnderSelected }
    val onOverSelected = remember(controller) { controller::onOverSelected }
    val onTopBarBack = remember(controller) { controller::onBack }

    val model = controller.uiState.collectAsState().value

    if (model != null) {
        Column(modifier = modifier) {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(56.dp).padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onTopBarBack) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                }
                Text("Over Under Methods")
            }
            AnimatedContent(
                targetState = model,
                modifier = Modifier.fillMaxSize(),
                contentKey = { model.stage },
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
                            onNext = onNext,
                            onUnderSelected = onUnderSelected,
                            onOverSelected = onOverSelected,
                        )
                    }

                    is OverUnderUiModel.MethodsList -> {
                        OverUnderMethodsList(
                            methods = model.methods,
                        )
                    }
                }
            }
        }
    } else {
        Box(modifier)
    }
}

@Composable
private fun OverUnderMethodSelection(
    unders: List<Pair<String, Boolean>>,
    overs: List<Pair<String, Boolean>>,
    onNext: () -> Unit,
    onUnderSelected: (String) -> Unit,
    onOverSelected: (String) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Row {
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Text("Unders")
                unders.forEach { (name, selected) ->
                    Text(
                        name, color = if (selected) Color.Black else Color.Gray,
                        modifier = Modifier.fillMaxWidth()
                            .clickable { onUnderSelected(name) },
                    )
                }
            }
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Text("Overs")
                overs.forEach { (name, selected) ->
                    Text(
                        name, color = if (selected) Color.Black else Color.Gray,
                        modifier = Modifier.fillMaxWidth()
                            .clickable { onOverSelected(name) },
                    )
                }
            }
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

@Composable
private fun OverUnderMethodsList(methods: List<OverUnderUiModel.MethodsList.OverUnderMethod>) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        methods.forEach {
            val text = buildString {
                append(it.name)
                append(" (")
                append(it.over)
                append(" over ")
                append(it.under)
                if (it.hl != null) {
                    append(" hl ${it.hl}")
                }
                if (it.le != null) {
                    append(" le ${it.le}")
                }
                append(")")
            }
            Text(text)
        }
    }
}

private sealed class OverUnderUiModel {
    data class MethodSelection(
        val unders: List<Pair<String, Boolean>>,
        val overs: List<Pair<String, Boolean>>,
    ) : OverUnderUiModel()

    data class MethodsList(
        val methods: List<OverUnderMethod>,
    ) : OverUnderUiModel() {
        data class OverUnderMethod(
            val name: String,
            val over: String,
            val under: String,
            val le: String?,
            val hl: String?,
        )
    }
}

private val OverUnderUiModel.stage: Int
    get() = when (this) {
        is OverUnderUiModel.MethodSelection -> 1
        is OverUnderUiModel.MethodsList -> 2
    }

private class OverUnderController(
    private val scope: CoroutineScope,
    private val onNavigateBack: () -> Unit,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val methodRepository: MethodRepository = MethodRepository(),
    methodCardsPreferences: MethodCardsPreferences = getMethodCardsPreferences(),
) {
    private val _uiState = MutableStateFlow<OverUnderUiModel?>(null)
    val uiState = _uiState.asStateFlow()

    private val stage = MutableStateFlow(1)
    private val unders = MutableStateFlow(emptySet<String>())
    private val overs = MutableStateFlow(emptySet<String>())

    init {
        scope.launch(defaultDispatcher) {
            val methodsFlow = combine(
                methodRepository.observeSelectedMethods(),
                methodCardsPreferences.observeStage(),
            ) { methods, stage ->
                val allMethods = methods.filter {
                    it.stage == stage &&
                        it.underOverNotation != null
                }
                allMethods.map { it to it.shortName(allMethods) }
            }
            combine(methodsFlow, stage, unders, overs) { methods, stage, unders, overs ->
                if (stage == 1) {
                    OverUnderUiModel.MethodSelection(
                        unders = methods.map { it.second to (it.second in unders) },
                        overs = methods.map { it.second to (it.second in overs) },
                    )
                } else {
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
                                        )
                                    ) to "1$lastDigit"
                                } else if (fs == "1$lastDigit") {
                                    PlaceNotation(
                                        listOf(
                                            underNotation.sequences[0].dropLast(1).plus("$minusOne$lastDigit"),
                                            underNotation.sequences[1],
                                        )
                                    ) to "$minusOne$lastDigit"
                                } else {
                                    null
                                }
                                listOfNotNull(underNotation to null, otherHalfLead)
                            } else {
                                listOf(underNotation to null)
                            }

                            val leadEnds = if (overNotation.sequences.size == 2) {
                                val fs = overNotation.sequences[1].last()
                                val otherLeadEnd = if (fs == "$minusOne$lastDigit") {
                                    PlaceNotation(
                                        listOf(
                                            overNotation.sequences[0],
                                            overNotation.sequences[1].dropLast(1).plus("1$lastDigit"),
                                        )
                                    ) to "1$lastDigit"
                                } else if (fs == "1$lastDigit") {
                                    PlaceNotation(
                                        listOf(
                                            overNotation.sequences[0],
                                            overNotation.sequences[1].dropLast(1).plus("$minusOne$lastDigit"),
                                        )
                                    ) to "$minusOne$lastDigit"
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
                                        OverUnderVariant(pn, u.second, o.second, le, hl)
                                    }
                                }
                            }
                        }
                    }.sortedBy { it.underMethod == it.overMethod }.filter { it.pn !in existingMethodNotations }
                    val allFound = methodRepository.searchByPlaceNotations(newNotations.map { it.pn })
                    OverUnderUiModel.MethodsList(
                        methods = allFound.map { found ->
                            val variant = newNotations.first { it.pn == found.placeNotation }
                            OverUnderUiModel.MethodsList.OverUnderMethod(
                                name = found.shortName(allFound),
                                over = variant.overMethod,
                                under = variant.underMethod,
                                le = variant.leadEnd,
                                hl = variant.halfLead,
                            )
                        },
                    )
                }
            }
                .collect { _uiState.value = it }
        }
    }

    fun onNext() {
        stage.update { it.plus(1).coerceAtMost(2) }
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
        if (stage.value == 1) {
            onNavigateBack()
        } else {
            stage.value = 1
        }
    }

    fun onCleared() {
    }
}

private data class OverUnderVariant(
    val pn: PlaceNotation,
    val overMethod: String,
    val underMethod: String,
    val leadEnd: String?,
    val halfLead: String?
)
