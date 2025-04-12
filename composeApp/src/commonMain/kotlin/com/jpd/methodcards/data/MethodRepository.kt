package com.jpd.methodcards.data

import com.jpd.methodcards.di.MethodCardDi
import com.jpd.methodcards.domain.MethodFrequency
import com.jpd.methodcards.domain.MethodSelection
import com.jpd.methodcards.domain.MethodWithCalls
import com.jpd.methodcards.domain.PersistedSimulatorState
import com.jpd.methodcards.domain.PlaceNotation
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MethodRepository(
    private val preferences: MethodCardsPreferences = MethodCardDi.getMethodCardsPreferences(),
    private val dao: MethodDao = MethodCardDi.getMethodDao(),
    private val simulatorPersistence: SimulatorPersistence = MethodCardDi.getSimulatorPersistence(),
) {
    private fun observeStage(): Flow<Int> = preferences.observeStage()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMethods(): Flow<Pair<Int, List<MethodSelection>>> = observeStage().flatMapLatest { stage ->
        dao.getMethodsByStage(stage).map { stage to it }
    }

    fun observeSelectedMethods(): Flow<List<MethodWithCalls>> = dao.getSelectedMethods()

    fun observeMethod(name: String): Flow<MethodWithCalls?> = dao.getMethod(name)

    suspend fun setStage(stage: Int) {
        preferences.setStage(stage)
    }

    suspend fun selectOrDeselectMethod(method: String) {
        dao.toggleMethodSelected(method)
    }

    suspend fun setBlueLineMethod(method: String) {
        dao.setBluelineMethod(method)
    }

    suspend fun selectOrDeselectMultiMethod(method: String) {
        dao.toggleMethodEnabledForMultiMethod(method)
    }

    suspend fun deselectAllMultiMethod() {
        dao.deselectAllMethodsForMultiMethod()
    }

    suspend fun setMultiMethodFrequency(method: String, frequency: MethodFrequency) {
        dao.setMultiMethodFrequency(method, frequency)
    }

    suspend fun getSimulatorModel(): PersistedSimulatorState? {
        val stage = preferences.observeStage().first()
        return simulatorPersistence.getSimulatorModel(stage)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun persistSimulatorModel(model: PersistedSimulatorState) {
        GlobalScope.launch {
            simulatorPersistence.persistSimulatorModel(model)
        }
    }

    suspend fun incrementMethodStatistics(method: MethodWithCalls, place: Int, error: Boolean) {
        dao.incrementMethodStatistics(method.name, method.stage, place, error)
    }

    suspend fun searchByPlaceNotation(pn: PlaceNotation): MethodWithCalls? {
        return dao.searchByPlaceNotation(pn)
    }

    suspend fun addMethod(method: MethodWithCalls) {
        dao.addMethod(method)
    }
}
