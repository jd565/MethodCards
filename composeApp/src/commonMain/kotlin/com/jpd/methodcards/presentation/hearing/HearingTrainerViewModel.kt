package com.jpd.methodcards.presentation.hearing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.jpd.methodcards.di.MethodCardDi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalResourceApi::class)
class HearingTrainerViewModel @OptIn(ExperimentalTime::class) constructor(
    private val audioPlayer: AudioPlayer = MethodCardDi.getAudioPlayer(),
    private val random: Random = Random,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val clock: Clock = Clock.System,
    private val strikingTrainer: Boolean,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HearingTrainerState())
    val uiState = _uiState.asStateFlow()
    private var ringingJob: Job? = null
    private val pealSpeed: Duration = 3.hours
    private var alteredBell: Int = -1
    private var alteredBellFast: Boolean = false
    private var row: List<Int> = emptyList()

    init {
        newRow()
    }

    private fun newRow() {
        _uiState.update {
            it.copy(
                stage = 8,
                isPlaying = false,
                showFeedbackDialog = false,
                feedbackCorrect = false,
                isBellFast = null,
                selectedBell = null,
            )
        }
        alteredBell = random.nextInt(_uiState.value.stage - 1) + 1
        alteredBellFast = random.nextBoolean()
        row = List(7) { it + 1 }.shuffled(random).plus(8)
    }

    fun playPause() {
        if (_uiState.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun play() {
        _uiState.update { it.copy(isPlaying = true) }
        ringingJob?.cancel()
        ringingJob = viewModelScope.launch(defaultDispatcher) {
            val start = clock.now()
            if (strikingTrainer) {
                repeat(_uiState.value.stage) { idx ->
                    val bell = idx + 1
                    val bellDelay = if (alteredBell == bell && alteredBellFast) {
                        println(bellFastDelay.toString())
                        bellFastDelay
                    } else if (alteredBell == bell && !alteredBellFast) {
                        bellSlowDelay
                    } else {
                        bellNormalDelay
                    }
                    val delayFromStart = bellDelay + bellNormalDelay * idx
                    val time = start.plus(delayFromStart)
                    val delay = time - clock.now()
                    delay(delay)
                    println("Playing bell $bell after delay $delay ($delayFromStart)")
                    audioPlayer.playBell(bell)
                }
            } else {
                row.forEachIndexed { idx, bell ->
                    val delayFromStart = bellNormalDelay * (idx + 1)
                    val time = start.plus(delayFromStart)
                    val delay = time - clock.now()
                    delay(delay)
                    println("Playing bell $bell after delay $delay ($delayFromStart)")
                    audioPlayer.playBell(bell)
                }
            }
            _uiState.update { it.copy(isPlaying = false) }
        }
    }

    private val bellNormalDelay: Duration by lazy {
        val perRow = pealSpeed.div(5000)
        perRow.div(_uiState.value.stage)
    }

    private val bellSlowDelay: Duration = bellNormalDelay * 1.2

    private val bellFastDelay: Duration = bellNormalDelay * 0.8

    private fun pause() {
        ringingJob?.cancel()
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun selectBell(bell: Int) {
        _uiState.update {
            it.copy(selectedBell = bell)
        }
    }

    fun selectFast() {
        _uiState.update {
            it.copy(isBellFast = true)
        }
    }

    fun selectSlow() {
        _uiState.update {
            it.copy(isBellFast = false)
        }
    }

    fun submitGuess() {
        check()
    }

    private fun check() {
        val current = _uiState.value
        if (strikingTrainer) {
            if (current.selectedBell != null && current.isBellFast != null) {
                val correctBell = alteredBell == current.selectedBell
                val correctDirection = alteredBellFast == current.isBellFast
                val isCorrect = correctBell && correctDirection
                _uiState.update {
                    it.copy(
                        showFeedbackDialog = true,
                        feedbackCorrect = isCorrect,
                    )
                }
            }
        } else {
            if (current.selectedBell != null) {
                val isCorrect = row.indexOf(3) + 1 == current.selectedBell
                _uiState.update {
                    it.copy(
                        showFeedbackDialog = true,
                        feedbackCorrect = isCorrect,
                    )
                }
            }
        }
    }

    fun dismissFeedback(wasCorrect: Boolean) {
        if (wasCorrect) {
            newRow()
        }
    }

    companion object {
        @OptIn(ExperimentalTime::class)
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                return HearingTrainerViewModel(strikingTrainer = true) as T
            }
        }

        @OptIn(ExperimentalTime::class)
        val PositionFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                return HearingTrainerViewModel(strikingTrainer = false) as T
            }
        }
    }
}
