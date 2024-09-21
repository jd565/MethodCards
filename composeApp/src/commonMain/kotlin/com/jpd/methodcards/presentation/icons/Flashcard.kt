package com.jpd.methodcards.presentation.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.Filled.Flashcard: ImageVector
    get() {
        if (_flashcard != null) {
            return _flashcard!!
        }
        _flashcard =
            materialIcon(name = "Filled.Flashcard") {
                materialPath {
                    moveTo(19f, 5f)
                    horizontalLineTo(5f)
                    curveTo(3.9f, 5f, 3f, 5.9f, 3f, 7f)
                    verticalLineToRelative(10f)
                    curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                    horizontalLineToRelative(14f)
                    curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                    verticalLineTo(7f)
                    curveTo(21f, 5.9f, 20.1f, 5f, 19f, 5f)
                    close()
                    moveTo(19f, 17f)
                    horizontalLineTo(5f)
                    verticalLineTo(7f)
                    horizontalLineToRelative(14f)
                    verticalLineTo(17f)
                    close()
                }
            }
        return _flashcard!!
    }

private var _flashcard: ImageVector? = null
