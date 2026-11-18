package com.enoc.modojuego.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.enoc.modojuego.MainActivity
import com.enoc.modojuego.R
import com.enoc.modojuego.core.Cleaner
import com.enoc.modojuego.core.Dnd
import com.enoc.modojuego.core.GameState
import com.enoc.modojuego.data.Prefs
import java.util.concurrent.Executors

class GameService : Service() {

    companion object {
        const val ACTION_START = "com.enoc.modojuego.START"
        const val ACTION_STOP = "com.enoc.modojuego.STOP"
        const val EXTRA_GAME = "game"

        private const val CH_ACTIVE = "modo_activo"
        private const val CH_SUMMARY = "modo_resumen"
        private const val NOTIF_ACTIVE = 42
        private const val NOTIF_SUMMARY = 43
        private const val TICK_MS = 2000L
        private const val SWEEP_EVERY_TICKS = 20 // ~40 s
    }

    private val handler = Handler(Looper.getMainLooper())
    private val worker = Executors.newSingleThreadExecutor()
    private lateinit var prefs: Prefs
    private var overlay: StatsOverlay? = null
    private var running = false

    private val loop = object : Runnable {
        private var n = 0
        override fun run() {
            overlay?.update()
            if (n++ % SWEEP_EVERY_TICKS == 0) sweepAsync()
            handler.postDelayed(this, TICK_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            shutdown()
            return START_NOT_STICKY
        }
        begin(intent?.getStringExtra(EXTRA_GAME))
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        shutdown()
        worker.shutdown()
        super.onDestroy()
    }

    // ---------------------------------------------------------------- ciclo

    private fun begin(game: String?) {
        prefs.currentGame = game
        ensureChannels()
        ServiceCompat.startForeground(
            this, NOTIF_ACTIVE, activeNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
        )

        if (!running) {
            running = true
            GameState.blocked.clear()
            GameState.running.value = true
            handler.post(loop)
        } else {
            sweepAsync()
        }

        // Reconcilia opciones (también cuando se cambian con el modo ya activo).
        if (prefs.dnd) Dnd.apply(this, prefs) else Dnd.restore(this, prefs)
        if (prefs.overlay) {
            if (overlay == null) overlay = StatsOverlay(this)
            overlay?.show()
        } else {
            overlay?.hide()
            overlay = null
        }
    }

    private fun shutdown() {
        if (running) {
            running = false
            handler.removeCallbacksAndMessages(null)
            overlay?.hide()
            overlay = null
            Dnd.restore(this, prefs)
            postSummary()
            GameState.running.value = false
            prefs.currentGame = null
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun sweepAsync() {
        val game = prefs.currentGame
        worker.execute { runCatching { Cleaner.sweep(this, prefs, game) } }
    }

    // ---------------------------------------------------------- notificaciones

    private fun ensureChannels() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CH_ACTIVE, "Modo juego activo", NotificationManager.IMPORTANCE_LOW),
        )
        nm.createNotificationChannel(
            NotificationChannel(CH_SUMMARY, "Resumen al terminar", NotificationManager.IMPORTANCE_DEFAULT),
        )
    }

    private fun activeNotification(): Notification {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE,
        )
        val stop = PendingIntent.getService(
            this, 1, Intent(this, GameService::class.java).setAction(ACTION_STOP), PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CH_ACTIVE)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle("Modo juego activo")
            .setContentText("Segundo plano bajo control")
            .setContentIntent(open)
            .addAction(0, "Detener", stop)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    /** Al terminar, avisa cuántas notificaciones se ocultaron y de qué apps, para no perderse nada. */
    private fun postSummary() {
        val blocked = GameState.blocked.toMap()
        if (blocked.isEmpty()) return
        val pm = packageManager
        val lines = blocked.entries
            .sortedByDescending { it.value }
            .take(6)
            .map { (pkg, count) ->
                val label = runCatching {
                    pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                }.getOrDefault(pkg)
                "$label  ×$count"
            }
        val total = blocked.values.sum()
        val notif = NotificationCompat.Builder(this, CH_SUMMARY)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle("Mientras jugabas: $total avisos ocultos")
            .setContentText(lines.joinToString("  ·  "))
            .setStyle(NotificationCompat.BigTextStyle().bigText(lines.joinToString("\n")))
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(this).notify(NOTIF_SUMMARY, notif)
        } catch (_: SecurityException) {
            // Sin permiso de notificaciones: se omite el resumen.
        }
    }
}
