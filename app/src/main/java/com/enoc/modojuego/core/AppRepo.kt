package com.enoc.modojuego.core

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo

/**
 * userFacing = app instalada por el usuario o app de sistema actualizada (Chrome, YouTube...).
 * Las apps de sistema puras nunca se tocan.
 */
data class AppItem(val pkg: String, val label: String, val userFacing: Boolean)

object AppRepo {
    fun launchable(ctx: Context, labels: Boolean = true): List<AppItem> {
        val pm = ctx.packageManager
        val q = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(q, 0)
            .map { ri ->
                val info = ri.activityInfo.applicationInfo
                val system = info.flags and ApplicationInfo.FLAG_SYSTEM != 0
                val updated = info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
                AppItem(
                    pkg = info.packageName,
                    label = if (labels) ri.loadLabel(pm).toString() else info.packageName,
                    userFacing = !system || updated,
                )
            }
            .filter { it.pkg != ctx.packageName }
            .distinctBy { it.pkg }
            .sortedBy { it.label.lowercase() }
    }
}
