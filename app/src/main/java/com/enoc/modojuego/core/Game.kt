package com.enoc.modojuego.core

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.enoc.modojuego.service.GameService
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.ConcurrentHashMap

/** Estado en memoria: si el proceso muere, el modo juego también termina (y los filtros dejan de actuar). */
object GameState {
    val running = MutableStateFlow(false)

    /** Avisos bloqueados durante esta sesión, por paquete. */
    val blocked = ConcurrentHashMap<String, Int>()
}

object Game {
    /** Activa el modo juego. Si ya estaba activo, solo actualiza el juego y reaplica las opciones. */
    fun start(ctx: Context, game: String?) {
        val i = Intent(ctx, GameService::class.java)
            .setAction(GameService.ACTION_START)
            .putExtra(GameService.EXTRA_GAME, game)
        ContextCompat.startForegroundService(ctx, i)
    }

    fun stop(ctx: Context) {
        ctx.startService(Intent(ctx, GameService::class.java).setAction(GameService.ACTION_STOP))
    }
}
