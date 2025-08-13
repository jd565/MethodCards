package com.jpd.methodcards.data

import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.w3c.dom.Storage
import org.w3c.dom.get
import org.w3c.dom.set

class StorageBasedFlow<T> private constructor(
    private val key: String,
    private val storage: Storage,
    private val serializer: (T) -> String,
    private val flow: MutableStateFlow<T>,
) {
    constructor(
        key: String,
        serializer: (T) -> String,
        deserializer: (String?) -> T,
        storage: Storage = localStorage,
    ) : this(
        key,
        storage,
        serializer,
        MutableStateFlow(deserializer(storage[key]))
    )
    var value: T
        get() = flow.value
        set(value) {
            flow.value = value
            storage[key] = serializer(value)
        }

    fun update(value: (T) -> T) {
        flow.update {
            val new = value(it)
            storage[key] = serializer(new)
            new
        }
    }
    fun flow() = flow
}

fun IntStorageBasedFlow(
    key: String,
    default: Int = 0,
    storage: Storage = localStorage,
): StorageBasedFlow<Int> = StorageBasedFlow(
    key,
    { it.toString() },
    { it?.toIntOrNull() ?: default },
    storage,
)

fun BooleanStorageBasedFlow(
    key: String,
    default: Boolean = false,
    storage: Storage = localStorage,
): StorageBasedFlow<Boolean> = StorageBasedFlow(
    key,
    { it.toString() },
    { it?.toBooleanStrictOrNull() ?: default },
    storage,
)

inline fun <reified T: Enum<T>> EnumStorageBasedFlow(
    key: String,
    default: T = enumValues<T>().first(),
    storage: Storage = localStorage,
): StorageBasedFlow<T> = StorageBasedFlow(
    key,
    { it.name },
    { value -> enumValues<T>().firstOrNull { it.name == value } ?: default },
    storage,
)
