package com.jd565.methods.generate

import com.jpd.methodcards.domain.MethodClassDescriptor
import com.jpd.methodcards.domain.MethodClassification
import com.jpd.methodcards.domain.PlaceNotation
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ClassificationTest {
    @Test
    fun checkAllClassifications() = runTest {
        val service = CccbrService()
        service.getAllMethods().sets.forEach { methodSet ->
            val properties = methodSet.properties
            for (method in methodSet.methods) {
                val stage = (method.stage?.value ?: properties.stage?.value)?.toIntOrNull()!!
                if (stage > 16) continue
                val notation = method.notation?.value!!
                val myClassification = PlaceNotation(notation).fullNotation(stage)
                    .classification

                val xmlClassification = method.classification ?: properties.classification ?: continue
                val classification = xmlClassification.classification ?: continue
                val theirClassification = MethodClassDescriptor(
                    MethodClassification.entries.first { it.part == classification },
                    differential = xmlClassification.differential ?: false,
                    little = xmlClassification.little ?: false,
                )

                assertEquals(
                    theirClassification,
                    myClassification,
                    "Failed for method: ${method.title?.value}, stage: $stage, notation: $notation",
                )
            }
        }
    }
}
