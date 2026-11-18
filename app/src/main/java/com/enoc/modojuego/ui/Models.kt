package com.enoc.modojuego.ui

import com.enoc.modojuego.core.AppItem

data class Perms(
    val notifications: Boolean,
    val overlay: Boolean,
    val dnd: Boolean,
    val listener: Boolean,
)

data class Options(
    val overlay: Boolean,
    val dnd: Boolean,
    val filter: Boolean,
)

enum class Perm { NOTIF, OVERLAY, DND, LISTENER }

class Actions(
    val toggle: () -> Unit,
    val play: (AppItem) -> Unit,
    val setOverlay: (Boolean) -> Unit,
    val setDnd: (Boolean) -> Unit,
    val setFilter: (Boolean) -> Unit,
    val grant: (Perm) -> Unit,
)
