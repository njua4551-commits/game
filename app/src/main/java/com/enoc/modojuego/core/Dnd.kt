package com.enoc.modojuego.core

import android.app.NotificationManager
import android.content.Context
import com.enoc.modojuego.data.Prefs

/** No molestar "solo llamadas y alarmas". Guarda la política anterior y la devuelve al terminar. */
object Dnd {
    private fun nm(ctx: Context): NotificationManager =
        ctx.getSystemService(NotificationManager::class.java)

    fun granted(ctx: Context): Boolean = nm(ctx).isNotificationPolicyAccessGranted

    fun apply(ctx: Context, p: Prefs) {
        val n = nm(ctx)
        if (!n.isNotificationPolicyAccessGranted) return

        if (!p.dndApplied) {
            val cur = n.notificationPolicy
            p.savePolicy(
                cur.priorityCategories,
                cur.priorityCallSenders,
                cur.priorityMessageSenders,
                cur.suppressedVisualEffects,
                n.currentInterruptionFilter,
            )
            p.dndApplied = true
        }

        n.notificationPolicy = NotificationManager.Policy(
            NotificationManager.Policy.PRIORITY_CATEGORY_CALLS or
                NotificationManager.Policy.PRIORITY_CATEGORY_REPEAT_CALLERS or
                NotificationManager.Policy.PRIORITY_CATEGORY_ALARMS,
            NotificationManager.Policy.PRIORITY_SENDERS_ANY,
            NotificationManager.Policy.PRIORITY_SENDERS_ANY,
            NotificationManager.Policy.SUPPRESSED_EFFECT_PEEK or
                NotificationManager.Policy.SUPPRESSED_EFFECT_LIGHTS,
        )
        n.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
    }

    fun restore(ctx: Context, p: Prefs) {
        if (!p.dndApplied) return
        val n = nm(ctx)
        if (n.isNotificationPolicyAccessGranted) {
            n.notificationPolicy = NotificationManager.Policy(
                p.prevCategories,
                p.prevCalls,
                p.prevMessages,
                p.prevEffects,
            )
            n.setInterruptionFilter(p.prevFilter)
        }
        p.dndApplied = false
    }
}
