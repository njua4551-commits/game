package com.enoc.modojuego.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.enoc.modojuego.core.AppItem
import com.enoc.modojuego.core.AppRepo
import com.enoc.modojuego.core.Dnd
import com.enoc.modojuego.core.Game
import com.enoc.modojuego.core.GameState
import com.enoc.modojuego.data.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun readPerms(ctx: Context): Perms {
    val notif = if (Build.VERSION.SDK_INT >= 33) {
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
    return Perms(
        notifications = notif,
        overlay = Settings.canDrawOverlays(ctx),
        dnd = Dnd.granted(ctx),
        listener = NotificationManagerCompat.getEnabledListenerPackages(ctx).contains(ctx.packageName),
    )
}

private fun Set<String>.toggle(x: String): Set<String> = if (x in this) this - x else this + x

@Composable
fun Root(prefs: Prefs, requestNotif: () -> Unit) {
    val ctx = LocalContext.current

    // Se incrementa al volver a la app (por ejemplo desde Ajustes) para releer permisos y apps.
    var tick by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { tick++ }

    val running by GameState.running.collectAsState()

    var apps by remember { mutableStateOf(emptyList<AppItem>()) }
    LaunchedEffect(tick) {
        apps = withContext(Dispatchers.Default) { AppRepo.launchable(ctx) }
    }

    var games by remember { mutableStateOf(prefs.games) }
    var allowed by remember { mutableStateOf(prefs.allowed) }
    var opts by remember { mutableStateOf(Options(prefs.overlay, prefs.dnd, prefs.filter)) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val perms = remember(tick) { readPerms(ctx) }
    val gameApps = remember(apps, games) { apps.filter { it.pkg in games } }

    fun grant(p: Perm) {
        when (p) {
            Perm.NOTIF -> requestNotif()
            Perm.OVERLAY -> ctx.startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${ctx.packageName}")),
            )
            Perm.DND -> ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            Perm.LISTENER -> ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    // Con el modo ya activo, reenvía el inicio para que el servicio reaplique las opciones.
    fun refreshService() {
        if (GameState.running.value) Game.start(ctx, prefs.currentGame)
    }

    val act = Actions(
        toggle = { if (running) Game.stop(ctx) else Game.start(ctx, null) },
        play = { app ->
            Game.start(ctx, app.pkg)
            ctx.packageManager.getLaunchIntentForPackage(app.pkg)?.let {
                ctx.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        },
        setOverlay = { v ->
            prefs.overlay = v
            opts = opts.copy(overlay = v)
            if (v && !Settings.canDrawOverlays(ctx)) grant(Perm.OVERLAY)
            refreshService()
        },
        setDnd = { v ->
            prefs.dnd = v
            opts = opts.copy(dnd = v)
            if (v && !Dnd.granted(ctx)) grant(Perm.DND)
            refreshService()
        },
        setFilter = { v ->
            prefs.filter = v
            opts = opts.copy(filter = v)
            if (v && !readPerms(ctx).listener) grant(Perm.LISTENER)
        },
        grant = { p -> grant(p) },
    )

    Box(Modifier.fillMaxSize().background(P.bg)) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                when (tab) {
                    0 -> HomeScreen(running, gameApps, perms, opts, act)
                    else -> AppsScreen(
                        apps = apps,
                        games = games,
                        allowed = allowed,
                        onGame = { pkg ->
                            games = games.toggle(pkg)
                            prefs.games = games
                        },
                        onAllowed = { pkg ->
                            allowed = allowed.toggle(pkg)
                            prefs.allowed = allowed
                        },
                    )
                }
            }
            BottomNav(tab) { tab = it }
        }
    }
}
