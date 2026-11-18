package com.enoc.modojuego

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.enoc.modojuego.core.Dnd
import com.enoc.modojuego.core.GameState
import com.enoc.modojuego.data.Prefs
import com.enoc.modojuego.ui.ModoJuegoTheme
import com.enoc.modojuego.ui.Root

class MainActivity : ComponentActivity() {

    private val notifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Prefs(this)

        // Si el proceso murió con No molestar aplicado, devuelve la política original.
        if (!GameState.running.value) Dnd.restore(this, prefs)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            ModoJuegoTheme {
                Root(prefs) { requestNotif() }
            }
        }
    }

    private fun requestNotif() {
        if (Build.VERSION.SDK_INT >= 33) notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
