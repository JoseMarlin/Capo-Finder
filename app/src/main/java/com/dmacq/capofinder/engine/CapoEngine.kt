package com.dmacq.capofinder.engine

/** Note names with C = 0. Sharps are used for chord names; flats are shown beside the key picker. */
val NOTE_NAMES = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
val FLAT_NAMES = listOf("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B")

fun mod12(n: Int): Int = ((n % 12) + 12) % 12

/** "A#/Bb" for black keys, plain "D" for white keys. */
fun keyLabel(root: Int): String =
    if (NOTE_NAMES[root] == FLAT_NAMES[root]) NOTE_NAMES[root] else "${NOTE_NAMES[root]}/${FLAT_NAMES[root]}"

/**
 * The four main chords of a key and the shape families that can play it.
 * Offsets are semitones above the key's root.
 */
enum class Mode(
    val displayName: String,
    val suffix: String,
    val offsets: List<Int>,
    val qualities: List<String>,
    val degrees: List<String>,
    val homeRoot: Int,
    val familyRoots: List<Int>,
) {
    MAJOR(
        displayName = "major",
        suffix = "",
        offsets = listOf(0, 5, 7, 9),
        qualities = listOf("", "", "", "m"),
        degrees = listOf("I", "IV", "V", "vi"),
        homeRoot = 0, // C family is the home family
        familyRoots = listOf(0, 7, 2, 9, 4), // C, G, D, A, E
    ),
    MINOR(
        displayName = "minor",
        suffix = "m",
        offsets = listOf(0, 5, 7, 8),
        qualities = listOf("m", "m", "m", ""),
        degrees = listOf("i", "iv", "v", "VI"),
        homeRoot = 9, // Am family is the home family
        familyRoots = listOf(9, 4, 2), // Am, Em, Dm
    ),
}

/** One way to play the key: put the capo on [capo] and play [shapes]. */
data class CapoOption(
    val capo: Int,
    val familyName: String,
    val shapes: List<String>,
    val sounds: List<String>,
    val missing: List<String>,
    val isHome: Boolean,
)

/** "Learn [chord] to unlock capo [capo] with the [familyName]." */
data class Unlock(val chord: String, val capo: Int, val familyName: String)

sealed interface CapoResult {
    data class Match(
        val best: CapoOption,
        val alternatives: List<CapoOption>,
        val onlyOption: Boolean,
    ) : CapoResult

    data class NoMatch(val unlock: Unlock?) : CapoResult
}

object CapoEngine {
    /** Capo positions above this fret are hard to play and sound thin, so they are ignored. */
    const val MAX_CAPO = 7
    const val MAX_ALTERNATIVES = 2

    val DEFAULT_KNOWN: Set<String> = setOf(
        "C", "D", "E", "F", "G", "A", "B",
        "Dm", "Em", "Fm", "Am", "Bm",
    )

    private fun chord(pitchClass: Int, quality: String) = NOTE_NAMES[mod12(pitchClass)] + quality

    /**
     * capo fret = (target root - shape root) mod 12.
     * Keeps only families whose four chords are all known, lowest capo first.
     */
    fun recommend(root: Int, mode: Mode, known: Set<String>): CapoResult {
        val options = mode.familyRoots.map { familyRoot ->
            val shapes = mode.offsets.mapIndexed { i, off -> chord(familyRoot + off, mode.qualities[i]) }
            val sounds = mode.offsets.mapIndexed { i, off -> chord(root + off, mode.qualities[i]) }
            CapoOption(
                capo = mod12(root - familyRoot),
                familyName = NOTE_NAMES[familyRoot] + mode.suffix + " family",
                shapes = shapes,
                sounds = sounds,
                missing = shapes.filterNot { it in known },
                isHome = familyRoot == mode.homeRoot,
            )
        }.filter { it.capo <= MAX_CAPO }

        val playable = options
            .filter { it.missing.isEmpty() }
            .sortedWith(compareBy<CapoOption> { it.capo }.thenBy { if (it.isHome) 0 else 1 })

        if (playable.isNotEmpty()) {
            return CapoResult.Match(
                best = playable.first(),
                alternatives = playable.drop(1).take(MAX_ALTERNATIVES),
                onlyOption = playable.size == 1,
            )
        }

        val closest = options.filter { it.missing.size == 1 }.minByOrNull { it.capo }
        return CapoResult.NoMatch(
            closest?.let { Unlock(it.missing.first(), it.capo, it.familyName) }
        )
    }
}
