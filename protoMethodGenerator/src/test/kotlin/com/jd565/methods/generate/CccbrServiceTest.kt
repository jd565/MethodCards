package com.jd565.methods.generate

import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class CccbrServiceTest {
    @Test
    fun testGetAllMethods() = runTest {
        val service = CccbrService()
        val alliance = service.getAllMethods()
        alliance.sets.forEach {
            println(it.notes)
        }
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
