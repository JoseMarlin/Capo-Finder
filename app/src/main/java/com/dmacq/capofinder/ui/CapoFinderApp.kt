package com.dmacq.capofinder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dmacq.capofinder.data.CapoViewModel
import com.dmacq.capofinder.data.KeySelection
import com.dmacq.capofinder.data.UiState
import com.dmacq.capofinder.engine.CapoEngine
import com.dmacq.capofinder.engine.CapoOption
import com.dmacq.capofinder.engine.CapoResult
import com.dmacq.capofinder.engine.FLAT_NAMES
import com.dmacq.capofinder.engine.Mode
import com.dmacq.capofinder.engine.NOTE_NAMES
import com.dmacq.capofinder.engine.Unlock
import com.dmacq.capofinder.engine.keyLabel

@Composable
fun CapoFinderApp(vm: CapoViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val dark = state.darkOverride ?: isSystemInDarkTheme()

    CapoFinderTheme(dark = dark) {
        MainScreen(
            state = state,
            dark = dark,
            onSelectKey = vm::selectKey,
            onToggleChord = vm::toggleChord,
            onToggleChordsPanel = vm::toggleChordsPanel,
            onToggleDark = { vm.setDark(!dark) },
        )
    }
}

@Composable
fun MainScreen(
    state: UiState,
    dark: Boolean,
    onSelectKey: (Int, Mode) -> Unit,
    onToggleChord: (String) -> Unit,
    onToggleChordsPanel: () -> Unit,
    onToggleDark: () -> Unit,
) {
    val c = LocalAppColors.current
    val current = state.current
    val result = remember(current, state.known) {
        CapoEngine.recommend(current.root, current.mode, state.known)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .statusBarsPadding(),
    ) {
        Header(dark = dark, onToggleDark = onToggleDark)

        ResultCard(
            current = current,
            result = result,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        // Scrolls on its own so the answer above and the picker below stay put.
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (result is CapoResult.Match && result.alternatives.isNotEmpty()) {
                SectionLabel("Also works")
                result.alternatives.forEach { AlternativeRow(it) }
            }
            MyChords(
                known = state.known,
                expanded = state.showChords,
                onToggleExpanded = onToggleChordsPanel,
                onToggleChord = onToggleChord,
            )
        }

        BottomPicker(
            current = current,
            recent = state.recent,
            onSelectKey = onSelectKey,
        )
    }
}

@Composable
private fun Header(dark: Boolean, onToggleDark: () -> Unit) {
    val c = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Capo Finder",
            color = c.ink,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        ToggleChip(
            label = if (dark) "Light" else "Dark",
            selected = false,
            onClick = onToggleDark,
            modifier = Modifier.width(72.dp),
            height = 44.dp,
            shape = RoundedCornerShape(12.dp),
            accessibilityLabel = if (dark) "Switch to light mode" else "Switch to dark mode",
        )
    }
}

@Composable
private fun ResultCard(current: KeySelection, result: CapoResult, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    val shape = RoundedCornerShape(18.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(c.card)
            .border(1.dp, c.line, shape)
            .padding(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SectionLabel("Lead calls ${keyLabel(current.root)} ${current.mode.displayName}")
        when (result) {
            is CapoResult.Match -> MatchContent(result, current.mode)
            is CapoResult.NoMatch -> NoMatchContent(result.unlock)
        }
    }
}

private fun capoText(capo: Int) = if (capo == 0) "No capo" else "Capo $capo"

@Composable
private fun MatchContent(result: CapoResult.Match, mode: Mode) {
    val c = LocalAppColors.current
    val best = result.best
    val why = when {
        result.onlyOption -> "Only family you fully know"
        best.capo == 0 -> "All known, no capo"
        else -> "All known, lowest capo"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = capoText(best.capo),
            color = c.accentText,
            fontSize = 46.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 48.sp,
        )
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = best.familyName, color = c.ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(text = why, color = c.sub, fontSize = 12.sp)
        }
    }

    // Top: the chord the band hears. Bottom: the shape you play.
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        best.shapes.forEachIndexed { i, shape ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(c.tile)
                    .padding(horizontal = 4.dp, vertical = 6.dp)
                    .semantics(mergeDescendants = true) {},
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "${mode.degrees[i]} \u00B7 ${best.sounds[i]}",
                    color = c.sub,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(text = "\u2193", color = c.sub, fontSize = 12.sp)
                Text(text = shape, color = c.accentText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    Fretboard(capo = best.capo)
}

@Composable
private fun NoMatchContent(unlock: Unlock?) {
    val c = LocalAppColors.current
    val hint = if (unlock != null) {
        "Learn ${unlock.chord} to unlock ${capoText(unlock.capo).lowercase()} with the ${unlock.familyName}."
    } else {
        "Every option is missing two or more chords. Add more chords in My chords to get a match."
    }
    Column(
        modifier = Modifier.padding(top = 6.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "No match yet", color = c.ink, fontSize = 34.sp, fontWeight = FontWeight.Bold, lineHeight = 38.sp)
        Text(text = hint, color = c.ink, fontSize = 15.sp)
        Text(
            text = "Only capo positions up to fret ${CapoEngine.MAX_CAPO} are considered.",
            color = c.sub,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun AlternativeRow(option: CapoOption) {
    val c = LocalAppColors.current
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(c.card)
            .border(1.dp, c.line, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = capoText(option.capo),
            modifier = Modifier.width(74.dp),
            color = c.accentText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = option.familyName, color = c.ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = option.shapes.joinToString(" \u00B7 "), color = c.sub, fontSize = 12.sp)
        }
    }
}

@Composable
private fun MyChords(
    known: Set<String>,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onToggleChord: (String) -> Unit,
) {
    val c = LocalAppColors.current
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(shape)
            .background(c.card)
            .border(1.dp, c.line, shape)
            .clickable(role = Role.Button, onClick = onToggleExpanded)
            .semantics { stateDescription = if (expanded) "Expanded" else "Collapsed" }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row {
            Text(text = "My chords ", color = c.ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(text = "(${known.size} known)", color = c.sub, fontSize = 15.sp)
        }
        Text(
            text = if (expanded) "Hide" else "Edit",
            color = c.accentText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }

    if (expanded) {
        Text(
            text = "Tap a chord to add or remove it. Recommendations only use chords marked as known.",
            color = c.sub,
            fontSize = 13.sp,
        )
        val all = NOTE_NAMES.map { it } + NOTE_NAMES.map { it + "m" }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            all.chunked(6).forEach { rowChords ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    rowChords.forEach { chord ->
                        ToggleChip(
                            label = chord,
                            selected = chord in known,
                            onClick = { onToggleChord(chord) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

/** Recent keys, Major/Minor and the 12 root notes sit at the bottom, within thumb reach. */
@Composable
private fun BottomPicker(
    current: KeySelection,
    recent: List<KeySelection>,
    onSelectKey: (Int, Mode) -> Unit,
) {
    val c = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(c.card)
            .navigationBarsPadding()
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (recent.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Recent")
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    recent.forEach { key ->
                        val name = NOTE_NAMES[key.root] + key.mode.suffix
                        ToggleChip(
                            label = name,
                            selected = false,
                            onClick = { onSelectKey(key.root, key.mode) },
                            shape = RoundedCornerShape(999.dp),
                            idleBackground = c.tile,
                            accessibilityLabel = "${keyLabel(key.root)} ${key.mode.displayName}",
                        )
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToggleChip(
                label = "Major",
                selected = current.mode == Mode.MAJOR,
                onClick = { onSelectKey(current.root, Mode.MAJOR) },
                modifier = Modifier.weight(1f),
                fontSize = 15.sp,
            )
            ToggleChip(
                label = "Minor",
                selected = current.mode == Mode.MINOR,
                onClick = { onSelectKey(current.root, Mode.MINOR) },
                modifier = Modifier.weight(1f),
                fontSize = 15.sp,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            (0..11).chunked(6).forEach { rowRoots ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    rowRoots.forEach { root ->
                        val sharp = NOTE_NAMES[root]
                        val flat = FLAT_NAMES[root]
                        ToggleChip(
                            label = if (sharp == flat) sharp else "$sharp\n$flat",
                            selected = current.root == root,
                            onClick = { onSelectKey(root, current.mode) },
                            modifier = Modifier.weight(1f),
                            height = 46.dp,
                            fontSize = 13.sp,
                            accessibilityLabel = if (sharp == flat) sharp else "$sharp, also called $flat",
                        )
                    }
                }
            }
        }
    }
}
