package com.jpd.methodcards.data

import com.jpd.MethodProto
import com.jpd.methodcards.domain.MethodFrequency
import com.jpd.methodcards.domain.MethodSelection
import com.jpd.methodcards.domain.MethodWithCalls
import com.jpd.methodcards.domain.PlaceNotation
import kotlinx.coroutines.flow.Flow

interface MethodDao {
    fun getMethodsByStage(stage: Int): Flow<List<MethodSelection>>
    fun getSelectedMethods(): Flow<List<MethodWithCalls>>
    fun getMethod(name: String): Flow<MethodWithCalls?>
    suspend fun toggleMethodSelected(name: String)
    suspend fun toggleMethodEnabledForMultiMethod(name: String)
    suspend fun deselectAllMethodsForMultiMethod()
    suspend fun setMultiMethodFrequency(name: String, frequency: MethodFrequency)
    suspend fun setBluelineMethod(name: String)
    suspend fun incrementMethodStatistics(
        methodName: String,
        stage: Int,
        lead: Int,
        error: Boolean
    )
    suspend fun insert(methods: List<MethodProto>)
    suspend fun searchByPlaceNotation(pn: List<PlaceNotation>): List<MethodWithCalls>
    suspend fun addMethod(method: MethodWithCalls)
}
