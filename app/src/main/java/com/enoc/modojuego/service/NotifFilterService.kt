package com.enoc.modojuego.service

import android.app.Notification
import android.os.SystemClock
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.enoc.modojuego.core.AppRepo
import com.enoc.modojuego.core.Cleaner
import com.enoc.modojuego.core.GameState
import com.enoc.modojuego.data.Prefs

/**
 * Oculta las notificaciones de apps de usuario que no están en MANTENER mientras el modo juego está activo.
 * Siempre deja pasar llamadas, alarmas, teléfono, teclado y launcher.
 */
class NotifFilterService : NotificationListenerService() {
    private var cache: Set<String> = emptySet()
    private var cacheAt = 0L

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!GameState.running.value) return
        val prefs = Prefs(this)
        if (!prefs.filter) return

        val category = sbn.notification.category
        if (category == Notification.CATEGORY_CALL || category == Notification.CATEGORY_ALARM) return

        val pkg = sbn.packageName
        if (pkg in Cleaner.protectedPackages(this, prefs, prefs.currentGame)) return
        if (pkg !in userApps()) return

        cancelNotification(sbn.key)
        GameState.blocked.merge(pkg, 1) { a, b -> a + b }
    }

    private fun userApps(): Set<String> {
        val now = SystemClock.elapsedRealtime()
        if (now - cacheAt > 60_000) {
            cache = AppRepo.launchable(this, labels = false).filter { it.userFacing }.map { it.pkg }.toSet()
            cacheAt = now
        }
        return cache
    }
}
