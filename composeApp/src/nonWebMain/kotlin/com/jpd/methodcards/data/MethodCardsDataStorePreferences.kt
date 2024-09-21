package com.jpd.methodcards.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.jpd.methodcards.di.MethodCardNonWebDi
import com.jpd.methodcards.domain.CallFrequency
import com.jpd.methodcards.domain.ExtraPathType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class MethodCardsDataStorePreferences(
    private val store: DataStore<Preferences> = MethodCardNonWebDi.dataStore,
): MethodCardsPreferences {
    private val stagePreference = intPreferencesKey("stage")
    private val simulatorShowTreblePreference = intPreferencesKey("simulator_show_treble_2")
    private val simulatorShowCourseBellPreference = intPreferencesKey("simulator_show_course_bell")
    private val simulatorShowLeadEndNotationPreference =
        booleanPreferencesKey("simulator_show_lead_end_notation")
    private val simulatorCallFrequencyPreference = intPreferencesKey("simulator_call_frequency")
    private val simulatorHalfLeadSplicePreference =
        booleanPreferencesKey("simulator_half_lead_splice")
    private val simulatorUse4thsPlaceCallsPreference =
        booleanPreferencesKey("simulator_use_4ths_place_calls")

    override fun observeStage(): Flow<Int> = store.data.map { preferences ->
        preferences[stagePreference] ?: 8
    }.distinctUntilChanged()

    override suspend fun setStage(stage: Int) {
        store.edit { preferences ->
            preferences[stagePreference] = stage
        }
    }

    override fun observeSimulatorShowTreble(): Flow<ExtraPathType> {
        return store.data.map { preferences ->
            preferences[simulatorShowTreblePreference]?.let { i ->
                ExtraPathType.entries.firstOrNull { it.ordinal == i }
            } ?: ExtraPathType.Full
        }.distinctUntilChanged()
    }

    override fun observeSimulatorShowCourseBell(): Flow<ExtraPathType> {
        return store.data.map { preferences ->
            preferences[simulatorShowCourseBellPreference]?.let { i ->
                ExtraPathType.entries.firstOrNull { it.ordinal == i }
            } ?: ExtraPathType.None
        }.distinctUntilChanged()
    }

    override fun observeSimulatorShowLeadEndNotation(): Flow<Boolean> {
        return store.data.map { preferences ->
            preferences[simulatorShowLeadEndNotationPreference] ?: false
        }.distinctUntilChanged()
    }

    override fun observeSimulatorCallFrequency(): Flow<CallFrequency> {
        return store.data.map { preferences ->
            preferences[simulatorCallFrequencyPreference]?.toCallFrequency() ?: CallFrequency.Regular
        }.distinctUntilChanged()
    }

    override fun observeSimulatorHalfLeadSplicing(): Flow<Boolean> {
        return store.data.map { preferences ->
            preferences[simulatorHalfLeadSplicePreference] ?: false
        }.distinctUntilChanged()
    }

    override fun observeSimulatorUse4thsPlaceCalls(): Flow<Boolean> {
        return store.data.map { preferences ->
            preferences[simulatorUse4thsPlaceCallsPreference] ?: false
        }.distinctUntilChanged()
    }

    override suspend fun setSimulatorShowTreble(showTreble: ExtraPathType) {
        store.edit { preferences ->
            preferences[simulatorShowTreblePreference] = showTreble.ordinal
        }
    }

    override suspend fun setSimulatorShowCourseBell(showCourseBell: ExtraPathType) {
        store.edit { preferences ->
            preferences[simulatorShowCourseBellPreference] = showCourseBell.ordinal
        }
    }

    override suspend fun setSimulatorShowLeadEndNotation(show: Boolean) {
        store.edit { preferences ->
            preferences[simulatorShowLeadEndNotationPreference] = show
        }
    }

    override suspend fun setSimulatorCallFrequency(callFrequency: CallFrequency) {
        store.edit { preferences ->
            preferences[simulatorCallFrequencyPreference] = callFrequency.toInt()
        }
    }

    override suspend fun setSimulatorHalfLeadSplicing(halfLeadSplice: Boolean) {
        store.edit { preferences ->
            preferences[simulatorHalfLeadSplicePreference] = halfLeadSplice
        }
    }

    override suspend fun setSimulatorUse4thsPlaceCalls(use: Boolean) {
        store.edit { preferences ->
            preferences[simulatorUse4thsPlaceCallsPreference] = use
        }
    }

    private fun Int.toCallFrequency(): CallFrequency = when (this) {
        0 -> CallFrequency.Manual
        1 -> CallFrequency.Regular
        2 -> CallFrequency.Always
        else -> throw IllegalArgumentException("Invalid call frequency $this")
    }

    private fun CallFrequency.toInt(): Int = when (this) {
        CallFrequency.Manual -> 0
        CallFrequency.Regular -> 1
        CallFrequency.Always -> 2
    }
}
