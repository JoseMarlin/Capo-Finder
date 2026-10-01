package com.dmacq.capofinder.data

import com.dmacq.capofinder.engine.Mode

data class KeySelection(val root: Int, val mode: Mode)

data class UiState(
    val current: KeySelection,
    val recent: List<KeySelection>,
    val known: Set<String>,
    /** null = follow the system light/dark setting. */
    val darkOverride: Boolean?,
    val showChords: Boolean = false,
)
