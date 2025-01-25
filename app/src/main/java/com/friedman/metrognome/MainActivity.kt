package com.friedman.metrognome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.compose.MetrognomeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            MetrognomeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MetrognomeScreen()
                }


            }
        }
//        binding.fab.setOnClickListener { view ->
//            if (isPlaying) {
//                AudioPlayer.startPlayback(false)
//                isPlaying = false
//            } else {
//
//
//                AudioPlayer.startPlayback(true)
//                AudioPlayer.setMeasures(mMeasures)
//                AudioPlayer.setBpm(binding.bpm.text.toString().toInt())
//                isPlaying = true
//            }
//            binding.fab.setImageResource(if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)
//        }
//        binding.seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
//            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
//                binding.bpm.setText("$p1")
//            }
//
//            override fun onStartTrackingTouch(p0: SeekBar?) {
//
//            }
//
//            override fun onStopTrackingTouch(p0: SeekBar?) {
//                var bpm = binding.bpm.text.toString()
//                val bpmInt = if (bpm == "") 120 else bpm.toInt()
//                if (isPlaying) {
//
//                    AudioPlayer.setBpm(if (bpmInt > 0) bpmInt else 120)
//                }
//                binding.seekBar.progress = bpmInt
//            }
//        }
//
//        )
//        binding.bpm.addTextChangedListener(
//
//
//            afterTextChanged = { s ->
//                run {
//                    if (isPlaying) {
//                        var bpm = s?.toString() ?: ""
//                        if (bpm == "") {
//                            bpm = "120"
//                        }
//                        val bpmInt = bpm.toInt()
//                        AudioPlayer.setBpm(if (bpmInt > 0) bpmInt else 120)
//                    }
//
//                }
//            }
//        )


    }



}
fun getMeasures(): List<Measure> {
    return listOf(
        Measure(6, 8, intArrayOf(3,3)),
        Measure(7, 4, intArrayOf(2,2,3)),
    )
}
@Composable
fun MetrognomeScreen(modifier: Modifier = Modifier) {
    val mMeasures = remember {getMeasures().toMutableStateList()}
    val mIsPlaying = remember { mutableStateOf(false) }
    val mBpm = remember { mutableStateOf(120) }
    Column(modifier = modifier.padding(16.dp)) {
       BPMConfig(modifier, bpm = mBpm.value, onValueChange = { it -> mBpm.value = it
           if (mIsPlaying.value) {

               AudioPlayer.setBpm(mBpm.value)
           }
       })
       MeasureElementList(measures = mMeasures, onClickMeasure = { it -> mMeasures.remove(it)
           if (mIsPlaying.value) {
               AudioPlayer.setMeasures(mMeasures.toList().toTypedArray())
           }
       })
       Button(
           onClick = {
               mMeasures.add(Measure(3, 4, intArrayOf(2, 2)))
               if (mIsPlaying.value) {
                   AudioPlayer.setMeasures(mMeasures.toList().toTypedArray())
               }
                     },
       ) {
           Text("Add Measure")
       }
        if (!mIsPlaying.value) {


            Button(
                onClick = {
                    AudioPlayer.startPlayback(true)
                    AudioPlayer.setMeasures(mMeasures.toList().toTypedArray())
                    AudioPlayer.setBpm(mBpm.value)
                    mIsPlaying.value = true
                }
            ) {
                Text("Start")
            }
        } else {
            Button(
                onClick = {
                    AudioPlayer.startPlayback(false)
                    mIsPlaying.value = false
                }
            ) {
                Text("Stop")
            }
        }
    }
}
@Composable
fun BPMConfig(modifier: Modifier = Modifier, bpm: Int, onValueChange: (Int) -> Unit) {
    Column(modifier = modifier.padding(16.dp).fillMaxWidth()) {
        Text("$bpm BPM", modifier = modifier.padding(16.dp).fillMaxWidth(), textAlign = TextAlign.Center)
        Slider(bpm / 500f, onValueChange = { onValueChange((it * 500).toInt()) })
    }
}
@Composable
fun MeasureElement(measure: Measure, onClick: () -> Unit = {}) {
        Button(onClick = onClick) {
            Column {
        Text(text = measure.timeSignatureTop.toString())
        Text(text = measure.timeSignatureBottom.toString())
    }
        }
}
@Composable
fun MeasureElementList(measures: List<Measure>, onClickMeasure: (Measure) -> Unit = {}) {
    LazyRow {
        items(measures) { measure ->
            MeasureElement(measure = measure, onClick = { onClickMeasure(measure) })
        }


    }
}