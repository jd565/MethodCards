package com.jd565.methods.generate

import kotlinx.coroutines.test.runTest
import org.junit.Test

class BellBoardPerformanceParserTest {
    @Test
    fun testParse() = runTest {
        val service = CccbrService()
        val methods = service.getAllMethods()
            .sets
            .flatMapTo(mutableSetOf()) { it.methods.mapNotNull { m -> m.title?.value }}

        val performances = service.getBellboardPerformances()

        val parser = BellBoardPerformanceParser
        performances.forEach { println(it) }
        // println(parser.parse(performances, methods).joinToString("\n"))
    }
}
