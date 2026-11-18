package com.enoc.modojuego.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

@Composable
fun Hairline(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(P.line))
}

@Composable
fun Eyebrow(text: String, color: Color = P.dim, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.6.sp,
        modifier = modifier,
    )
}

/** Botón principal: contorno rojo en espera, relleno rojo cuando está activo. */
@Composable
fun MainButton(active: Boolean, onClick: () -> Unit) {
    val bg = if (active) P.red else Color.Transparent
    val fg = if (active) Color.Black else P.red
    Box(
        Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(Cut)
            .background(bg)
            .border(1.5.dp, P.red, Cut)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (active) "DETENER" else "ACTIVAR",
            color = fg,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 6.sp,
        )
    }
}

@Composable
fun SquareSwitch(on: Boolean) {
    Box(
        Modifier
            .size(width = 44.dp, height = 22.dp)
            .clip(CutCornerShape(5.dp))
            .background(if (on) P.red else P.line),
    ) {
        Box(
            Modifier
                .align(if (on) Alignment.CenterEnd else Alignment.CenterStart)
                .padding(3.dp)
                .size(16.dp)
                .background(if (on) Color.Black else P.dim),
        )
    }
}

@Composable
fun OptionRow(title: String, sub: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = P.text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(sub, color = P.dim, fontSize = 12.sp, lineHeight = 16.sp)
        }
        Spacer(Modifier.width(16.dp))
        SquareSwitch(checked)
    }
}

@Composable
fun TagButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(CutSmall)
            .background(if (selected) P.red else Color.Transparent)
            .border(1.dp, if (selected) P.red else P.line, CutSmall)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = if (selected) Color.Black else P.dim,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
        )
    }
}

@Composable
fun AppIcon(pkg: String, size: Dp = 36.dp) {
    val ctx = LocalContext.current
    val bmp = remember(pkg) {
        runCatching { ctx.packageManager.getApplicationIcon(pkg).toBitmap(96, 96).asImageBitmap() }.getOrNull()
    }
    Box(Modifier.size(size).clip(CutTiny).background(P.line)) {
        if (bmp != null) Image(bmp, contentDescription = null, modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun BottomNav(tab: Int, onTab: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth().background(P.bg)) {
        Hairline()
        Row(Modifier.fillMaxWidth()) {
            listOf("INICIO", "APPS").forEachIndexed { i, label ->
                val sel = tab == i
                Box(
                    Modifier
                        .weight(1f)
                        .clickable { onTab(i) }
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        color = if (sel) P.text else P.dim,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                    )
                    if (sel) {
                        Box(
                            Modifier
                                .align(Alignment.TopCenter)
                                .width(28.dp)
                                .height(3.dp)
                                .background(P.red),
                        )
                    }
                }
            }
        }
    }
}
