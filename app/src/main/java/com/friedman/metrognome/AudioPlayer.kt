package com.friedman.metrognome

import android.util.Log
interface MetronomeCallback {

    fun onMetronomeClick(measure: Int, beat: Int)
}
object AudioPlayer {
    init {
        System.loadLibrary("metrognome")
    }
    fun startPlayback(shouldStart: Boolean, metronomeCallback: MetronomeCallback) {
        if (shouldStart) {
            startAudioStreamNative(metronomeCallback)
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
    private external fun startAudioStreamNative(metronomeCallback: MetronomeCallback): Int
    private external fun stopAudioStreamNative(): Int
    private external fun audioSetBpm(bpm: Int): Int
    private external fun setMeasuresNative(measures: Array<Measure>): Int
}