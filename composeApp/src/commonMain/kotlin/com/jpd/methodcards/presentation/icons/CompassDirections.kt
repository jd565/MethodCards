package com.jpd.methodcards.presentation.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

public val Icons.Filled.SouthWest: ImageVector
    get() {
        if (_southWest != null) {
            return _southWest!!
        }
        _southWest =
            ImageVector
                .Builder(
                    name = "Filled.SouthWest",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 960f,
                    viewportHeight = 960f,
                    autoMirror = false,
                ).apply {
                    materialPath {
                        moveTo(200f, 760f)
                        lineTo(200f, 360f)
                        lineTo(280f, 360f)
                        lineTo(280f, 624f)
                        lineTo(744f, 160f)
                        lineTo(800f, 216f)
                        lineTo(336f, 680f)
                        lineTo(600f, 680f)
                        lineTo(600f, 760f)
                        lineTo(200f, 760f)
                        close()
                    }
                }.build()
        return _southWest!!
    }

public val Icons.Filled.South: ImageVector
    get() {
        if (_south != null) {
            return _south!!
        }
        _south =
            ImageVector
                .Builder(
                    name = "Filled.South",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 960f,
                    viewportHeight = 960f,
                    autoMirror = false,
                ).apply {
                    materialPath {
                        "M480,880L200,600L256,544L440,727L440,80L520,80L520,727L704,543L760,600L480,880Z"
                        moveTo(480f, 880f)
                        lineTo(200f, 600f)
                        lineTo(256f, 544f)
                        lineTo(440f, 727f)
                        lineTo(440f, 80f)
                        lineTo(520f, 80f)
                        lineTo(520f, 727f)
                        lineTo(704f, 543f)
                        lineTo(760f, 600f)
                        lineTo(480f, 880f)
                        close()
                    }
                }.build()
        return _south!!
    }

public val Icons.Filled.SouthEast: ImageVector
    get() {
        if (_southEast != null) {
            return _southEast!!
        }
        _southEast =
            ImageVector
                .Builder(
                    name = "Filled.SouthEast",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 960f,
                    viewportHeight = 960f,
                    autoMirror = false,
                ).apply {
                    materialPath {
                        moveTo(360f, 760f)
                        lineTo(360f, 680f)
                        lineTo(624f, 680f)
                        lineTo(160f, 216f)
                        lineTo(216f, 160f)
                        lineTo(680f, 624f)
                        lineTo(680f, 360f)
                        lineTo(760f, 360f)
                        lineTo(760f, 760f)
                        lineTo(360f, 760f)
                        close()
                    }
                }.build()
        return _southEast!!
    }

private var _southWest: ImageVector? = null
private var _south: ImageVector? = null
private var _southEast: ImageVector? = null
