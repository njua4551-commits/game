package com.enoc.modojuego.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enoc.modojuego.core.AppItem
import com.enoc.modojuego.core.Snap
import com.enoc.modojuego.core.Stats
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    running: Boolean,
    games: List<AppItem>,
    perms: Perms,
    opts: Options,
    act: Actions,
) {
    val ctx = LocalContext.current
    var snap by remember { mutableStateOf(Stats.read(ctx)) }
    LaunchedEffect(Unit) {
        while (true) {
            snap = Stats.read(ctx)
            delay(2000)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))

        // Cabecera
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("MODO", color = P.dim, fontSize = 15.sp, fontWeight = FontWeight.Medium, letterSpacing = 6.sp)
                Text(
                    "JUEGO",
                    color = P.text,
                    fontSize = 44.sp,
                    lineHeight = 44.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(Build.MODEL, color = P.dim, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                Text(
                    String.format("%.1f GB RAM", snap.totalGb),
                    color = P.dim,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(if (running) P.red else P.dim))
            Spacer(Modifier.width(10.dp))
            Text(
                if (running) "ACTIVO" else "EN ESPERA",
                color = if (running) P.red else P.dim,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 2.sp,
            )
        }

        Spacer(Modifier.height(22.dp))
        Hairline()
        StatStrip(snap)
        Hairline()
        Spacer(Modifier.height(22.dp))

        MainButton(running, act.toggle)

        // Juegos
        Spacer(Modifier.height(32.dp))
        Eyebrow("Juegos")
        Spacer(Modifier.height(6.dp))
        if (games.isEmpty()) {
            Text(
                "Todavía no hay juegos. Márcalos en la pestaña APPS.",
                color = P.dim,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 10.dp),
            )
        } else {
            games.forEach { app ->
                Hairline()
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppIcon(app.pkg, 40.dp)
                    Spacer(Modifier.width(14.dp))
                    Text(
                        app.label,
                        color = P.text,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                    TagButton("JUGAR", true) { act.play(app) }
                }
            }
            Hairline()
        }

        // Opciones
        Spacer(Modifier.height(32.dp))
        Eyebrow("Opciones")
        Spacer(Modifier.height(4.dp))
        Hairline()
        OptionRow(
            "Contador en pantalla",
            "RAM y temperatura de la batería sobre el juego.",
            opts.overlay,
            act.setOverlay,
        )
        Hairline()
        OptionRow(
            "No molestar",
            "Solo suenan llamadas y alarmas. Sin avisos emergentes.",
            opts.dnd,
            act.setDnd,
        )
        Hairline()
        OptionRow(
            "Filtrar notificaciones",
            "Oculta los avisos de las apps que no marcaste como MANTENER.",
            opts.filter,
            act.setFilter,
        )
        Hairline()

        // Permisos
        Spacer(Modifier.height(32.dp))
        Eyebrow("Permisos")
        Spacer(Modifier.height(4.dp))
        Hairline()
        PermRow("Notificaciones", "Para el aviso fijo del modo juego.", perms.notifications) {
            act.grant(Perm.NOTIF)
        }
        Hairline()
        PermRow("Superposición", "Para el contador sobre el juego.", perms.overlay) {
            act.grant(Perm.OVERLAY)
        }
        Hairline()
        PermRow("Acceso a No molestar", "Para silenciar todo menos llamadas.", perms.dnd) {
            act.grant(Perm.DND)
        }
        Hairline()
        PermRow("Acceso a notificaciones", "Para filtrar por app.", perms.listener) {
            act.grant(Perm.LISTENER)
        }
        Hairline()

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun StatStrip(s: Snap) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Stat("RAM LIBRE", String.format("%.1f", s.freeGb), "GB", 0.dp, Modifier.weight(1f))
        VLine()
        Stat("BATERÍA", String.format("%.0f", s.tempC), "°C", 14.dp, Modifier.weight(1f))
        VLine()
        Stat("CARGA", s.batt.toString(), "%", 14.dp, Modifier.weight(1f))
    }
}

@Composable
private fun VLine() {
    Box(Modifier.width(1.dp).fillMaxHeight().background(P.line))
}

@Composable
private fun Stat(
    label: String,
    value: String,
    unit: String,
    startPad: Dp,
    modifier: Modifier,
) {
    Column(modifier.padding(start = startPad, top = 14.dp, bottom = 14.dp)) {
        Eyebrow(label)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value,
                color = P.text,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                unit,
                color = P.dim,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 4.dp, bottom = 5.dp),
            )
        }
    }
}

@Composable
private fun PermRow(title: String, sub: String, ok: Boolean, onGrant: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = P.text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(sub, color = P.dim, fontSize = 12.sp, lineHeight = 16.sp)
        }
        Spacer(Modifier.width(12.dp))
        if (ok) {
            Text("LISTO", color = P.dim, fontSize = 11.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.6.sp)
        } else {
            TagButton("CONCEDER", true, onGrant)
        }
    }
}
