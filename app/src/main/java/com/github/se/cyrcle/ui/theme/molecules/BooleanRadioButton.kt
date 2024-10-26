package com.github.se.cyrcle.ui.theme.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A composable that displays a question and two radio buttons for a boolean value
 *
 * @param question the question to display
 * @param state the MutableState that stores the boolean value
 * @param modifier the modifier to apply to the composable
 */
@Composable
fun BooleanRadioButton(
    question: String,
    state: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
  Column {
    Text(text = question)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()) {
          RadioButton(
              selected = state.value,
              onClick = { state.value = true },
              modifier = Modifier.padding(2.dp))
          Text(text = "Yes")
          RadioButton(
              selected = !state.value,
              onClick = { state.value = false },
              modifier = Modifier.padding(start = 20.dp))
          Text(text = "No")
        }
  }
}
