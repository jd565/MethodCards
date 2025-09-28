package com.jpd.methodcards.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.jpd.methodcards.data.AppDatabase
import com.jpd.methodcards.data.SimulatorDataStoreSerializer
import com.jpd.methodcards.domain.PersistedSimulatorState
import com.jpd.methodcards.presentation.hearing.AudioPlayer
import com.jpd.methodcards.presentation.listener.AudioRecorder
import com.jpd.methodcards.presentation.listener.AudioRecorderImpl
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath

actual object MethodCardNonWebDi {
    lateinit var appContext: Context

    actual val dataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.createWithPath {
            appContext.filesDir.resolve("methods.preferences_pb").toOkioPath()
        }
    }

    actual val databaseBuilder by lazy {
        val dbFile = appContext.getDatabasePath("my_room.db")
        Room.databaseBuilder<AppDatabase>(
            context = appContext,
            name = dbFile.absolutePath,
        )
    }

    actual val simulatorDataStore: DataStore<PersistedSimulatorState> by lazy {
        val producePath = { appContext.filesDir.resolve("simulator.preferences_pb").absolutePath.toPath() }

        DataStoreFactory.create(
            storage = OkioStorage(
                fileSystem = FileSystem.SYSTEM,
                producePath = producePath,
                serializer = SimulatorDataStoreSerializer(),
            ),
            corruptionHandler = ReplaceFileCorruptionHandler { SimulatorDataStoreSerializer().defaultValue },
        )
    }

    actual fun getAudioRecorder(): AudioRecorder = AudioRecorderImpl()
    actual fun getAudioPlayer(): AudioPlayer {
        TODO()
    }
}
