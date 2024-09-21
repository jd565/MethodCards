package com.jpd.methodcards.data

import com.jpd.methodcards.domain.CallFrequency
import com.jpd.methodcards.domain.ExtraPathType
import kotlinx.coroutines.flow.Flow

interface MethodCardsPreferences {
    fun observeStage(): Flow<Int>
    suspend fun setStage(stage: Int)
    fun observeSimulatorShowTreble(): Flow<ExtraPathType>
    fun observeSimulatorShowCourseBell(): Flow<ExtraPathType>
    fun observeSimulatorShowLeadEndNotation(): Flow<Boolean>
    fun observeSimulatorCallFrequency(): Flow<CallFrequency>
    fun observeSimulatorHalfLeadSplicing(): Flow<Boolean>
    fun observeSimulatorUse4thsPlaceCalls(): Flow<Boolean>
    suspend fun setSimulatorShowTreble(showTreble: ExtraPathType)
    suspend fun setSimulatorShowCourseBell(showCourseBell: ExtraPathType)
    suspend fun setSimulatorShowLeadEndNotation(show: Boolean)
    suspend fun setSimulatorCallFrequency(callFrequency: CallFrequency)
    suspend fun setSimulatorHalfLeadSplicing(halfLeadSplice: Boolean)
    suspend fun setSimulatorUse4thsPlaceCalls(use: Boolean)
}
