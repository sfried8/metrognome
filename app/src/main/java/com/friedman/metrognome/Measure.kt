package com.friedman.metrognome

val SILENT = 0
val STRONG_ACCENT = 1
val ACCENT = 2
val UNACCENT = 3
val DUPLE = 2
val TRIPLE = 3
data class Measure (val timeSignatureTop: Int, val timeSignatureBottom: Int, @JvmField val accentPattern: IntArray, val voiceEighths: Boolean) {
}
