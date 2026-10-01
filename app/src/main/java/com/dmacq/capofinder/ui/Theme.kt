package com.dmacq.capofinder.ui

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Colour tokens from the approved design, one set per theme. */
data class AppColors(
    val bg: Color,
    val card: Color,
    val ink: Color,
    val sub: Color,
    val line: Color,
    val tile: Color,
    val accent: Color,
    val onAccent: Color,
    val accentText: Color,
    val board: Color,
    val string: Color,
    val wire: Color,
)

val LightColors = AppColors(
    bg = Color(0xFFEEF0EC),
    card = Color(0xFFFFFFFF),
    ink = Color(0xFF16211C),
    sub = Color(0xFF4F5C55),
    line = Color(0xFFD5DAD5),
    tile = Color(0xFFEEF0EC),
    accent = Color(0xFF0F5C45),
    onAccent = Color(0xFFFFFFFF),
    accentText = Color(0xFF0F5C45),
    board = Color(0xFFCFD8D2),
    string = Color(0xFF7C8A83),
    wire = Color(0xFF4F5C55),
)

val DarkColors = AppColors(
    bg = Color(0xFF101714),
    card = Color(0xFF1A2420),
    ink = Color(0xFFEEF3EF),
    sub = Color(0xFFA9B8B0),
    line = Color(0xFF2E3C36),
    tile = Color(0xFF232F2A),
    accent = Color(0xFF5FD3A4),
    onAccent = Color(0xFF0B1A14),
    accentText = Color(0xFF5FD3A4),
    board = Color(0xFF2A3832),
    string = Color(0xFF7E8F86),
    wire = Color(0xFFA9B8B0),
)

/** The capo bar looks the same in both themes. */
val CapoYellow = Color(0xFFF2B632)
val CapoEdge = Color(0xFF16211C)

val LocalAppColors = staticCompositionLocalOf { LightColors }

@Composable
fun CapoFinderTheme(dark: Boolean, content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
    }
    CompositionLocalProvider(LocalAppColors provides if (dark) DarkColors else LightColors) {
        MaterialTheme(
            colorScheme = if (dark) darkColorScheme() else lightColorScheme(),
            content = content,
        )
    }
}
