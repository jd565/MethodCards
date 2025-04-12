package com.jpd.methodcards.presentation.simulator

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jpd.methodcards.domain.CallDetails
import com.jpd.methodcards.domain.CallFrequency
import com.jpd.methodcards.domain.ExtraPathType
import com.jpd.methodcards.domain.LeadWithCalls
import com.jpd.methodcards.domain.MethodWithCalls
import com.jpd.methodcards.domain.PersistedSimulatorState
import com.jpd.methodcards.domain.Row
import kotlin.collections.set

@Immutable
internal data class RowInformation(
    private val row: Row,
    val placeIndex: Int,
    val place2Index: Int,
    val trebleIndex: Int,
    val courseBell1Index: Int,
    val courseBell2Index: Int,
    val nextRow: Row,
    val nextPlaceIndex: Int,
    val nextPlace2Index: Int,
    val isLeadEnd: Boolean,
    val call: String?,
    val leadEndNotation: String?,
) {
    fun persist() = PersistedSimulatorState.RowInformation(
        row.row.toList(),
        nextRow.row.toList(),
        isLeadEnd,
        call,
        leadEndNotation,
    )
}

@Stable
internal class SimulatorState private constructor(
    private val methods: List<MethodWithCalls>,
    persistedState: PersistedSimulatorState?,
    private val updateStatistics: (MethodWithCalls, Int, Boolean) -> Unit,
    private val persistState: (PersistedSimulatorState) -> Unit,
    private val use4thsPlaceCalls: Boolean,
    val handbellMode: Boolean,
) {
    constructor(
        methods: List<MethodWithCalls>,
        updateStatistics: (MethodWithCalls, Int, Boolean) -> Unit,
        persistState: (PersistedSimulatorState) -> Unit,
        use4thsPlaceCalls: Boolean,
        handbellMode: Boolean,
    ) : this(
        methods = methods,
        persistedState = null,
        updateStatistics = updateStatistics,
        use4thsPlaceCalls = use4thsPlaceCalls,
        persistState = persistState,
        handbellMode = handbellMode,
    )

    constructor(
        methods: List<MethodWithCalls>,
        persistedState: PersistedSimulatorState,
        updateStatistics: (MethodWithCalls, Int, Boolean) -> Unit,
        persistState: (PersistedSimulatorState) -> Unit,
    ) : this(
        methods = persistedState.methodNames.map { name -> methods.first { m -> m.name == name } },
        persistedState = persistedState,
        updateStatistics = updateStatistics,
        use4thsPlaceCalls = persistedState.use4thsPlaceCalls,
        persistState = persistState,
        handbellMode = persistedState.handbellMode,
    )

    private val place: Int = persistedState?.place ?: methods.random().leadCycles.flatten().random()
    private val place2: Int = persistedState?.place2 ?: run {
        var p: Int
        do {
            p = methods.random().leadCycles.flatten().random()
        } while (p == place)
        p
    }
    val placeMethodCounts: Map<Int, MutableMap<MethodWithCalls, Pair<Int, Int>>> =
        persistedState?.placeMethodCounts(methods) ?: buildMap {
            methods.forEach { method ->
                repeat(method.stage) { b ->
                    getOrPut(b + 1) { mutableMapOf() }.put(method, Pair(0, 0))
                }
            }
        }

    val stage = methods.maxOf { it.stage }
    val rowCount: Int get() = _rowCount
    private var _rowCount by mutableIntStateOf(persistedState?.rowCount ?: 0)
    val errorCount: Int get() = _errorCount
    private var _errorCount by mutableIntStateOf(persistedState?.errorCount ?: 0)
    private var leads: Pair<LeadWithCalls, LeadWithCalls> by mutableStateOf(
        persistedState?.let {
            Pair(
                it.currentLead!!.toLeadWithCall(methods),
                it.nextLead!!.toLeadWithCall(methods),
            )
        } ?: chooseRandomMethod(),
    )
    val method: MethodWithCalls get() = leads.first.method
    private var leadEndPlace: Int = persistedState?.leadEndPlace ?: place
    private var idx = persistedState?.index ?: 0
    private val _calls = mutableStateListOf<CallDetails>()
    val calls: List<CallDetails> get() = _calls
    private var _nextCall by mutableStateOf<CallDetails?>(null)
    val nextCall: CallDetails? get() = _nextCall
    val showTrebleLine: ExtraPathType get() = _showTrebleLine.value
    val showCourseBell: ExtraPathType get() = _showCourseBell.value
    private val _callFrequency = mutableStateOf(CallFrequency.Regular)
    val callFrequency: CallFrequency get() = _callFrequency.value
    private val _showTrebleLine = mutableStateOf(ExtraPathType.Full)
    private val _showCourseBell = mutableStateOf(ExtraPathType.None)
    private val _showLeadEndNotation = mutableStateOf(false)
    private var courseBells: Pair<Int, Int> = Pair(0, 0)
    val showLeadEndNotation: Boolean get() = _showLeadEndNotation.value
    val rows = persistedState?.snapshotStateRows() ?: mutableStateListOf(firstRow())

    private var madeErrorThisSection = false

    private fun advance(delta: Int, delta2: Int?): Boolean {
        if (handbellMode) {
            check(delta2 != null)
        }

        val ci = rows.last()
        val current = ci.placeIndex
        val next = ci.nextPlaceIndex
        val bell1ok = next == current + delta
        val bell2ok = !handbellMode || ci.nextPlace2Index == ci.place2Index + delta2!!
        return if (bell1ok && bell2ok) {
            if (rows.size > 100) {
                rows.removeAt(0)
            }
            rows.add(next())
            _rowCount++
            true
        } else {
            if (!madeErrorThisSection) {
                madeErrorThisSection = true
                val (counts, place) = placeMethodCounts to leadEndPlace
                val stats = counts[place]!![method]!!
                counts[place]!![method] = Pair(stats.first, stats.second + 1)
            }
            _errorCount++
            false
        }
    }

    fun move(bell1: Int, bell2: Int?): Boolean = advance(bell1, bell2)

    fun updateShowTreble(showTreble: ExtraPathType) {
        _showTrebleLine.value = showTreble
    }

    fun updateShowCourseBell(showCourseBell: ExtraPathType) {
        if (!handbellMode) {
            _showCourseBell.value = showCourseBell
        }
    }

    fun updateShowLeadEndNotation(showLeadEndNotation: Boolean) {
        _showLeadEndNotation.value = showLeadEndNotation
    }

    fun updateCallFrequency(callFrequency: CallFrequency) {
        this._callFrequency.value = callFrequency
    }

    fun makeCall(call: CallDetails) {
        _nextCall = if (nextCall == call) null else call
    }

    fun cacheMethods() {
        methods.forEach { method ->
            method.leadEnd
            method.leadEndOptions
            method.callIndexes(use4thsPlaceCalls).forEach { (_, calls) ->
                calls.forEach {
                    it.leadEndTranspose
                }
            }
        }
    }

    fun persist(): PersistedSimulatorState {
        return PersistedSimulatorState(
            methodNames = methods.map { it.name },
            place = place,
            leadEndPlaceCounts = placeMethodCounts.entries
                .sortedBy { it.key }
                .map { (_, places) ->
                    PersistedSimulatorState.PlaceCount(
                        buildMap {
                            places.forEach { (method, counts) ->
                                this[method.name] = PersistedSimulatorState.PlaceCountPair(counts.first, counts.second)
                            }
                        },
                    )
                },
            rowCount = rowCount,
            errorCount = errorCount,
            currentLead = leads.first.persist(),
            nextLead = leads.second.persist(),
            leadEndPlace = leadEndPlace,
            index = idx,
            rows = rows.map { it.persist() },
            use4thsPlaceCalls = use4thsPlaceCalls,
        )
    }

    private fun next(): RowInformation {
        if (idx == 0) {
            updateStatistics(method, leadEndPlace, madeErrorThisSection)

            leadEndPlace = rows.last().nextPlaceIndex + 1
            leads = Pair(leads.second, leads.first)
            println("Now ringing place $leadEndPlace for ${method.debugName}")
            val stats = placeMethodCounts[leadEndPlace]!![method]!!
            placeMethodCounts[leadEndPlace]!![method] = Pair(stats.first + 1, stats.second)
            madeErrorThisSection = false
            calculateCourseBells(rows.last().nextRow, leadEndPlace)
            _calls.clear()
            _calls.addAll(method.calls)
        }
        if (idx == 1) {
            persistState(persist())
        }
        val notation = method.fullNotation.notation

        val (callDisplay, leadEndNotation) = when {
            idx == notation.size - 1 -> {
                leads = generatePath(
                    placeMethodCounts,
                    leads.first,
                    leadEndPlace,
                    callFrequency,
                    idx + 1,
                    nextCall,
                    use4thsPlaceCalls,
                )
                if (leads.second.method != leads.first.method) {
                    methodCall(leads.second.method)
                } else {
                    null to null
                }
            }

            (idx + 1) in method.callIndexes(use4thsPlaceCalls) -> {
                leads = generatePath(
                    placeMethodCounts,
                    leads.first,
                    leadEndPlace,
                    callFrequency,
                    idx + 1,
                    nextCall,
                    use4thsPlaceCalls,
                )
                _nextCall = null
                leads.first.calls.firstOrNull { it.first == idx + 1 }?.second?.name to null
            }

            else -> null to null
        }

        val activeCall = leads.first.calls.findLast { it.first <= idx }?.second
        val pn = if (activeCall != null && idx - activeCall.startIdx <= activeCall.callNotation.lastIndex) {
            activeCall.callNotation[idx - activeCall.startIdx]
        } else {
            notation[idx]
        }

        val currentRow = rows.last().nextRow
        val nextRow = currentRow.nextRow(pn, method.stage)

        if (rows.size > 100) {
            rows.removeAt(0)
        }

        val isLeadEnd = idx == 0

        idx++
        if (idx == notation.size) {
            idx = 0
        }

        return RowInformation(
            row = currentRow,
            placeIndex = currentRow.indexOf(place),
            place2Index = currentRow.indexOf(place2),
            trebleIndex = currentRow.indexOf(1),
            courseBell1Index = currentRow.indexOf(courseBells.first),
            courseBell2Index = currentRow.indexOf(courseBells.second),
            nextRow = nextRow,
            nextPlaceIndex = nextRow.indexOf(place),
            nextPlace2Index = nextRow.indexOf(place2),
            isLeadEnd = isLeadEnd,
            call = callDisplay,
            leadEndNotation = leadEndNotation,
        )
    }

    private fun firstRow(): RowInformation {
        val notation = method.fullNotation.notation

        placeMethodCounts[leadEndPlace]!![method] = Pair(1, 0)
        madeErrorThisSection = false
        _calls.clear()
        _calls.addAll(method.calls)

        val pn = notation[idx]
        val currentRow = Row.rounds(stage)
        val nextRow = currentRow.nextRow(pn, method.stage)

        val (methodName, leadEndNotation) = if (methods.size != 1) methodCall(method) else null to null
        calculateCourseBells(currentRow, leadEndPlace)

        idx++
        return RowInformation(
            row = currentRow,
            placeIndex = currentRow.indexOf(place),
            place2Index = currentRow.indexOf(place2),
            trebleIndex = currentRow.indexOf(1),
            courseBell1Index = currentRow.indexOf(courseBells.first),
            courseBell2Index = currentRow.indexOf(courseBells.second),
            nextRow = nextRow,
            nextPlaceIndex = nextRow.indexOf(place),
            nextPlace2Index = nextRow.indexOf(place2),
            isLeadEnd = true,
            call = methodName,
            leadEndNotation = leadEndNotation,
        )
    }

    private fun methodCall(method: MethodWithCalls): Pair<String, String> {
        return method.shortName(methods) to method.leadEndNotation
    }

    private fun chooseRandomMethod(): Pair<LeadWithCalls, LeadWithCalls> {
        val weightedMethods = methods
            .filter { it.stage >= place && it.stage >= place2 }
            .flatMap {
                List(it.multiMethodFrequency.frequency) { _ -> it }
            }
        return Pair(
            weightedMethods.random().leadEndOptions.random(),
            weightedMethods.random().leadEndOptions.random(),
        )
    }

    private fun calculateCourseBells(row: Row, leadEndPlace: Int) {
        var lower = leadEndPlace - 2
        if (lower <= method.huntBells.size) {
            lower = method.huntBells.size + 1
            if (lower == leadEndPlace) {
                lower++
            }
        }
        var upper = leadEndPlace + 2
        if (upper > method.stage) {
            upper = method.stage
            if (upper == leadEndPlace) {
                upper--
            }
        }
        val courseBell1 = if (leadEndPlace.mod(2) == 0) {
            lower
        } else {
            upper
        }
        val courseBell2 = if (leadEndPlace.mod(2) == 0) {
            upper
        } else {
            lower
        }
        courseBells = Pair(
            row[courseBell1 - 1],
            row[courseBell2 - 1],
        )
    }
}

private fun PersistedSimulatorState.placeMethodCounts(methods: List<MethodWithCalls>): Map<Int, MutableMap<MethodWithCalls, Pair<Int, Int>>> {
    val result = mutableMapOf<Int, MutableMap<MethodWithCalls, Pair<Int, Int>>>()
    leadEndPlaceCounts.forEachIndexed { idx, counts ->
        val r = mutableMapOf<MethodWithCalls, Pair<Int, Int>>()
        result[idx + 1] = r
        counts.counts.forEach { (name, v) ->
            r[methods.first { it.name == name }] = Pair(v.total, v.error)
        }
    }
    return result
}

private fun PersistedSimulatorState.snapshotStateRows(): SnapshotStateList<RowInformation> {
    return SnapshotStateList<RowInformation>().also { list ->
        rows.forEach {
            list.add(
                RowInformation(
                    Row(it.row.toIntArray()),
                    it.row.indexOf(place),
                    it.row.indexOf(place2),
                    it.row.indexOf(1),
                    -1,
                    -1,
                    Row(it.nextRow.toIntArray()),
                    it.nextRow.indexOf(place),
                    it.nextRow.indexOf(place2),
                    it.isLeadEnd,
                    it.call,
                    it.leadEndNotation,
                ),
            )
        }
    }
}

private fun PersistedSimulatorState.LeadWithCalls.toLeadWithCall(methods: List<MethodWithCalls>): LeadWithCalls {
    val method = methods.first { it.name == methodName }
    val calls = calls.map { call ->
        if (call.call == null) {
            Pair(call.index, null)
        } else {
            Pair(call.index, method.callIndexes(false)[call.index]!!.first { c -> c.name == call.call })
        }
    }
    return LeadWithCalls(
        method,
        calls,
        Row(intArrayOf()),
    )
}

private fun LeadWithCalls.persist() = PersistedSimulatorState.LeadWithCalls(
    methodName = method.name,
    calls = calls.map { PersistedSimulatorState.LeadWithCalls.CallAtIndex(it.first, it.second?.name) },
)
