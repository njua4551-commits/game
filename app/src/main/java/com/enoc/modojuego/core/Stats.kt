package com.enoc.modojuego.core

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

data class Snap(
    val freeGb: Float,
    val usedGb: Float,
    val totalGb: Float,
    val tempC: Float,
    val batt: Int,
)

object Stats {
    private const val GB = 1024f * 1024f * 1024f

    fun read(ctx: Context): Snap {
        val am = ctx.getSystemService(ActivityManager::class.java)
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)

        val bat: Intent? = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = bat?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = bat?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val temp = (bat?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f
        val pct = if (level >= 0 && scale > 0) level * 100 / scale else 0

        return Snap(
            freeGb = mi.availMem / GB,
            usedGb = (mi.totalMem - mi.availMem) / GB,
            totalGb = mi.totalMem / GB,
            tempC = temp,
            batt = pct,
        )
    }
}
