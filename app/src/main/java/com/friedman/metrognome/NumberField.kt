package com.friedman.metrognome

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun NumberField(text: Int, onValueChange: (Int) -> Unit) {


    TextField(
        value = if(text > 0) text.toString() else "",
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        onValueChange = { it -> onValueChange(it.toIntOrNull() ?: 0)}
    )

}