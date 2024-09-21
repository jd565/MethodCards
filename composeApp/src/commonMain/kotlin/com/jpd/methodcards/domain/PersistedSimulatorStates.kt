package com.jpd.methodcards.domain

import kotlinx.serialization.Serializable

@Serializable
data class PersistedSimulatorStates(
    val states: Map<Int, PersistedSimulatorState> = emptyMap(),
)
