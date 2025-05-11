package com.jd565.methods.generate

import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class CccbrServiceTest {
    @Test
    fun testGetAllMethods() = runTest {
        val service = CccbrService()
        val alliance = service.getAllMethods()
        var stedman: Pair<XmlMethodSet, XmlMethod>? = null
        alliance.sets.forEach { set ->
            set.methods.forEach { method ->
                if (method.title?.value == "Stedman Triples") {
                    stedman = set to method
                }
            }
        }
        println(stedman)
    }

    @Test
    fun testGetPerformances() = runTest {
        val service = CccbrService()
        val performances = service.getBellboardPerformances()
        performances.forEach {
            println(it)
        }
    }
}
