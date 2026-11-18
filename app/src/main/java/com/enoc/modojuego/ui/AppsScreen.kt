package com.enoc.modojuego.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enoc.modojuego.core.AppItem

@Composable
fun AppsScreen(
    apps: List<AppItem>,
    games: Set<String>,
    allowed: Set<String>,
    onGame: (String) -> Unit,
    onAllowed: (String) -> Unit,
) {
    var q by remember { mutableStateOf("") }
    val shown = remember(apps, q) {
        if (q.isBlank()) apps else apps.filter { it.label.contains(q.trim(), ignoreCase = true) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Text("APPS", color = P.text, fontSize = 34.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Spacer(Modifier.height(10.dp))
        Text(
            "JUEGO la muestra en Inicio para lanzarla. MANTENER evita que se cierre o se silencie en modo juego.",
            color = P.dim,
            fontSize = 12.sp,
            lineHeight = 17.sp,
        )
        Spacer(Modifier.height(14.dp))

        BasicTextField(
            value = q,
            onValueChange = { q = it },
            singleLine = true,
            textStyle = TextStyle(color = P.text, fontSize = 16.sp),
            cursorBrush = SolidColor(P.red),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Column {
                    Box(Modifier.padding(vertical = 10.dp)) {
                        if (q.isEmpty()) Text("Buscar aplicación", color = P.dim, fontSize = 16.sp)
                        inner()
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(P.red))
                }
            },
        )
        Spacer(Modifier.height(6.dp))

        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            items(shown, key = { it.pkg }) { app ->
                AppRow(
                    app = app,
                    isGame = app.pkg in games,
                    isKept = app.pkg in allowed,
                    onGame = { onGame(app.pkg) },
                    onKept = { onAllowed(app.pkg) },
                )
                Hairline()
            }
        }
    }
}

@Composable
private fun AppRow(app: AppItem, isGame: Boolean, isKept: Boolean, onGame: () -> Unit, onKept: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(app.pkg)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                app.label,
                color = P.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                app.pkg,
                color = P.dim,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        TagButton("JUEGO", isGame, onGame)
        Spacer(Modifier.width(6.dp))
        TagButton("MANTENER", isKept, onKept)
    }
}
