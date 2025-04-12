package com.jpd.methodcards.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class PlaceNotationTest {
    @Test
    fun testClassifications() {
        listOf(
            Triple("-14-36-56,12", 6, MethodClassDescriptor(MethodClassification.Bob)),
            Triple("3456.16-1236.12.36,12", 6, MethodClassDescriptor(MethodClassification.Place)),
            Triple("-34-16-12-16-12-16,16", 6, MethodClassDescriptor(MethodClassification.TrebleBob)),
            Triple("-36-14-12-36-14-56,12", 6, MethodClassDescriptor(MethodClassification.Surprise)),
            Triple("-34-14-12-16-34-56,12", 6, MethodClassDescriptor(MethodClassification.Delight)),
            Triple("-36-14.1234-1234.16-34-56,12", 6, MethodClassDescriptor(MethodClassification.TreblePlace)),
            Triple("-38-14-1258-36-14-58-16.78,12", 8, MethodClassDescriptor(MethodClassification.Alliance)),
            Triple("-36-14-12-36-1456.56-56.14-36-12-14-36-12", 6, MethodClassDescriptor(MethodClassification.Hybrid)),
            Triple("34.10-14,12", 10, MethodClassDescriptor(MethodClassification.Bob, little = true)),
            Triple("5.1.5.1.5,345", 5, MethodClassDescriptor(MethodClassification.None, differential = true)),
            Triple("-14,34", 4, MethodClassDescriptor(MethodClassification.Bob, little = true)),
            Triple("-14-14.12.14.34.12", 4, MethodClassDescriptor(MethodClassification.Bob)),
        ).forEach { (pn, stage, expected) ->
            assertEquals(
                expected,
                PlaceNotation(pn).fullNotation(stage).classification,
                "Failed for pn: $pn, stage: $stage",
            )
        }
    }
}
