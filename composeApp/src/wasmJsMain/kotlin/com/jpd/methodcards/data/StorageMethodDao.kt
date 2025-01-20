package com.jpd.methodcards.data

import com.jpd.methodcards.domain.CallDetails
import com.jpd.methodcards.domain.MethodFrequency
import com.jpd.methodcards.domain.MethodSelection
import com.jpd.methodcards.domain.MethodWithCalls
import com.jpd.methodcards.domain.PlaceNotation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlin.collections.set

class StorageMethodDao : MethodDao {
    private val methodsByStage = MutableStateFlow(emptyMap<Int, List<MethodSelection>>())
    private val methodsByName = MutableStateFlow(emptyMap<String, MethodWithCalls>())
    private var magicByName = emptyMap<String, Int>()

    // private val selectedMethods = MutableStateFlow(LinkedHashSet(
    //     // storage.getItem(SELECTED_METHODS_KEY)?.split(",") ?: emptyList()
    //     emptyList<String>()
    // ))
    // private val multiMethodEnabledMethods = MutableStateFlow(
    //     // storage.getItem(MULTI_METHOD_ENABLED_METHODS_KEY)?.split(",")?.toSet() ?: emptySet()
    //     emptySet<String>()
    // )
    //     private val multiMethodFrequency = MutableStateFlow(
    //         // storage.getItem(MULTI_METHOD_FREQUENCY_KEY)?.split(",")?.associate {
    //         //     val (name, frequency) = it.split(":")
    //         //     name to MethodFrequency.valueOf(frequency)
    //         // } ?: emptyMap()
    //         emptyMap<String, MethodFrequency>()
    //     )
    // private val blueLineMethod = MutableStateFlow(
    //     // storage.getItem(BLUE_LINE_METHOD_KEY) ?: ""
    //     ""
    // )

    private val selectedMethods = StorageBasedFlow(
        SELECTED_METHODS_KEY,
        { it.joinToString(",") },
        { it?.split(",")?.toCollection(LinkedHashSet()) ?: LinkedHashSet() },
    )
    private val multiMethodEnabledMethods = StorageBasedFlow(
        MULTI_METHOD_ENABLED_METHODS_KEY,
        { it.joinToString(",") },
        { it?.split(",")?.toSet() ?: emptySet() },
    )
    private val multiMethodFrequency = StorageBasedFlow(
        MULTI_METHOD_FREQUENCY_KEY,
        { it.entries.joinToString(",") { (name, frequency) -> "$name:$frequency" } },
        {
            it?.split(",")?.associate { str ->
                val (name, frequency) = str.split(":")
                name to MethodFrequency.valueOf(frequency)
            } ?: emptyMap()
        },
    )
    private val blueLineMethod = StorageBasedFlow(BLUE_LINE_METHOD_KEY, { it }, { it ?: "" })

    override fun getMethodsByStage(stage: Int): Flow<List<MethodSelection>> {
        return combine(
            methodsByStage.map { it[stage] },
            selectedMethods.flow(),
        ) { methods, selected ->
            val out = mutableListOf<MethodSelection>()
            var selectedCount = 0
            methods?.forEach { m ->
                if (m.name in selected) {
                    out.add(selectedCount, m.copy(selected = true))
                    selectedCount++
                } else {
                    out.add(m)
                }
            }
            out
        }
    }

    override fun getSelectedMethods(): Flow<List<MethodWithCalls>> {
        return combine(
            selectedMethods.flow(),
            methodsByName,
            multiMethodEnabledMethods.flow(),
            multiMethodFrequency.flow(),
            blueLineMethod.flow(),
        ) { selected, all, mmeSet, mmfMap, bleStr ->
            selected.mapNotNull { name ->
                // val mme = name in mmeSet
                // val mmf = mmfMap[name] ?: MethodFrequency.Regular
                // val ble = name == bleStr
                // if (mme || ble || mmf != MethodFrequency.Regular) {
                //     all[name]?.copy(
                //         enabledForMultiMethod = mme,
                //         multiMethodFrequency = mmf,
                //         enabledForBlueline = ble,
                //     )
                // } else {
                    all[name]
                // }
            }
        }
    }

    override fun getMethod(name: String): Flow<MethodWithCalls?> {
        return combine(
            methodsByName,
            multiMethodEnabledMethods.flow(),
            multiMethodFrequency.flow(),
            blueLineMethod.flow(),
        ) { all, mmeSet, mmfMap, bleStr ->
            val mme = name in mmeSet
            val mmf = mmfMap[name] ?: MethodFrequency.Regular
            val ble = name == bleStr
            if (mme || ble || mmf != MethodFrequency.Regular) {
                all[name]?.copy(
                    enabledForMultiMethod = mme,
                    multiMethodFrequency = mmf,
                    enabledForBlueline = ble,
                )
            } else {
                all[name]
            }
        }
    }

    override suspend fun toggleMethodSelected(name: String) {
        selectedMethods.update { selected ->
            if (name in selected) {
                val new = LinkedHashSet(selected)
                new.remove(name)
                new
            } else {
                // This ensures that the selected methods are sorted by magic number
                val mbn = magicByName
                val newMagic = mbn[name] ?: Int.MIN_VALUE
                val new = LinkedHashSet<String>(selected.size + 1)
                var added = false
                selected.forEach { existing ->
                    if (!added) {
                        val magic = mbn[existing] ?: Int.MIN_VALUE
                        if (newMagic > magic || (newMagic == magic && name < existing)) {
                            new.add(name)
                            added = true
                        }
                    }
                    new.add(existing)
                }
                if (!added) {
                    new.add(name)
                }
                new
            }
        }
    }

    override suspend fun toggleMethodEnabledForMultiMethod(name: String) {
        multiMethodEnabledMethods.update { enabled ->
            if (name in enabled) {
                enabled - name
            } else {
                enabled + name
            }
        }
    }

    override suspend fun deselectAllMethodsForMultiMethod() {
        multiMethodEnabledMethods.value = emptySet()
    }

    override suspend fun setMultiMethodFrequency(name: String, frequency: MethodFrequency) {
        multiMethodFrequency.update { frequencies ->
            frequencies + (name to frequency)
        }
    }

    override suspend fun setBluelineMethod(name: String) {
        blueLineMethod.value = name
    }

    override suspend fun incrementMethodStatistics(methodName: String, stage: Int, lead: Int, error: Boolean) {
        // TODO("Not yet implemented")
    }

    override suspend fun insert(methods: List<MethodProto>) {
        val byName = mutableMapOf<String, MethodWithCalls>()
        val byStage = mutableMapOf<Int, MutableList<MethodSelection>>()
        val magic = mutableMapOf<String, Int>()
        methods.forEach { method ->
            byStage.getOrPut(method.stage) { mutableListOf() }
                .add(MethodSelection(method.name, false, PlaceNotation(method.notation)))
            byName[method.name] = method.toDomain()
            magic[method.name] = method.magic
        }
        methodsByName.value = byName
        methodsByStage.value = byStage
        magicByName = magic
    }

    companion object {
        private const val SELECTED_METHODS_KEY = "selectedMethods"
        private const val MULTI_METHOD_ENABLED_METHODS_KEY = "multiMethodEnabledMethods"
        private const val MULTI_METHOD_FREQUENCY_KEY = "multiMethodFrequency"
        private const val BLUE_LINE_METHOD_KEY = "blueLineMethod"
    }
}

private fun MethodProto.toDomain(): MethodWithCalls {
    return MethodWithCalls(
        name = name,
        placeNotation = PlaceNotation(notation),
        stage = stage,
        ruleoffsFrom = ruleoffsFrom,
        ruleoffsEvery = ruleoffsEvery,
        calls = calls.map { it.value.toDomain(name, it.key) },
        classification = classification,
        enabledForMultiMethod = false,
        multiMethodFrequency = MethodFrequency.Regular,
        enabledForBlueline = false,
    )
}

private fun MethodProto.CallProto.toDomain(methodName: String, callName: String): CallDetails {
    return CallDetails(
        methodName = methodName,
        name = callName,
        symbol = symbol,
        notation = PlaceNotation(notation),
        from = from,
        every = every,
        cover = cover,
    )
}
