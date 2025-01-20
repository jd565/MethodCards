package com.jpd.methodcards.data

import com.jpd.methodcards.domain.CallFrequency
import com.jpd.methodcards.domain.ExtraPathType
import kotlinx.coroutines.flow.Flow

class MethodCardsStoragePreferences : MethodCardsPreferences {
    private val stage = StorageBasedFlow(STAGE_KEY, { it.toString() }, { it?.toIntOrNull() ?: 8 })
    private val simulatorShowTreble = EnumStorageBasedFlow<ExtraPathType>(SIMULATOR_SHOW_TREBLE_KEY)
    private val simulatorShowCourseBell = EnumStorageBasedFlow(SIMULATOR_SHOW_COURSE_BELL_KEY, ExtraPathType.None)
    private val simulatorShowLeadEndNotation = BooleanStorageBasedFlow(SIMULATOR_SHOW_LEAD_END_NOTATION_KEY)
    private val simulatorCallFrequency = EnumStorageBasedFlow(SIMULATOR_CALL_FREQUENCY_KEY, CallFrequency.Regular)
    private val simulatorHalfLeadSplicing = BooleanStorageBasedFlow(SIMULATOR_HALF_LEAD_SPLICING_KEY)
    private val simulatorUse4thsPlaceCalls = BooleanStorageBasedFlow(SIMULATOR_USE_4THS_PLACE_CALLS_KEY)

    override fun observeStage(): Flow<Int> {
        return stage.flow()
    }

    override suspend fun setStage(stage: Int) {
        this.stage.value = stage
    }

    override fun observeSimulatorShowTreble(): Flow<ExtraPathType> {
        return simulatorShowTreble.flow()
    }

    override fun observeSimulatorShowCourseBell(): Flow<ExtraPathType> {
        return simulatorShowCourseBell.flow()
    }

    override fun observeSimulatorShowLeadEndNotation(): Flow<Boolean> {
        return simulatorShowLeadEndNotation.flow()
    }

    override fun observeSimulatorCallFrequency(): Flow<CallFrequency> {
        return simulatorCallFrequency.flow()
    }

    override fun observeSimulatorHalfLeadSplicing(): Flow<Boolean> {
        return simulatorHalfLeadSplicing.flow()
    }

    override fun observeSimulatorUse4thsPlaceCalls(): Flow<Boolean> {
        return simulatorUse4thsPlaceCalls.flow()
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

    companion object {
        private const val STAGE_KEY = "stage"
        private const val SIMULATOR_SHOW_TREBLE_KEY = "simulatorShowTreble"
        private const val SIMULATOR_SHOW_COURSE_BELL_KEY = "simulatorShowCourseBell"
        private const val SIMULATOR_SHOW_LEAD_END_NOTATION_KEY = "simulatorShowLeadEndNotation"
        private const val SIMULATOR_CALL_FREQUENCY_KEY = "simulatorCallFrequency"
        private const val SIMULATOR_HALF_LEAD_SPLICING_KEY = "simulatorHalfLeadSplicing"
        private const val SIMULATOR_USE_4THS_PLACE_CALLS_KEY = "simulatorUse4thsPlaceCalls"
    }
}
