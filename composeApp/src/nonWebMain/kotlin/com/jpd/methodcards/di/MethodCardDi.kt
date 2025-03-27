package com.jpd.methodcards.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.jpd.methodcards.data.AppDatabase
import com.jpd.methodcards.data.MethodCardsDataStorePreferences
import com.jpd.methodcards.data.MethodCardsPreferences
import com.jpd.methodcards.data.MethodDao
import com.jpd.methodcards.data.RoomMethodDao
import com.jpd.methodcards.data.SimulatorDataStorePersistence
import com.jpd.methodcards.data.SimulatorPersistence
import com.jpd.methodcards.data.library.MethodLibrary
import com.jpd.methodcards.data.library.MethodLibraryVersion
import com.jpd.methodcards.domain.PersistedSimulatorState
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

expect object MethodCardNonWebDi {
    val dataStore: DataStore<Preferences>
    val simulatorDataStore: DataStore<PersistedSimulatorState>
    val databaseBuilder: RoomDatabase.Builder<AppDatabase>
}

actual object MethodCardDi {
    actual fun getMethodCardsPreferences(): MethodCardsPreferences {
        return MethodCardsDataStorePreferences()
    }
    actual fun getMethodDao(): MethodDao {
        return RoomMethodDao(database.getMethodDao())
    }
    actual fun getSimulatorPersistence(): SimulatorPersistence {
        return SimulatorDataStorePersistence(MethodCardNonWebDi.simulatorDataStore)
    }
}

private val persistedMethodLibraryVersionKey = intPreferencesKey("method_library_version")
@OptIn(DelicateCoroutinesApi::class)
private val database: AppDatabase by lazy {
    MethodCardNonWebDi.databaseBuilder
        .fallbackToDestructiveMigrationOnDowngrade(true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .addCallback(
            object : RoomDatabase.Callback() {
                override fun onOpen(connection: SQLiteConnection) {
                    GlobalScope.launch {
                        if (MethodCardNonWebDi.dataStore.data.first()[persistedMethodLibraryVersionKey] != MethodLibraryVersion ||
                            database.getMethodDao().getMethodsByStage(4).first().isEmpty()
                        ) {
                            MethodLibrary().getMethods()
                            MethodCardNonWebDi.dataStore.edit { prefs ->
                                prefs[persistedMethodLibraryVersionKey] = MethodLibraryVersion
                            }
                        }
                    }
                }
            },
        )
        .build()
}
