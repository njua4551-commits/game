package com.enoc.modojuego.data

import android.app.NotificationManager
import android.content.Context

/** Preferencias simples. Se lee siempre fresco, así el servicio ve los cambios al instante. */
class Prefs(context: Context) {
    private val sp = context.applicationContext.getSharedPreferences("modojuego", Context.MODE_PRIVATE)

    /** Apps que el usuario marcó como juegos (aparecen en Inicio para lanzarlas). */
    var games: Set<String>
        get() = sp.getStringSet("games", emptySet())?.toSet() ?: emptySet()
        set(v) = sp.edit().putStringSet("games", v.toSet()).apply()

    /** Apps que no se cierran ni se silencian durante el modo juego. */
    var allowed: Set<String>
        get() = sp.getStringSet("allowed", emptySet())?.toSet() ?: emptySet()
        set(v) = sp.edit().putStringSet("allowed", v.toSet()).apply()

    var overlay: Boolean
        get() = sp.getBoolean("overlay", true)
        set(v) = sp.edit().putBoolean("overlay", v).apply()

    var dnd: Boolean
        get() = sp.getBoolean("dnd", false)
        set(v) = sp.edit().putBoolean("dnd", v).apply()

    var filter: Boolean
        get() = sp.getBoolean("filter", true)
        set(v) = sp.edit().putBoolean("filter", v).apply()

    var currentGame: String?
        get() = sp.getString("current_game", null)
        set(v) = sp.edit().putString("current_game", v).apply()

    // --- Política de No molestar previa, para devolverla tal cual al terminar ---
    var dndApplied: Boolean
        get() = sp.getBoolean("dnd_applied", false)
        set(v) = sp.edit().putBoolean("dnd_applied", v).apply()

    val prevCategories: Int get() = sp.getInt("p_cat", 0)
    val prevCalls: Int get() = sp.getInt("p_calls", 0)
    val prevMessages: Int get() = sp.getInt("p_msgs", 0)
    val prevEffects: Int get() = sp.getInt("p_fx", 0)
    val prevFilter: Int get() = sp.getInt("p_filter", NotificationManager.INTERRUPTION_FILTER_ALL)

    fun savePolicy(categories: Int, calls: Int, messages: Int, effects: Int, filter: Int) {
        sp.edit()
            .putInt("p_cat", categories)
            .putInt("p_calls", calls)
            .putInt("p_msgs", messages)
            .putInt("p_fx", effects)
            .putInt("p_filter", filter)
            .apply()
    }
}
