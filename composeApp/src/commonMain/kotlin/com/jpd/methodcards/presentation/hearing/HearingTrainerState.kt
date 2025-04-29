package com.jpd.methodcards.presentation.hearing

data class HearingTrainerState(
    val stage: Int = 8,
    val isPlaying: Boolean = false,
    val showFeedbackDialog: Boolean = false,
    val feedbackMessage: String = "",
    val isBellFast: Boolean? = null,
    val selectedBell: Int? = null,
)
