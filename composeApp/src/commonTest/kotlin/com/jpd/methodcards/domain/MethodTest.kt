package com.jpd.methodcards.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class MethodTest {
    private fun Method(
        name: String,
        placeNotation: String,
        stage: Int,
    ) = MethodWithCalls(
        name,
        PlaceNotation(placeNotation),
        stage,
        stage,
        0,
        classification = MethodClassification.TreblePlace,
        emptyList(),
        enabledForMultiMethod = false,
        multiMethodFrequency = MethodFrequency.Regular,
        enabledForBlueline = false,
        customMethod = false,
    )

    @Test
    fun testGrandsireTriplesBob() {
        val grandsire = Method(
            name = "Grandsire Triples",
            placeNotation = "3,1.7.1.7.1.7.1",
            stage = 7,
        ).copy(
            calls = listOf(
                CallDetails(
                    methodName = "Grandsire Triples",
                    name = "Bob",
                    symbol = "-",
                    notation = PlaceNotation("3.1"),
                    from = -1,
                    every = 14,
                ),
            ),
        )

        grandsire.callIndexes(false)
            .forEach { (_, list) ->
                list.forEach { call ->
                    call.leadEnd
                    call.callNotation
                }
            }
    }

    @Test
    fun testCambridgeBristolOverUnder() {
        val cam = Method(
            name = "Cambridge Surprise Major",
            placeNotation = "x38x14x1258x36x14x58x16x78,12",
            stage = 8,
        )
        val bris = Method(
            name = "Bristol Surprise Major",
            placeNotation = "x58x14.58x58.36.14x14.58x14x18,18",
            stage = 8,
        )

        val new = cam.underOverNotation!!.first.merge(bris.underOverNotation!!.second)

        val expected = "x58x14.58.12.58.36x14x58x16x78,18"

        assertEquals(
            expected,
            new?.asString(),
        )
    }

    @Test
    fun testCambridgeOverUnder() {
        val method =
            Method(
                name = "Cambridge Surprise Maximus",
                placeNotation = "-3T-14-125T-36-147T-58-169T-70-18-9T-10-ET,12",
                stage = 12,
            )
        val under = "x.x.x1x12x3x14x5x16x7x18x9x10xET,x"
        val over = "x3Tx4x5Tx6x7Tx8x9Tx0x.x.xTx.x.x.x,12"

        val actual = method.underOverNotation!!
        assertEquals(
            Pair(PlaceNotation(under), PlaceNotation(over)),
            actual,
        )

        assertEquals(
            method.placeNotation,
            actual.first.merge(actual.second),
        )
    }

    @Test
    fun testCambridgeMajor() {
        val method =
            Method(
                name = "Cambridge Surprise Major",
                placeNotation = "-38-14-1258-36-14-58-16-78,12",
                stage = 8,
            )
        assertEquals(
            "15738264",
            method.leads
                .first()
                .lead
                .last()
                .row
                .joinToString(separator = "") { it.toBellChar() },
        )
        assertEquals(7, method.leads.size)
    }

    @Test
    fun testCambridgeMax() {
        val method =
            Method(
                name = "Cambridge Surprise Maximus",
                placeNotation = "-3T-14-125T-36-147T-58-169T-70-18-9T-10-ET,12",
                stage = 12,
            )
        assertEquals(
            "157392E4T608",
            method.leads
                .first()
                .lead
                .last()
                .row
                .joinToString(separator = "") { it.toBellChar() },
        )
        assertEquals(11, method.leads.size)
    }

    @Test
    fun testCambridgeSixteen() {
        val method =
            Method(
                name = "Cambridge Surprise Sixteen",
                placeNotation = "-3D-14-125D-36-147D-58-169D-70-18ED-9T-10AD-EB-1T-AD-1B-CD,12",
                stage = 16,
            )
        assertEquals(
            "157392E4A6C8D0BT",
            method.leads
                .first()
                .lead
                .last()
                .row
                .joinToString(separator = "") { it.toBellChar() },
        )
        assertEquals(15, method.leads.size)
    }

    @Test
    fun testGrandsireDoubles() {
        val method =
            Method(
                name = "Grandsire Doubles",
                placeNotation = "3,1.5.1.5.1",
                stage = 5,
            )
        assertEquals(
            "12534",
            method.leads
                .first()
                .lead
                .last()
                .row
                .joinToString(separator = "") { it.toBellChar() },
        )
        assertEquals(3, method.leads.size)
    }

    @Test
    fun testBaldrickTriples() {
        val method =
            Method(
                name = "Baldrick Triples",
                placeNotation = "7.1.7.147,127",
                stage = 7,
            )
        assertEquals(
            "1647253",
            method.leads
                .first()
                .lead
                .last()
                .row
                .joinToString(separator = "") { it.toBellChar() },
        )
        assertEquals(3, method.leads.size)
        assertEquals(listOf(1), method.huntBells)
        assertEquals(
            listOf(
                listOf(2, 5, 6),
                listOf(3, 7, 4),
            ),
            method.leadCycles,
        )
    }

    @Test
    fun testCloisterTriples() {
        val method =
            Method(
                name = "Cloister Triples",
                placeNotation = "3.1.7.1.3.1",
                stage = 7,
            )
        assertEquals(
            "4236175",
            method.leads
                .first()
                .lead
                .last()
                .row
                .joinToString(separator = "") { it.toBellChar() },
        )
        assertEquals(5, method.leads.size)
        assertEquals(listOf(2, 3), method.huntBells)
        assertEquals(
            listOf(
                listOf(1, 5, 7, 6, 4),
            ),
            method.leadCycles,
        )
    }

    @Test
    fun testPlaceNotationExpansion() {
        var pn = PlaceNotation("3.1,3.1")
        assertEquals(
            listOf("3", "1", "3", "3", "1", "3"),
            pn.fullNotation(5).notation,
        )
        pn = PlaceNotation("3.1")
        assertEquals(
            listOf("3", "1"),
            pn.fullNotation(5).notation,
        )
    }
}
