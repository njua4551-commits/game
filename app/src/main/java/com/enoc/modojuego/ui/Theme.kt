package com.enoc.modojuego.ui

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Paleta: negro mate, hairlines grises y un único rojo. Sin gradientes. */
object P {
    val bg = Color(0xFF0B0B0C)
    val surface = Color(0xFF141416)
    val line = Color(0xFF2A2A2E)
    val text = Color(0xFFECECEE)
    val dim = Color(0xFF8C8C94)
    val red = Color(0xFFE5202B)
}

/** Esquinas cortadas en diagonal, en lugar de redondeadas. */
val Cut = CutCornerShape(topEnd = 14.dp, bottomStart = 14.dp)
val CutSmall = CutCornerShape(topEnd = 8.dp, bottomStart = 8.dp)
val CutTiny = CutCornerShape(topEnd = 6.dp, bottomStart = 6.dp)

@Composable
fun ModoJuegoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = P.red,
            onPrimary = Color.Black,
            background = P.bg,
            surface = P.surface,
            onBackground = P.text,
            onSurface = P.text,
        ),
        content = content,
    )
}
