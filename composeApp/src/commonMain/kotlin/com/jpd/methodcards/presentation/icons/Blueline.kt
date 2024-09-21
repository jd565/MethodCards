package com.jpd.methodcards.presentation.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.Filled.Blueline: ImageVector
    get() {
        if (_blueline != null) {
            return _blueline!!
        }
        _blueline =
            materialIcon(name = "Filled.Blueline") {
                materialPath {
                    moveTo(3.5f, 18.49f)
                    lineToRelative(6.0f, -6.01f)
                    lineToRelative(4.0f, 4.0f)
                    lineTo(22.0f, 6.92f)
                    lineToRelative(-1.41f, -1.41f)
                    lineToRelative(-7.09f, 7.97f)
                    lineToRelative(-4.0f, -4.0f)
                    lineTo(2.0f, 16.99f)
                    close()
                }
            }
        return _blueline!!
    }

private var _blueline: ImageVector? = null
