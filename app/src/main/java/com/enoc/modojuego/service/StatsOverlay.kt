package com.enoc.modojuego.service

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.enoc.modojuego.core.Stats
import java.util.Locale

/** Contador flotante sin interacción: RAM usada y batería. Vistas clásicas para no depender de Compose en un servicio. */
class StatsOverlay(private val ctx: Context) {
    private val red = 0xFFE5202B.toInt()
    private val wm = ctx.getSystemService(WindowManager::class.java)
    private val density = ctx.resources.displayMetrics.density
    private var shown = false

    private fun dp(v: Int) = (v * density).toInt()

    private val tv = TextView(ctx).apply {
        typeface = Typeface.MONOSPACE
        textSize = 11f
        setTextColor(0xFFECECEE.toInt())
        setPadding(dp(10), dp(6), dp(10), dp(6))
        background = GradientDrawable().apply {
            setColor(0xE60B0B0C.toInt())
            setStroke(dp(1), 0xFF2A2A2E.toInt())
        }
    }

    private val lp = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT,
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = dp(12)
        y = dp(28)
    }

    fun show() {
        if (shown || !Settings.canDrawOverlays(ctx)) return
        update()
        try {
            wm.addView(tv, lp)
            shown = true
        } catch (_: Exception) {
            shown = false
        }
    }

    fun hide() {
        if (!shown) return
        try {
            wm.removeView(tv)
        } catch (_: Exception) {
        }
        shown = false
    }

    fun update() {
        val s = Stats.read(ctx)
        tv.text = buildSpannedString {
            color(red) { append("RAM ") }
            append(String.format(Locale.US, "%.1f/%.0fG", s.usedGb, s.totalGb))
            append("   ")
            color(red) { append("BAT ") }
            append(String.format(Locale.US, "%.0f°  %d%%", s.tempC, s.batt))
        }
    }
}
