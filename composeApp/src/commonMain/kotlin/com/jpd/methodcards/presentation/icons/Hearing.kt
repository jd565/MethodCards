package com.jpd.methodcards.presentation.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

public val Icons.Filled.Hearing: ImageVector
    get() {
        if (_hearing != null) {
            return _hearing!!
        }
        _hearing = ImageVector.Builder(
            name = "Filled.Hearing",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f,
            autoMirror = false,
        ).apply {
            materialPath {
                moveTo(280f, 880f)
                quadTo(342f, 880f, 381.5f, 849f)
                quadTo(421f, 818f, 442f, 758f)
                quadTo(459f, 708f, 474.5f, 688f)
                quadTo(490f, 668f, 546f, 624f)
                quadTo(608f, 574f, 644f, 511f)
                quadTo(680f, 448f, 680f, 360f)
                quadTo(680f, 241f, 599.5f, 160.5f)
                quadTo(519f, 80f, 400f, 80f)
                quadTo(281f, 80f, 200.5f, 160.5f)
                quadTo(120f, 241f, 120f, 360f)
                lineTo(200f, 360f)
                quadTo(200f, 275f, 257.5f, 217.5f)
                quadTo(315f, 160f, 400f, 160f)
                quadTo(485f, 160f, 542.5f, 217.5f)
                quadTo(600f, 275f, 600f, 360f)
                quadTo(600f, 428f, 573f, 476f)
                quadTo(546f, 524f, 496f, 562f)
                quadTo(444f, 600f, 415f, 636f)
                quadTo(386f, 672f, 372f, 714f)
                quadTo(358f, 758f, 338.5f, 779f)
                quadTo(319f, 800f, 280f, 800f)
                quadTo(247f, 800f, 223.5f, 776.5f)
                quadTo(200f, 753f, 200f, 720f)
                lineTo(120f, 720f)
                quadTo(120f, 786f, 167f, 833f)
                quadTo(214f, 880f, 280f, 880f)
                close()
                moveTo(712f, 670f)
                quadTo(771f, 610f, 805.5f, 530.5f)
                quadTo(840f, 451f, 840f, 360f)
                quadTo(840f, 268f, 805.5f, 188f)
                quadTo(771f, 108f, 712f, 48f)
                lineTo(654f, 104f)
                quadTo(704f, 154f, 732f, 219.5f)
                quadTo(760f, 285f, 760f, 360f)
                quadTo(760f, 434f, 732f, 499f)
                quadTo(704f, 564f, 654f, 614f)
                lineTo(712f, 670f)
                close()
                moveTo(400f, 460f)
                quadTo(442f, 460f, 471f, 430.5f)
                quadTo(500f, 401f, 500f, 360f)
                quadTo(500f, 318f, 471f, 289f)
                quadTo(442f, 260f, 400f, 260f)
                quadTo(358f, 260f, 329f, 289f)
                quadTo(300f, 318f, 300f, 360f)
                quadTo(300f, 401f, 329f, 430.5f)
                quadTo(358f, 460f, 400f, 460f)
                close()
            }
        }.build()
        return _hearing!!
    }

private var _hearing: ImageVector? = null
