package com.dmacq.capofinder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The one button style used for keys, chords, Major/Minor and recent keys.
 * Every instance is at least 44dp tall so it is easy to hit with a thumb.
 */
@Composable
fun ToggleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
    fontSize: TextUnit = 14.sp,
    shape: Shape = RoundedCornerShape(10.dp),
    idleBackground: Color? = null,
    accessibilityLabel: String? = null,
) {
    val c = LocalAppColors.current
    val background = if (selected) c.accent else (idleBackground ?: c.card)
    val textColor = if (selected) c.onAccent else c.ink
    val borderColor = if (selected) c.accent else c.line

    Box(
        modifier = modifier
            .heightIn(min = height)
            .clip(shape)
            .background(background)
            .border(1.dp, borderColor, shape)
            .selectable(selected = selected, role = Role.Button, onClick = onClick)
            .semantics { if (accessibilityLabel != null) contentDescription = accessibilityLabel },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = fontSize * 1.15f,
        )
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = LocalAppColors.current.sub,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
    )
}
