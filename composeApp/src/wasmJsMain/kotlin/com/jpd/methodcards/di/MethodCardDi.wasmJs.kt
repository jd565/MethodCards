package com.jpd.methodcards.di

import com.jpd.methodcards.data.MethodCardsPreferences
import com.jpd.methodcards.data.MethodCardsStoragePreferences
import com.jpd.methodcards.data.MethodDao
import com.jpd.methodcards.data.SimulatorPersistence
import com.jpd.methodcards.data.SimulatorStoragePersistence
import com.jpd.methodcards.data.StorageMethodDao
import org.w3c.dom.Storage

actual object MethodCardDi {
    private lateinit var storage: Storage

    fun init(storage: Storage) {
        this.storage = storage
    }

    private val preferences by lazy {
        MethodCardsStoragePreferences(storage)
    }
    actual fun getMethodCardsPreferences(): MethodCardsPreferences = preferences

    private val dao by lazy {
        StorageMethodDao(storage)
    }
    actual fun getMethodDao(): MethodDao = dao

    private val simulatorPersistence by lazy {
        SimulatorStoragePersistence(storage)
    }
    actual fun getSimulatorPersistence(): SimulatorPersistence {
        return simulatorPersistence
    }
}
