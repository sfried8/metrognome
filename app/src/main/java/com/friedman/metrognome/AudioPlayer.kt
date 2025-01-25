package com.friedman.metrognome

object AudioPlayer {
    init {
        System.loadLibrary("metrognome")
    }
    fun startPlayback(shouldStart: Boolean) {
        if (shouldStart) {
            startAudioStreamNative()
        } else {
            stopAudioStreamNative()
        }

    }

    fun setBpm(bpm: Int) {
        audioSetBpm(bpm)
    }
    fun setMeasures(measures : Array<Measure>) {
        setMeasuresNative(measures)
    }
    private external fun startAudioStreamNative(): Int
    private external fun stopAudioStreamNative(): Int
    private external fun audioSetBpm(bpm: Int): Int
    private external fun setMeasuresNative(measures: Array<Measure>): Int
}