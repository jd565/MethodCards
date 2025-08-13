package com.jpd.methodcards.di

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
import com.jpd.methodcards.presentation.hearing.AudioPlayerImpl
import com.jpd.methodcards.presentation.listener.AudioRecorder
import com.jpd.methodcards.presentation.listener.AudioRecorderImpl
import kotlinx.cinterop.ExperimentalForeignApi
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

actual object MethodCardNonWebDi {
    @OptIn(ExperimentalForeignApi::class)
    actual val dataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.createWithPath {
            val documentDirectory: NSURL? =
                NSFileManager.defaultManager.URLForDirectory(
                    directory = NSDocumentDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = null,
                    create = false,
                    error = null,
                )
            val path = requireNotNull(documentDirectory).path + "/methods.preferences_pb"
            path.toPath()
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual val databaseBuilder by lazy {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        val dbFilePath = documentDirectory?.path!! + "/my_room.db"
        Room.databaseBuilder<AppDatabase>(
            name = dbFilePath,
        )
    }

    actual val simulatorDataStore: DataStore<PersistedSimulatorState> by lazy {
        @OptIn(ExperimentalForeignApi::class)
        val producePath = {
            val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null,
            )
            val path = requireNotNull(documentDirectory).path + "/simulator.preferences_pb"
            path.toPath()
        }

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

    actual fun getAudioPlayer(): AudioPlayer = AudioPlayerImpl()
}
