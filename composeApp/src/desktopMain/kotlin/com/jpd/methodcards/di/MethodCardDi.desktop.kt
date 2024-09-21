package com.jpd.methodcards.di

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jpd.methodcards.data.AppDatabase
import com.jpd.methodcards.data.SimulatorDataStoreSerializer
import com.jpd.methodcards.domain.PersistedSimulatorStates
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath
import java.io.File

actual object MethodCardNonWebDi {
    private val appDir by lazy {
        File(System.getProperty("java.io.tmpdir"), "methodcards").also { it.mkdirs() }
    }
    actual val dataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.createWithPath {
            appDir.resolve("methods.preferences_pb").toOkioPath()
        }
    }
    actual val simulatorDataStore: DataStore<PersistedSimulatorStates> by lazy {
        val producePath = { appDir.resolve("simulator.preferences_pb").absolutePath.toPath() }

        DataStoreFactory.create(
            storage = OkioStorage(
                fileSystem = FileSystem.SYSTEM,
                producePath = producePath,
                serializer = SimulatorDataStoreSerializer(),
            ),
            corruptionHandler = ReplaceFileCorruptionHandler { SimulatorDataStoreSerializer().defaultValue },
        )
    }

    actual val databaseBuilder: RoomDatabase.Builder<AppDatabase> by lazy {
        val dbFile = appDir.resolve("my_room.db")
        Room.databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath,
        )
    }
}
