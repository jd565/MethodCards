package com.jpd.methodcards.domain

data class FullMethodCall(
    val call: CallDetails,
    val ruleoffsFrom: Int,
    val ruleoffsEvery: Int,
    val startIdx: Int,
    val baseNotation: FullNotation,
    val stage: Int,
    val plainLeadEnd: Row,
) {
    val name: String get() = call.name
    val callNotation: List<String> by lazy { call.notation.fullNotation(stage).notation }
    private val lead: Lead by lazy {
        val full = baseNotation
            .withCall(startIdx, call.notation)

        Lead(full.sequence(Row.rounds(stage)).toList())
    }

    /**
     * The lead end that occurs after calling this call
     */
    val leadEnd: Row by lazy {
        lead.lead.last()
    }

    /**
     * Transpose to apply to a plain lead to change it to a called lead
     */
    val leadEndTranspose: Row by lazy {
        Row(stage) { idx ->
            val affected = leadEnd[idx]
            plainLeadEnd.indexOf(affected) + 1
        }
    }

    val affectedBells: List<Int> by lazy {
        buildList {
            leadEndTranspose.row.forEachIndexed { idx, bell ->
                if (bell != idx + 1) {
                    add(plainLeadEnd.row[idx])
                }
            }
        }
    }

    val rows: List<Row> by lazy {
        val take = 6.coerceAtLeast(call.cover + 1)
        val from = (startIdx - take + 1).coerceAtLeast(0)
        val adjust = (startIdx - take + 1).coerceAtMost(0)
        val to = startIdx + call.cover + take + adjust
        val rows = lead.lead.subList(from, to.coerceAtMost(lead.lead.size)).toMutableList()
        val expectedSize = to - from
        if (rows.size < expectedSize) {
            baseNotation.sequence(rows.last()).drop(1)
                .take(expectedSize - rows.size)
                .forEach { row ->
                    rows.add(row)
                }
        }
        rows
    }
    val rowRuleoffsFrom: Int by lazy {
        val take = 6.coerceAtLeast(call.cover + 1)
        val from = (startIdx - take + 1).coerceAtLeast(0)
        ruleoffsFrom - from
    }
}

data class MethodSelection(
    val name: String,
    val selected: Boolean,
    val placeNotation: PlaceNotation,
)

enum class CallFrequency {
    Manual,
    Regular,
    Always,
}

enum class ExtraPathType {
    Full,
    Crossing,
    None,
}

enum class MethodFrequency(val frequency: Int) {
    Rare(1),
    Uncommon(2),
    Regular(3),
    Common(4),
    Often(5);
}

data class LeadWithCalls(
    val method: MethodWithCalls,
    val calls: List<Pair<Int, FullMethodCall?>>,
    val leadEnd: Row,
) {
    fun debugString(): String = "${method.debugName} - ${calls.joinToString { "${it.second?.name} @ ${it.first}" }}"
}

data class MethodWithCalls(
    val name: String,
    val placeNotation: PlaceNotation,
    val stage: Int,
    val ruleoffsEvery: Int,
    val ruleoffsFrom: Int,
    val classification: MethodClassification,
    val calls: List<CallDetails>,
    val enabledForMultiMethod: Boolean,
    val multiMethodFrequency: MethodFrequency,
    val enabledForBlueline: Boolean,
    val customMethod: Boolean,
) {
    val underOverNotation: Pair<PlaceNotation, PlaceNotation>? by lazy {
        if (huntBells != listOf(1)) return@lazy null
        val notationSequences = placeNotation.sequences
        if (notationSequences.size > 2) return@lazy null
        if (notationSequences[1].size > 1) return@lazy null
        val lead = leads.first().lead
        val under = mutableListOf<String>()
        val over = mutableListOf<String>()
        notationSequences[0]
            .forEachIndexed { idx, sequence ->
                if (sequence == "x") {
                    under.add("x")
                    over.add("x")
                } else {
                    val tIdx = lead[idx].indexOf(1) + 1
                    under.add(
                        sequence.filter {
                            val bellInt = bellChars.indexOf(it.toString()) + 1
                            bellInt <= tIdx
                        }.ifEmpty { "x" },
                    )
                    over.add(
                        sequence.filter {
                            val bellInt = bellChars.indexOf(it.toString()) + 1
                            bellInt > tIdx
                        }.ifEmpty { "x" },
                    )
                }
            }

        Pair(
            PlaceNotation(listOf(under, listOf("x"))),
            PlaceNotation(listOf(over, notationSequences[1])),
        )
    }

    val fullNotation by lazy { placeNotation.fullNotation(stage) }

    val leads: List<Lead> get() = fullNotation.leads
    val leadEnd: Row get() = fullNotation.leadEnd
    val changesInLead: Int get() = fullNotation.changesInLead
    val leadEndNotation: String get() = fullNotation.leadEndNotation

    val huntBells: List<Int> get() = fullNotation.huntBells
    val leadCycles: List<List<Int>> get() = fullNotation.leadCycles

    fun callIndexes(use4thsPlaceCalls: Boolean): Map<Int, List<FullMethodCall>> {
        return if (use4thsPlaceCalls) {
            iVcallIndexes
        } else {
            callIndexes
        }
    }

    private val callIndexes: Map<Int, List<FullMethodCall>> by lazy {
        val baseNotation = fullNotation
        val indexes = mutableMapOf<Int, MutableList<FullMethodCall>>()
        calls.map { call ->
            var callStartIdx = call.from - 1
            val every = call.every.takeIf { it > 0 } ?: leads[0].lead.size
            while (callStartIdx < 0) callStartIdx += every
            while (callStartIdx <= baseNotation.notation.size) {
                indexes.getOrPut(callStartIdx) { mutableListOf() }
                    .add(
                        FullMethodCall(
                            call,
                            ruleoffsFrom = ruleoffsFrom,
                            ruleoffsEvery = ruleoffsEvery,
                            startIdx = callStartIdx,
                            baseNotation = baseNotation,
                            stage = stage,
                            plainLeadEnd = leadEnd,
                        ),
                    )
                callStartIdx += every
            }
        }
        indexes
    }

    private val iVcallIndexes: Map<Int, List<FullMethodCall>> by lazy {
        val baseNotation = fullNotation
        val indexes = mutableMapOf<Int, MutableList<FullMethodCall>>()
        calls.map { call ->
            val notation = call.notation.fullNotation(stage).notation
            if (notation.size == 1) {
                when (call.name) {
                    "Bob" -> call.copy(notation = PlaceNotation("14"))
                    "Single" -> call.copy(notation = PlaceNotation("1234"))
                    else -> call
                }
            } else {
                call
            }
        }
            .map { call ->
                var callStartIdx = call.from - 1
                while (callStartIdx < 0) callStartIdx += call.every
                while (callStartIdx <= baseNotation.notation.size) {
                    indexes.getOrPut(callStartIdx) { mutableListOf() }
                        .add(
                            FullMethodCall(
                                call,
                                ruleoffsFrom = ruleoffsFrom,
                                ruleoffsEvery = ruleoffsEvery,
                                startIdx = callStartIdx,
                                baseNotation = baseNotation,
                                stage = stage,
                                plainLeadEnd = leadEnd,
                            ),
                        )
                    callStartIdx += call.every
                }
            }
        indexes
    }

    val debugName: String get() = shortName(emptyList())

    fun nameWithoutStage(): String {
        return name.removeSuffix(" ${stageName(stage)}")
    }

    fun shortName(methods: Collection<MethodWithCalls>): String {
        var final = name
        if (methods.any { it.stage != stage }) return final
        val stageName = stageName(stage)
        if (final.endsWith(stageName)) {
            final = final.substring(0, final.length - stageName.length - 1)
        }

        if (methods.any { it.classification != classification }) return final
        if (classification.part.isNotBlank() && final.endsWith(classification.part)) {
            final = final.substring(0, final.length - classification.part.length - 1)
        }
        return final
    }

    val leadEndOptions: List<LeadWithCalls> by lazy {
        callIndexes.entries.sortedBy { it.key }
            .fold(listOf<List<Pair<Int, FullMethodCall?>>>()) { options, (idx, calls) ->
                if (options.isEmpty()) {
                    buildList {
                        addAll(calls.map { call -> listOf(Pair(idx, call)) })
                        add(listOf(Pair(idx, null)))
                        add(listOf(Pair(idx, null)))
                    }
                } else {
                    options.flatMap { existing ->
                        buildList {
                            addAll(calls.map { call -> existing + Pair(idx, call) })
                            add(existing + Pair(idx, null))
                            add(existing + Pair(idx, null))
                        }
                    }
                }
            }.map { calls ->
                val le = leadEnd
                calls.fold(leadEnd) { le, (idx, call) ->
                    call?.let {
                        call.leadEndTranspose.map { le[it - 1] }
                    } ?: le
                }
                LeadWithCalls(
                    this,
                    calls.sortedBy { it.first },
                    le,
                )
            }
    }

    companion object {
        val AllowedStages = (4..16).toList()

        fun stageName(stage: Int) = when (stage) {
            4 -> "Minimus"
            5 -> "Doubles"
            6 -> "Minor"
            7 -> "Triples"
            8 -> "Major"
            9 -> "Caters"
            10 -> "Royal"
            11 -> "Cinques"
            12 -> "Maximus"
            13 -> "Sextuples"
            14 -> "Fourteen"
            15 -> "Septuples"
            16 -> "Sixteen"
            else -> error("Invalid stage: $stage")
        }

        fun fromPlaceNotation(
            name: String,
            stage: Int,
            placeNotation: PlaceNotation,
        ): MethodWithCalls {
            val fullNotation = placeNotation.fullNotation(stage)
            val classification = fullNotation.classification.classification
            val methodName = if (name.endsWith(stageName(stage))) {
                name
            } else {
                buildList {
                    add(name)
                    if (classification.part.isNotEmpty()) {
                        add(classification.part)
                    }
                    add(stageName(stage))
                }.joinToString(" ")
            }
            return MethodWithCalls(
                name = methodName,
                placeNotation = placeNotation,
                stage = stage,
                ruleoffsFrom = 0,
                ruleoffsEvery = fullNotation.changesInLead,
                classification = classification,
                calls = emptyList(), //TODO
                enabledForMultiMethod = false,
                multiMethodFrequency = MethodFrequency.Regular,
                enabledForBlueline = false,
                customMethod = true,
            )
        }
    }
}
