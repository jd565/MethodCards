package com.jpd.methodcards.data

import com.jpd.methodcards.domain.CallFrequency
import com.jpd.methodcards.domain.ExtraPathType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.w3c.dom.Storage

class MethodCardsStoragePreferences(
    private val storage: Storage,
) : MethodCardsPreferences {
    private val stage = MutableStateFlow(8)
    private val simulatorShowTreble = MutableStateFlow(ExtraPathType.Full)
    private val simulatorShowCourseBell = MutableStateFlow(ExtraPathType.None)
    private val simulatorShowLeadEndNotation = MutableStateFlow(false)
    private val simulatorCallFrequency = MutableStateFlow(CallFrequency.Regular)
    private val simulatorHalfLeadSplicing = MutableStateFlow(false)
    private val simulatorUse4thsPlaceCalls = MutableStateFlow(false)

    override fun observeStage(): Flow<Int> {
        return stage
    }

    override suspend fun setStage(stage: Int) {
        this.stage.value = stage
    }

    override fun observeSimulatorShowTreble(): Flow<ExtraPathType> {
        return simulatorShowTreble
    }

    override fun observeSimulatorShowCourseBell(): Flow<ExtraPathType> {
        return simulatorShowCourseBell
    }

    override fun observeSimulatorShowLeadEndNotation(): Flow<Boolean> {
        return simulatorShowLeadEndNotation
    }

    override fun observeSimulatorCallFrequency(): Flow<CallFrequency> {
        return simulatorCallFrequency
    }

    override fun observeSimulatorHalfLeadSplicing(): Flow<Boolean> {
        return simulatorHalfLeadSplicing
    }

    override fun observeSimulatorUse4thsPlaceCalls(): Flow<Boolean> {
        return simulatorUse4thsPlaceCalls
    }

    override suspend fun setSimulatorShowTreble(showTreble: ExtraPathType) {
        simulatorShowTreble.value = showTreble
    }

    override suspend fun setSimulatorShowCourseBell(showCourseBell: ExtraPathType) {
        simulatorShowCourseBell.value = showCourseBell
    }

    override suspend fun setSimulatorShowLeadEndNotation(show: Boolean) {
        simulatorShowLeadEndNotation.value = show
    }

    override suspend fun setSimulatorCallFrequency(callFrequency: CallFrequency) {
        simulatorCallFrequency.value = callFrequency
    }

    override suspend fun setSimulatorHalfLeadSplicing(halfLeadSplice: Boolean) {
        simulatorHalfLeadSplicing.value = halfLeadSplice
    }

    override suspend fun setSimulatorUse4thsPlaceCalls(use: Boolean) {
        simulatorUse4thsPlaceCalls.value = use
    }
}
