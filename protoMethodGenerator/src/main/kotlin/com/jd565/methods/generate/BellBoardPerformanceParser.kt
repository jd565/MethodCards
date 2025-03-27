package com.jd565.methods.generate

import com.jpd.methodcards.domain.stageNameToInt

object BellBoardPerformanceParser {
    fun parse(performances: List<XmlPerformance>, methods: Set<String>): Map<Int, List<String>> {
        val map = mutableMapOf<String, Int>()
        performances.forEach { performance ->
            val title = performance.title.method.value.trim()
            val details = performance.details?.value
            if ((title.contains("Spliced") || title.contains(multiMethodRegex)) && details != null) {
                var t2 = title
                    .removePrefix("Spliced ")
                    .removePrefix("Mixed")
                if (t2.contains("(")) {
                    t2 = t2.substringBefore("(").trim()
                }
                val append = t2.split("\\s+".toRegex())
                    .asReversed()
                    .runningFold("") { acc, s -> "$s $acc".trim() }
                    .asReversed()

                val names = methodNameRegex.findAll(details)
                    .map { it.value }
                for (name in names) {
                    for (a in append) {
                        val possibleName = "$name $a".trim()
                        if (methods.contains(possibleName)) {
                            map[possibleName] = map.getOrPut(possibleName) { 0 } + 1
                            break
                        }
                    }
                }
            } else if (methods.contains(title)) {
                map[title] = map.getOrPut(title) { 0 } + 1
            }
        }

        val stageMap = mutableMapOf<Int, MutableList<String>>()

        map.map { it.key to it.value }
            .sortedByDescending { it.second }
            .forEach { (title, _) ->
                val stage = title.split(" ").last().stageNameToInt()
                stageMap.getOrPut(stage) { mutableListOf() }.add(title)
            }
        return stageMap
    }

    private val multiMethodPattern = "\\(\\d+([mpv]/)*[mpv]\\)"
    private val multiMethodRegex = Regex(multiMethodPattern)

    private val methodNamePart = "[A-Z][a-z']+"
    private val methodNameRegex = Regex("(${methodNamePart} )*${methodNamePart}")
}
