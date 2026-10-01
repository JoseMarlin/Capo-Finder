package com.dmacq.capofinder.data

import android.content.Context
import com.dmacq.capofinder.engine.CapoEngine
import com.dmacq.capofinder.engine.Mode

/** Saves the player's known chords, last key, recent keys and theme on the device. */
class CapoStore(context: Context) {
    private val prefs = context.getSharedPreferences("capo_finder", Context.MODE_PRIVATE)

    fun load(): UiState {
        val known = prefs.getStringSet(KEY_KNOWN, null)?.toSet() ?: CapoEngine.DEFAULT_KNOWN
        val root = prefs.getInt(KEY_ROOT, DEFAULT_ROOT).coerceIn(0, 11)
        val mode = if (prefs.getBoolean(KEY_MINOR, false)) Mode.MINOR else Mode.MAJOR
        val dark = when (prefs.getInt(KEY_DARK, -1)) {
            0 -> false
            1 -> true
            else -> null
        }
        return UiState(
            current = KeySelection(root, mode),
            recent = decodeRecent(prefs.getString(KEY_RECENT, "").orEmpty()),
            known = known,
            darkOverride = dark,
        )
    }

    fun save(state: UiState) {
        val darkCode = when (state.darkOverride) {
            false -> 0
            true -> 1
            null -> -1
        }
        prefs.edit()
            .putStringSet(KEY_KNOWN, state.known.toSet())
            .putInt(KEY_ROOT, state.current.root)
            .putBoolean(KEY_MINOR, state.current.mode == Mode.MINOR)
            .putString(KEY_RECENT, encodeRecent(state.recent))
            .putInt(KEY_DARK, darkCode)
            .apply()
    }

    private fun encodeRecent(list: List<KeySelection>): String =
        list.joinToString(",") { "${it.root}:${if (it.mode == Mode.MINOR) 1 else 0}" }

    private fun decodeRecent(raw: String): List<KeySelection> =
        raw.split(",").mapNotNull { part ->
            val bits = part.split(":")
            val root = bits.getOrNull(0)?.toIntOrNull()?.takeIf { it in 0..11 }
                ?: return@mapNotNull null
            val mode = if (bits.getOrNull(1) == "1") Mode.MINOR else Mode.MAJOR
            KeySelection(root, mode)
        }

    private companion object {
        const val KEY_KNOWN = "known_chords"
        const val KEY_ROOT = "root"
        const val KEY_MINOR = "minor"
        const val KEY_RECENT = "recent"
        const val KEY_DARK = "dark"
        const val DEFAULT_ROOT = 10 // A#/Bb
    }
}
