package com.friedman.metrognome

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.compose.MetrognomeTheme
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
fun measureFromEighthNoteGroupings(eighthNoteGroupings: IntArray): Measure {
    var sum = 0
    var isCompound = false
    for (eighthNoteGrouping in eighthNoteGroupings) {
        sum += eighthNoteGrouping
        if (eighthNoteGrouping == 3) {
            isCompound = true
        }
    }
    if (isCompound) {
        return Measure(sum, 8, eighthNoteGroupings, true)
    }
    return Measure(sum / 2, 4, eighthNoteGroupings, true)
}
fun getEighthNoteGrouping(timeSignatureTop: Int) : IntArray {
    var beatsLeft = timeSignatureTop
    val groupings : MutableList<Int> = mutableListOf()
    while (beatsLeft > 0) {
        if (beatsLeft == 2) {
            groupings.add(2)
            return groupings.toIntArray()
        } else if (beatsLeft == 4) {
            groupings.add(2)
            groupings.add(2)
            return groupings.toIntArray()
        } else {
            groupings.add(3)
            beatsLeft -= 3
        }
    }
    return groupings.toIntArray()
}
fun getMeasures(): List<Measure> {
    return listOf(
    )
}
@Composable
fun MetrognomeScreen(modifier: Modifier = Modifier) {
    val mMeasures = remember {getMeasures().toMutableStateList()}
    val mIsPlaying = remember { mutableStateOf(false) }
    val mBpm = remember { mutableStateOf(120) }
    val mActiveMeasure = remember { mutableStateOf(0) }
    val mShowingComposeDialog = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val metronomeCallback = object : MetronomeCallback {
        override fun onMetronomeClick(measure: Int, beat: Int) {
            mActiveMeasure.value = measure
            if (beat == 0) {
                val measureObject = mMeasures[measure]
                Log.d("measure", "$measure : $beat")
                Log.d("measure", measureObject.timeSignatureTop.toString())
                val bps = mBpm.value / 60f
                val mspb = 1000 / bps
                val animationDuration = mspb * measureObject.timeSignatureTop
                scope.launch {  measureObject.fairyDotState.play(animationDuration.toInt())}
            }
        }
    }
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
       }, activeMeasure = mActiveMeasure.value)
        Button(onClick = { mShowingComposeDialog.value = true}) {
            Text("Add Measure")
        }
        if (mShowingComposeDialog.value) {
            MeasureComposerDialog(onSumbit = {
                mMeasures.add(it)
               if (mIsPlaying.value)
                AudioPlayer.setMeasures(mMeasures.toList().toTypedArray())
                mShowingComposeDialog.value = false
            }, onDismiss = { mShowingComposeDialog.value = false})
        }
//       MeasureComposer(onSumbit = { it -> mMeasures.add(it)}, modifier = modifier)
        if (!mIsPlaying.value) {


            Button(
                onClick = {
                    AudioPlayer.startPlayback(true, metronomeCallback)
                    AudioPlayer.setMeasures(mMeasures.toList().toTypedArray())
                    AudioPlayer.setBpm(mBpm.value)
                    mIsPlaying.value = true
                }
            ) {
                Image(
                    painter = painterResource(android.R.drawable.ic_media_play),
                    contentDescription = "play"
                )
            }
        } else {
            Button(
                onClick = {
                    AudioPlayer.startPlayback(false, metronomeCallback)
                    mIsPlaying.value = false
                }
            ) {
                Image(
                    painter = painterResource(android.R.drawable.ic_media_pause),
                    contentDescription = "pause"
                )
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
fun MeasureComposerDialog(onSumbit: (Measure) -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val eighthNoteGroupings = remember { emptyList<Int>().toMutableStateList()  }
    Dialog(onDismissRequest = { onDismiss()}) {
        Card(
            modifier = modifier.padding(16.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column {


                LazyRow {
                    items(eighthNoteGroupings) { eighthNoteGrouping ->
                        when (eighthNoteGrouping) {
                            DUPLE -> Image(
                                painter = painterResource(R.drawable.duple),
                                contentDescription = "duple",
modifier = modifier.height(48.dp).padding(4.dp)
                            )

                            TRIPLE -> Image(
                                painter = painterResource(R.drawable.triplet),
                                contentDescription = "triplet",
                                        modifier = modifier.height(48.dp).padding(4.dp)
                            )

                            else -> Image(
                                painter = painterResource(R.drawable.quarter),
                                contentDescription = "quarter",
                                        modifier = modifier.height(40.dp)
                            )
                        }
                    }
                }
                Row(modifier = modifier.padding(16.dp)) {


                    Button(modifier = modifier.height(32.dp), onClick = { eighthNoteGroupings.add(2) }) {
                        Image(
                            painter = painterResource(R.drawable.duple),
                            contentDescription = "duple"
                        )
                    }
                    Button(modifier = modifier.height(32.dp), onClick = { eighthNoteGroupings.add(3) }) {
                    Image(
                        painter = painterResource(R.drawable.triplet),
                        contentDescription = "triplet"
                    )
                    }
                    Button(modifier = modifier.height(32.dp), onClick = { eighthNoteGroupings.removeAt(eighthNoteGroupings.lastIndex) }) {
                        Image(
                            painter = painterResource(android.R.drawable.ic_delete),
                            contentDescription = "delete"
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onDismiss() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Dismiss")
                    }
                    TextButton(
                        onClick = { onSumbit(measureFromEighthNoteGroupings(eighthNoteGroupings.toIntArray())) },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Confirm")
                    }
                }

            }
        }
    }
}
@Composable
fun MeasureElement( modifier: Modifier = Modifier, measure: Measure, onClick: () -> Unit = {}) {
        Button(onClick = onClick, modifier = modifier) {
            Column {
                Text(text = measure.timeSignatureTop.toString())
                Text(text = measure.timeSignatureBottom.toString())
            }
        }
    FairyDot(measure.fairyDotState)
}
class FairyDotState() {
    private val animatable = Animatable(0f)
    val x: Float get() = 30.5f * cos(2* PI * animatable.value - PI/2).toFloat()
    val y: Float get() = 30.5f * sin(2* PI * animatable.value - PI/2).toFloat()
    suspend fun play(animationDuration: Int) {
        animatable.snapTo(0f)
        animatable.animateTo(1f, animationSpec =  tween(durationMillis = animationDuration, easing = LinearEasing))
    }
}
@Composable
fun FairyDot(state: FairyDotState) {
    Box(modifier = Modifier.offset(x = state.x.dp - 49.5.dp, y = state.y.dp + 30.5.dp)) {
        Box(modifier = Modifier.size(19.dp).clip(CircleShape).border(1.5.dp, Color.Blue, CircleShape))
    }

}
@Composable
fun MeasureElementList(measures: List<Measure>, onClickMeasure: (Measure) -> Unit = {}, activeMeasure: Int) {
    LazyRow {
        items(measures) { measure ->
            MeasureElement(modifier = Modifier.size(80.dp), measure = measure, onClick = { onClickMeasure(measure) }

            )
        }


    }
}