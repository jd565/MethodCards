package com.jpd.methodcards.domain

import kotlinx.serialization.Serializable

@Serializable
data class PersistedSimulatorState(
    val methodNames: List<String> = emptyList(),
    val place: Int = 0,
    val leadEndPlaceCounts: List<PlaceCount> = emptyList(),
    val rowCount: Int = 0,
    val errorCount: Int = 0,
    val currentLead: LeadWithCalls? = null,
    val nextLead: LeadWithCalls? = null,
    val leadEndPlace: Int = 0,
    val index: Int = 0,
    val rows: List<RowInformation> = emptyList(),
    val use4thsPlaceCalls: Boolean = false,
    val handbellMode: Boolean = false,
    val place2: Int = 0,
) {

    @Serializable
    data class PlaceCount(
        val counts: Map<String, PlaceCountPair> = emptyMap(),
    )

    @Serializable
    data class PlaceCountPair(
        val total: Int,
        val error: Int,
    )

    @Serializable
    data class RowInformation(
        val row: List<Int> = emptyList(),
        val nextRow: List<Int> = emptyList(),
        val isLeadEnd: Boolean = false,
        val call: String? = null,
        val methodName: String? = null,
        val leadEndNotation: String? = null,
    )

    @Serializable
    data class LeadWithCalls(
        val methodName: String = "",
        val calls: List<CallAtIndex> = emptyList(),
    ) {
        @Serializable
        data class CallAtIndex(
            val index: Int = 0,
            val call: String? = null,
        )
    }
}
