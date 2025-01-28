package com.friedman.metrognome

import android.util.Log

val SILENT = 0
val STRONG_ACCENT = 1
val ACCENT = 2
val UNACCENT = 3
val DUPLE = 2
val TRIPLE = 3
class Measure (val timeSignatureTop: Int, val timeSignatureBottom: Int, @JvmField val accentPattern: IntArray, val voiceEighths: Boolean) {
    fun beatTick(beat: Int) {
        Log.d("Measure", "beatTick: $beat")
    }
    var fairyDotState = FairyDotState()
}
