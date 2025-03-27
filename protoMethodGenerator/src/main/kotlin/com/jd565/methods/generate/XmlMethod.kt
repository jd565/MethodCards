package com.jd565.methods.generate

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
data class XmlCollection(
    @XmlElement @XmlSerialName("methodSet") val sets: List<XmlMethodSet>,
)

@Serializable
data class XmlMethodSet(
    @XmlElement @XmlSerialName("properties") val properties: XmlMethodSetProperties,
    @XmlElement @XmlSerialName("method") val methods: List<XmlMethod>,
    @XmlElement @XmlSerialName("notes") val notes: XmlField?,
)

@Serializable
data class XmlMethodSetProperties(
    @XmlElement @XmlSerialName("stage") val stage: XmlField?,
    @XmlElement @XmlSerialName("classification") val classification: XmlClassification?,
    @XmlElement @XmlSerialName("lengthOfLead") val lengthOfLead: XmlField?,
    @XmlElement @XmlSerialName("numberOfHunts") val numberOfHunts: XmlField?,
    @XmlElement @XmlSerialName("leadHead") val leadHead: XmlField?,
    @XmlElement @XmlSerialName("leadHeadCode") val leadHeadCode: XmlField?,
    @XmlElement @XmlSerialName("symmetry") val symmetry: XmlField?,
    @XmlElement @XmlSerialName("falseness") val falseness: XmlMethodFalseness?,
)

@Serializable
data class XmlMethodFalseness(
    @XmlElement @XmlSerialName("falseCourseHeads") val falseCourseHeads: List<XmlFalseCourseHeads>,
    @XmlElement @XmlSerialName("fchGroups") val fchGroups: XmlField?
)

@Serializable
data class XmlFalseCourseHeads(
    @XmlElement @XmlSerialName("inCourse") val inCourse: XmlField,
    @XmlElement @XmlSerialName("outOfCourse") val outOfCourse: XmlField,
)

@Serializable
data class XmlMethod(
    @XmlElement @XmlSerialName("title") val title: XmlField?,
    @XmlElement @XmlSerialName("name") val name: XmlField?,
    @XmlElement @XmlSerialName("notation") val notation: XmlField?,
    @XmlElement @XmlSerialName("stage") val stage: XmlField?,
    @XmlElement @XmlSerialName("classification") val classification: XmlClassification?,
    @XmlElement @XmlSerialName("lengthOfLead") val lengthOfLead: XmlField?,
    @XmlElement @XmlSerialName("numberOfHunts") val numberOfHunts: XmlField?,
    @XmlElement @XmlSerialName("leadHead") val leadHead: XmlField?,
    @XmlElement @XmlSerialName("leadHeadCode") val leadHeadCode: XmlField?,
    @XmlElement @XmlSerialName("symmetry") val symmetry: XmlField?,
    @XmlElement @XmlSerialName("falseness") val falseness: XmlMethodFalseness?,
)

@Serializable
data class XmlClassification(
    val little: Boolean?,
    val differential: Boolean?,
    val plain: Boolean?,
    val trebleDodging: Boolean?,
    @XmlValue val classification: String?,
)

@Serializable
data class XmlField(
    @XmlValue val value: String
)
