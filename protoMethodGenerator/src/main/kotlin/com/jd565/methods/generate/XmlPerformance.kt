package com.jd565.methods.generate

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
data class XmlPerformances(
    @XmlElement @XmlSerialName("performance") val performances: List<XmlPerformance>,
)

@Serializable
data class XmlPerformance(
    @XmlElement @XmlSerialName("title") val title: XmlPerformanceTitle,
    @XmlElement @XmlSerialName("details") val details: XmlField?,
)

@Serializable
data class XmlPerformanceTitle(
    @XmlElement @XmlSerialName("method") val method: XmlField,
)
