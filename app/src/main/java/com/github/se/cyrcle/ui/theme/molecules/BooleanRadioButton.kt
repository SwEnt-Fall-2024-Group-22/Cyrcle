package com.github.se.cyrcle.ui.theme.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.ui.theme.atoms.RadioButton

/**
 * Create a question and two radio buttons for a boolean value.
 *
 * @param question The question to display.
 * @param state The MutableState that stores the boolean value.
 * @param modifier The modifier to apply to the composable.
 */
@Composable
fun BooleanRadioButton(
    question: String,
    state: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    testTag: String = "BooleanRadioButton"
) {
  Column(Modifier.testTag(testTag)) {
    Text(text = question, Modifier.testTag("${testTag}Question"))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()) {
          RadioButton(
              selected = state.value,
              onClick = { state.value = true },
              modifier = Modifier.padding(2.dp),
              testTag = "${testTag}YesRadioButton")
          Text(text = "Yes", Modifier.testTag("${testTag}YesText"))
          RadioButton(
              selected = !state.value,
              onClick = { state.value = false },
              modifier = Modifier.padding(start = 20.dp),
              testTag = "${testTag}NoRadioButton")
          Text(text = "No", Modifier.testTag("${testTag}NoText"))
        }
  }
}
