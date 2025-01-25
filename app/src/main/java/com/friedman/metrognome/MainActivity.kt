package com.friedman.metrognome

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import androidx.core.widget.addTextChangedListener
import com.friedman.metrognome.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isPlaying = false
    private var mMeasures: Array<Measure> = arrayOf(
        Measure(6, 8, intArrayOf(3,3))
    )
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)


        binding.fab.setOnClickListener { view ->
            if (isPlaying) {
                AudioPlayer.startPlayback(false)
                isPlaying = false
            } else {


                AudioPlayer.startPlayback(true)
                AudioPlayer.setMeasures(mMeasures)
                AudioPlayer.setBpm(binding.bpm.text.toString().toInt())
                isPlaying = true
            }
        }
        binding.bpm.addTextChangedListener(


            afterTextChanged = { s ->
                run {
                    if (isPlaying) {
                        var bpm = s?.toString() ?: ""
                        if (bpm == "") {
                            bpm = "120"
                        }
                        val bpmInt = bpm.toInt()
                        AudioPlayer.setBpm(if (bpmInt > 0) bpmInt else 120)
                    }

                }
            }
        )


    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }


}