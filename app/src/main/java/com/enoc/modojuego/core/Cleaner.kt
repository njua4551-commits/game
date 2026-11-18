package com.enoc.modojuego.core

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.telecom.TelecomManager
import com.enoc.modojuego.data.Prefs

object Cleaner {

    /** Paquetes que jamás se cierran ni se silencian: esta app, MANTENER, launcher, teléfono y teclado. */
    fun protectedPackages(ctx: Context, prefs: Prefs, extra: String? = null): Set<String> {
        val pm = ctx.packageManager
        val s = HashSet<String>()
        s.add(ctx.packageName)
        s.addAll(prefs.allowed)
        extra?.let { s.add(it) }

        pm.resolveActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
            PackageManager.MATCH_DEFAULT_ONLY,
        )?.activityInfo?.packageName?.let { s.add(it) }

        ctx.getSystemService(TelecomManager::class.java)?.defaultDialerPackage?.let { s.add(it) }

        Settings.Secure.getString(ctx.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
            ?.substringBefore('/')
            ?.let { s.add(it) }

        return s
    }

    /**
     * Pide al sistema cerrar los procesos en segundo plano de cada app de usuario no protegida.
     * Devuelve cuántas apps se procesaron (no cuántas estaban realmente vivas).
     */
    fun sweep(ctx: Context, prefs: Prefs, keepGame: String?): Int {
        val am = ctx.getSystemService(ActivityManager::class.java)
        val safe = protectedPackages(ctx, prefs, keepGame)
        var n = 0
        for (app in AppRepo.launchable(ctx, labels = false)) {
            if (!app.userFacing || app.pkg in safe) continue
            am.killBackgroundProcesses(app.pkg)
            n++
        }
        return n
    }
}
