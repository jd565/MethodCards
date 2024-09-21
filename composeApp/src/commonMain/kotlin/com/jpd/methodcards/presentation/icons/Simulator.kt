package com.jpd.methodcards.presentation.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.Filled.Simulator: ImageVector
    get() {
        if (_simulator != null) {
            return _simulator!!
        }
        _simulator =
            materialIcon(name = "Filled.Simulator") {
                materialPath {
                    moveTo(6.99f, 11f)
                    lineTo(3f, 15f)
                    lineToRelative(3.99f, 4f)
                    verticalLineToRelative(-3f)
                    horizontalLineTo(14f)
                    verticalLineToRelative(-2f)
                    horizontalLineTo(6.99f)
                    verticalLineToRelative(-3f)
                    close()
                    moveTo(21f, 9f)
                    lineToRelative(-3.99f, -4f)
                    verticalLineToRelative(3f)
                    horizontalLineTo(10f)
                    verticalLineToRelative(2f)
                    horizontalLineToRelative(7.01f)
                    verticalLineToRelative(3f)
                    lineTo(21f, 9f)
                }
            }
        return _simulator!!
    }

private var _simulator: ImageVector? = null
