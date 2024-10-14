package com.github.se.cyrcle.ui.theme.atoms

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/**
 * Create a themed `OutlinedTextField`, with simplified arguments. The value contained in the text
 * field must be declared as follow before calling this function :
 * ```
 * var text by remember { mutableStateOf("InitialText") }
 * TextField(onValueChange = { newText -> text = newText })
 * ```
 *
 * @param label The label of the field.
 * @param value The initial text that the field should contain.
 * @param onValueChange The function to update your mutable variable. Will often be `{ newText ->
 *   yourMutableVariable = newText }`.
 * @param singleLine Boolean indicating if the text field should only contain one line.
 * @param maxLines If not single line, contains the field within a maximum line number.
 * @param minLines The minimal number of lines that should be displayed at the same time.
 * @param testTag The test tag of the object.
 */
@Composable
fun InputText(
    label: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    value: String = "",
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    testTag: String = "InputText"
) {
  OutlinedTextField(
      value,
      onValueChange = onValueChange,
      modifier = modifier.testTag(testTag),
      label = { Text(label) },
      singleLine = singleLine,
      maxLines = maxLines,
      minLines = minLines,
  )
}
