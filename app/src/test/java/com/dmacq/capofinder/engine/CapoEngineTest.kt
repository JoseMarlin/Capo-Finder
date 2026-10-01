package com.dmacq.capofinder.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CapoEngineTest {
    private val known = CapoEngine.DEFAULT_KNOWN

    private fun match(root: Int, mode: Mode, chords: Set<String> = known) =
        CapoEngine.recommend(root, mode, chords) as CapoResult.Match

    @Test
    fun aSharpMajor_usesCapo3WithGFamily() {
        val best = match(10, Mode.MAJOR).best
        assertEquals(3, best.capo)
        assertEquals("G family", best.familyName)
        assertEquals(listOf("G", "C", "D", "Em"), best.shapes)
        assertEquals(listOf("A#", "D#", "F", "Gm"), best.sounds)
    }

    @Test
    fun aSharpMajor_rejectsAFamilyBecauseF_sharpMinorIsUnknown() {
        val result = match(10, Mode.MAJOR)
        assertTrue(result.onlyOption)
        assertTrue(result.alternatives.isEmpty())
    }

    @Test
    fun fSharpMajor_usesCapo4WithDFamily() {
        val best = match(6, Mode.MAJOR).best
        assertEquals(4, best.capo)
        assertEquals("D family", best.familyName)
    }

    @Test
    fun dMajor_needsNoCapo_andListsCFamilyAtCapo2() {
        val result = match(2, Mode.MAJOR)
        assertEquals(0, result.best.capo)
        assertEquals("D family", result.best.familyName)
        assertTrue(result.alternatives.any { it.capo == 2 && it.familyName == "C family" })
    }

    @Test
    fun aSharpMinor_usesCapo1WithAmFamily() {
        val best = match(10, Mode.MINOR).best
        assertEquals(1, best.capo)
        assertEquals("Am family", best.familyName)
    }

    @Test
    fun capoAboveFret7_isIgnored() {
        // C family would need capo 10 for A#, which must never be offered.
        val result = match(10, Mode.MAJOR)
        assertTrue((listOf(result.best) + result.alternatives).all { it.capo <= CapoEngine.MAX_CAPO })
    }

    @Test
    fun noMatch_namesTheSingleChordThatUnlocksTheBestOption() {
        val result = CapoEngine.recommend(1, Mode.MAJOR, setOf("C", "F", "G")) as CapoResult.NoMatch
        assertEquals(Unlock(chord = "Am", capo = 1, familyName = "C family"), result.unlock)
    }

    @Test
    fun noMatch_withTooManyMissingChords_hasNoSingleUnlock() {
        val result = CapoEngine.recommend(1, Mode.MAJOR, setOf("C")) as CapoResult.NoMatch
        assertNull(result.unlock)
    }

    @Test
    fun keyLabel_showsBothSpellingsForBlackKeys() {
        assertEquals("A#/Bb", keyLabel(10))
        assertEquals("D", keyLabel(2))
    }
}
