package com.jpd.methodcards.presentation.blueline

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jpd.methodcards.domain.Row
import com.jpd.methodcards.domain.toBellChar

private val Color.Companion.Orange get() = Color(0xffff6600)
private val Color.Companion.Purple get() = Color(0xff6600cc)
private val Color.Companion.Teal get() = Color(0xff009999)
private val Color.Companion.GoldenYellow get() = Color(0xffccaa00)
private val Color.Companion.DeepPink get() = Color(0xffff1493)
private val Color.Companion.LightBlue get() = Color(0xff89cff0)

val DarkBlueLineColors =
    listOf(
        Color.LightBlue,
        Color.Magenta,
        Color.GoldenYellow,
        Color.LightGray,
        Color.DeepPink,
        Color.White,
        Color.Red,
        Color.Green,
        Color.Orange,
        Color.Teal,
        Color.Purple,
    )

val LightBlueLineColors =
    listOf(
        Color.Blue,
        Color.Magenta,
        Color.GoldenYellow,
        Color.DarkGray,
        Color.DeepPink,
        Color.Black,
        Color.Red,
        Color.Green,
        Color.Orange,
        Color.Teal,
        Color.Purple,
    )

var blueLineColors: List<Color> = LightBlueLineColors
val TrebleLineColor = Color.Red

val BlueLineWidth = 4.dp
val TrebleLineWidth = 2.dp

val Density.BlueLineStroke get() = Stroke(width = BlueLineWidth.toPx())
val Density.TrebleLineStroke get() = Stroke(width = TrebleLineWidth.toPx())

data class BlueLineDetail(
    val place: Int,
    val color: Color,
    val width: Dp,
) {
    val path: Path = Path()
}

fun blueLineDetails(
    places: List<Int>,
    huntBells: List<Int>,
): List<BlueLineDetail> {
    val colors = blueLineColors
    return places
        .mapIndexedTo(mutableListOf()) { index, it ->
            BlueLineDetail(it, colors[index % colors.size], BlueLineWidth)
        }.apply {
            huntBells.forEach {
                add(0, BlueLineDetail(it, TrebleLineColor, TrebleLineWidth))
            }
        }
}

fun calculateBlueLineStyle(
    measurer: TextMeasurer,
    maxHeight: Float,
    numberOfRows: Int,
): TextStyle {
    var style =
        TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp,
            fontFamily = FontFamily.Monospace,
            lineHeightStyle =
            LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.Both,
            ),
        )
    do {
        require(style.fontSize.value > 0)
        val result =
            measurer.measure(1.toBellChar(), style = style, skipCache = true)
        println("Trying font size ${style.fontSize} with height ${result.size.height}")
        println("Total height ${result.size.height * numberOfRows} in $maxHeight")
        val totalHeight = result.size.height * numberOfRows
        if (totalHeight > maxHeight) {
            val newSize =
                ((maxHeight / totalHeight) * style.fontSize.value)
                    .sp
            style = style.copy(fontSize = newSize)
        }
    } while (totalHeight > maxHeight)
    return style
}

fun DrawScope.drawRows(
    rows: List<Row>,
    placeCharResults: List<TextLayoutResult>,
    textColor: Color,
    topLeft: Offset,
    blueLines: List<BlueLineDetail>,
    ruleoffsEvery: Int,
    ruleoffsFrom: Int,
): IntSize {
    val spacing = placeCharResults[0].size.height
    val size =
        IntSize(
            width = rows[0].row.size * spacing,
            height = rows.size * spacing,
        )

    rows.forEachIndexed { yIndex, row ->
        row.row.forEachIndexed { index, d ->
            val result = placeCharResults[d - 1]
            drawText(
                result,
                color = textColor,
                topLeft =
                Offset(
                    x = topLeft.x + index * spacing + (spacing - result.size.width) / 2,
                    y = topLeft.y + yIndex * spacing + (spacing - result.size.height) / 2,
                ),
            )
        }
    }

    val rules = mutableListOf<Int>()
    var ruleoff = ruleoffsFrom
    while (ruleoff <= rows.size) {
        if (ruleoff > 0) {
            rules.add(ruleoff)
        }
        ruleoff += ruleoffsEvery
    }
    rules.forEach { idx ->
        drawLine(
            textColor,
            start = Offset(
                topLeft.x,
                topLeft.y + spacing * idx - (0.5).dp.toPx(),
            ),
            end = Offset(
                topLeft.x + size.width,
                topLeft.y + spacing * idx - (0.5).dp.toPx(),
            ),
            strokeWidth = 1.dp.toPx(),
            pathEffect =
            PathEffect.dashPathEffect(
                floatArrayOf(
                    3.dp.toPx(),
                    (1.5).dp.toPx(),
                ),
            ),
        )
    }

    rows.forEachIndexed { index, row ->
        val y = topLeft.y + index * spacing + spacing / 2
        blueLines.forEach { blueLine ->
            val place = row.row.indexOf(blueLine.place)
            val x = topLeft.x + place * spacing + spacing / 2
            if (index == 0) {
                blueLine.path.reset()
                blueLine.path.moveTo(x, y)
            } else {
                blueLine.path.lineTo(x, y)
            }
        }
    }
    blueLines.forEach {
        drawPath(it.path, it.color, style = Stroke(width = it.width.toPx()))
    }

    return size
}

fun DrawScope.drawLeadIndicators(
    measurer: TextMeasurer,
    blueLineStyle: TextStyle,
    bells: List<Int>,
    topLeft: Offset,
    textColor: Color,
): IntSize {
    val placeStyle = blueLineStyle.copy(fontSize = blueLineStyle.fontSize * 2)
    var height = 0
    var width = 0
    bells.forEachIndexed { idx, bell ->
        val startPlaceResult = measurer.measure(bell.toBellChar(), style = placeStyle)

        drawCircle(
            color = blueLineColors[0],
            radius = startPlaceResult.size.height / 2f,
            center =
            Offset(
                x = topLeft.x + startPlaceResult.size.height / 2f + width,
                y = topLeft.y + startPlaceResult.size.height / 2f,
            ),
            style = Stroke(width = 2.dp.toPx()),
        )

        drawText(
            startPlaceResult,
            color = textColor,
            topLeft =
            Offset(
                x = topLeft.x + (startPlaceResult.size.height - startPlaceResult.size.width) / 2 + width,
                y = topLeft.y,
            ),
        )

        height = startPlaceResult.size.height
        width += startPlaceResult.size.height
        if (idx != bells.lastIndex) {
            width += 4.dp.roundToPx()
        }
    }

    return IntSize(
        width = width,
        height = height,
    )
}
