package com.jpd.methodcards.presentation.simulator

import com.jpd.methodcards.domain.CallDetails
import com.jpd.methodcards.domain.CallFrequency
import com.jpd.methodcards.domain.FullMethodCall
import com.jpd.methodcards.domain.LeadWithCalls
import com.jpd.methodcards.domain.MethodWithCalls

/**
 * Generate a path through methods to get to a place with the minimum weight.
 *
 * The weight is based on the number of times a method has been rung correctly,
 * and the desired frequency of the method
 */
fun generatePath(
    placeMethodCounts: Map<Int, MutableMap<MethodWithCalls, Pair<Int, Int>>>,
    currentLead: LeadWithCalls,
    currentPlace: Int,
    callFrequency: CallFrequency,
    index: Int,
    nextCall: CallDetails?,
    use4thsPlaceCalls: Boolean,
): Pair<LeadWithCalls, LeadWithCalls> {
    val weights: List<MethodWeights> = placeMethodCounts.map { (place, map) ->
        map.map { (method, count) ->
            MethodWeights(method, place, count.first - count.second)
        }
    }.flatten()
    val currentMethod = currentLead.method

    val queue = arrayOfNulls<QueueEntry>(MethodWithCalls.AllowedStages.last() + 1)

    val min = weights.minOf { it.weight }
    val targets = weights
        .flatMap { mw ->
            if (mw.weight == min) {
                List(mw.repeat(min)) { mw }
            } else {
                emptyList()
            }
        }
    println("Looking for targets ${targets.joinToString { it.debugString() }}")
    val targetPlaces = targets.map { it.place }

    var leadEnd = currentMethod.leadEnd.row
    val currentCalls = currentLead.calls.filter { (idx, _) -> idx < index }
    var options = listOf(currentCalls)

    currentMethod.callIndexes(use4thsPlaceCalls)
        .toList()
        .filter { (idx, _) -> idx >= index }
        .sortedBy { (idx, _) -> idx }
        .forEachIndexed { callOrder, (idx, calls) ->
            val isFirstCall = callOrder == 0
            options = options.flatMap { existing ->
                val newOptions = mutableListOf<List<Pair<Int, FullMethodCall?>>>()
                if (isFirstCall && callFrequency == CallFrequency.Manual) {
                    newOptions.add(
                        existing + Pair(idx, calls.firstOrNull { it.call.name == nextCall?.name }),
                    )
                } else {
                    if (callFrequency != CallFrequency.Manual) {
                        newOptions.addAll(
                            calls.map { call ->
                                existing + Pair(idx, call)
                            },
                        )
                    }
                    if (callFrequency != CallFrequency.Always) {
                        newOptions.add(existing + Pair(idx, null))
                        newOptions.add(existing + Pair(idx, null))
                    }
                }
                newOptions
            }
        }

    options.shuffled().associate { calls ->
        val leadEnd = calls.fold(leadEnd) { le, (_, call) ->
            call?.let {
                call.leadEndTranspose.map { le[it - 1] }
            } ?: le
        }
        val lead = LeadWithCalls(
            method = currentMethod,
            calls = calls,
            leadEnd = leadEnd,
        )
        leadEnd.indexOf(currentPlace) + 1 to lead
    }.forEach { (place, lead) ->
        queue[place] = QueueEntry(
            place = place,
            cost = 0,
            paths = listOf(listOf(lead)),
        )
    }

    var minQueue: QueueEntry = queue.randomMinBy { it?.cost ?: Int.MAX_VALUE }!!
    while (minQueue.place !in targetPlaces) {
        println("Queue is ${queue.filter { it?.explored == false }.map { it?.place }}")
        println("Evaluating place ${minQueue.place}")
        val current = minQueue
        current.explored = true
        val currentPlace = minQueue.place
        val currentCost = current.cost
        val currentPath = current.paths.random()

        val next = weights.filter { it.place == currentPlace }
        next.forEach { methodWeight ->
            val method = methodWeight.method
            val place = methodWeight.place
            val weight = methodWeight.weight
            println("Evaluating for method ${method.debugName}")
            method.nextLeadOptions(callFrequency)
                .forEach { option ->
                    val nextPlace = option.leadEnd.indexOf(place) + 1
                    val qe = queue[nextPlace]
                    val newPath = List(methodWeight.repeat(min)) { currentPath + option }
                    println("Can get to $nextPlace with ${option.debugString()}. Explored? ${qe?.explored}")
                    if (qe?.explored != true) {
                        val methodCost = weight - min
                        val newCost = currentCost + methodCost
                        println("Cost $newCost")
                        if (qe == null || qe.cost > newCost) {
                            queue[nextPlace] = QueueEntry(
                                place = nextPlace,
                                cost = newCost,
                                paths = newPath,
                            )
                        } else if (qe.cost == newCost) {
                            queue[nextPlace] = QueueEntry(
                                place = nextPlace,
                                cost = newCost,
                                paths = buildList {
                                    addAll(qe.paths)
                                    addAll(newPath)
                                },
                            )
                        }
                    }
                }
        }
        minQueue = queue.randomMinBy { qe -> qe?.cost?.takeIf { !qe.explored } ?: Int.MAX_VALUE }!!
    }

    val path = minQueue.paths.random()
    println("Can get to target place ${minQueue.place} with path ${path.map { it.debugString() }}")
    return if (path.size == 1) {
        path[0] to LeadWithCalls(
            targets.filter { it.place == minQueue.place }.random().method,
            emptyList(),
            emptyList(),
        )
    } else {
        path[0] to path[1]
    }
}

private fun <T, R : Comparable<R>> Array<out T>.randomMinBy(selector: (T) -> R): T {
    val items = ArrayList<T>(size)
    var min: R? = null
    this.forEach {
        val s = selector(it)
        val m = min
        if (m == null || s < m) {
            items.clear()
            items.add(it)
            min = s
        } else if (selector(it) == min) {
            items.add(it)
        }
    }
    return items.random()
}

private fun MethodWithCalls.nextLeadOptions(
    callFrequency: CallFrequency,
): List<LeadWithCalls> {
    return leadEndOptions
        .filter { option ->
            when (callFrequency) {
                CallFrequency.Manual -> option.calls.all { it.second == null }
                CallFrequency.Regular -> true
                CallFrequency.Always -> option.calls.all { it.second != null }
            }
        }
}

private data class QueueEntry(
    val place: Int,
    val cost: Int,
    val paths: List<List<LeadWithCalls>>,
    var explored: Boolean = false,
)

private data class MethodWeights(
    val method: MethodWithCalls,
    val place: Int,
    val timesRungCorrectly: Int,
) {
    val weight = timesRungCorrectly.div(method.multiMethodFrequency.frequency).takeIf { place !in method.huntBells }
        ?: Int.MAX_VALUE

    fun repeat(minWeight: Int): Int {
        return ((minWeight + 1) * method.multiMethodFrequency.frequency - timesRungCorrectly).coerceAtLeast(1)
    }

    fun debugString(): String {
        return "[${method.debugName}, $place, $timesRungCorrectly, $weight]"
    }
}
