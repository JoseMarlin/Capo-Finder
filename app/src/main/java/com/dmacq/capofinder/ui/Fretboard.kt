package com.dmacq.capofinder.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dmacq.capofinder.engine.CapoEngine

/** A mini guitar neck, frets 1 to 7, with the capo bar sitting just behind the chosen fret. */
@Composable
fun Fretboard(capo: Int, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    val frets = CapoEngine.MAX_CAPO
    val description = if (capo > 0) "Fretboard with the capo on fret $capo" else "Open position, no capo"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(6.dp)),
        ) {
            val space = size.width / frets
            drawRect(color = c.board)

            // Strings: thickest at the top.
            val thickness = listOf(3f, 2.5f, 2f, 1.5f, 1.2f, 1f)
            thickness.forEachIndexed { i, t ->
                drawRect(
                    color = c.string,
                    topLeft = Offset(0f, (8 + i * 9).dp.toPx()),
                    size = Size(size.width, t.dp.toPx()),
                )
            }

            // Fret wires.
            for (i in 1..frets) {
                drawRect(
                    color = c.wire,
                    topLeft = Offset(i * space - 1.dp.toPx(), 0f),
                    size = Size(2.dp.toPx(), size.height),
                )
            }

            // Nut.
            drawRect(color = c.ink, topLeft = Offset.Zero, size = Size(5.dp.toPx(), size.height))

            // Capo bar, just left of the fret wire.
            if (capo in 1..frets) {
                val barWidth = 12.dp.toPx()
                val topLeft = Offset(capo * space - barWidth - 2.dp.toPx(), 2.dp.toPx())
                val barSize = Size(barWidth, size.height - 4.dp.toPx())
                val corner = CornerRadius(6.dp.toPx())
                drawRoundRect(color = CapoYellow, topLeft = topLeft, size = barSize, cornerRadius = corner)
                drawRoundRect(
                    color = CapoEdge,
                    topLeft = topLeft,
                    size = barSize,
                    cornerRadius = corner,
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            for (n in 1..frets) {
                Text(
                    text = n.toString(),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = c.sub,
                    fontSize = 11.sp,
                    fontWeight = if (n == capo) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }

        Text(
            text = if (capo > 0) "Capo on fret $capo" else "Open position, no capo",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = c.sub,
            fontSize = 12.sp,
        )
    }
}
