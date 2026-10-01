package com.dmacq.capofinder.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dmacq.capofinder.engine.Mode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CapoViewModel(app: Application) : AndroidViewModel(app) {
    private val store = CapoStore(app)
    private val _state = MutableStateFlow(store.load())
    val state: StateFlow<UiState> = _state.asStateFlow()

    /** The key you leave becomes the newest "Recent" chip. */
    fun selectKey(root: Int, mode: Mode) {
        val s = _state.value
        val next = KeySelection(root, mode)
        if (next == s.current) return
        val recent = (listOf(s.current) + s.recent)
            .filter { it != next }
            .distinct()
            .take(MAX_RECENT)
        commit(s.copy(current = next, recent = recent))
    }

    fun toggleChord(name: String) {
        val s = _state.value
        val known = if (name in s.known) s.known - name else s.known + name
        commit(s.copy(known = known))
    }

    fun toggleChordsPanel() = _state.update { it.copy(showChords = !it.showChords) }

    fun setDark(dark: Boolean) = commit(_state.value.copy(darkOverride = dark))

    private fun commit(new: UiState) {
        _state.value = new
        store.save(new)
    }

    private companion object {
        const val MAX_RECENT = 5
    }
}
