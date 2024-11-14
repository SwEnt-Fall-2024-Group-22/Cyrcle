package com.github.se.cyrcle.ui.theme.atoms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.defaultOnColor
import com.github.se.cyrcle.ui.theme.getOutlinedTextFieldColors

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
 * @param hasClearIcon Boolean indicating if the field should have an icon to clear the text.
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
    hasClearIcon: Boolean = true,
    isError: Boolean = false,
    colorLevel: ColorLevel = ColorLevel.PRIMARY,
    testTag: String = "InputText"
) {
  OutlinedTextField(
      value,
      onValueChange = onValueChange,
      modifier = modifier.testTag(testTag),
      label = { Text(label, Modifier.testTag("${testTag}Text"), color = defaultOnColor()) },
      singleLine = singleLine,
      maxLines = maxLines,
      minLines = minLines,
      isError = isError,
      colors = getOutlinedTextFieldColors(ColorLevel.PRIMARY),
      trailingIcon = {
        if (hasClearIcon && value.isNotEmpty()) {
          Icon(
              imageVector = Icons.Filled.Close,
              contentDescription = "Clear",
              tint = defaultOnColor(),
              modifier = Modifier.clickable { onValueChange("") }.testTag("${testTag}Clear"))
        }
      })
}

/**
 * Create a themed `OutlinedTextField` with a condition checking the input. The value contained in
 * the text field must be declared as follow before calling this function :
 * ```
 * var text by remember { mutableStateOf("InitialText") }
 * ConditionCheckingInputText(
 *     onValueChange = { newText -> text = newText },
 *     value = text)
 * ```
 *
 * @param label The label of the field.
 * @param value The initial text that the field should contain.
 * @param onValueChange The function to update your mutable variable. Will often be `{ newText ->
 *   yourMutableVariable = newText }`.
 * @param minCharacters The minimum number of characters that the input should contain.
 * @param maxCharacters The maximum number of characters that the input should contain.
 * @param hasClearIcon Boolean indicating if the field should have an icon to clear the text.
 * @param testTag The test tag of the object.
 */
@Composable
fun ConditionCheckingInputText(
    label: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    value: String = "",
    minCharacters: Int = 0,
    maxCharacters: Int = Int.MAX_VALUE,
    hasClearIcon: Boolean = true,
    testTag: String = "ConditionCheckingInputText"
) {
  Column {
    val isError = value.length !in minCharacters..maxCharacters
    InputText(
        label = label,
        modifier = modifier,
        onValueChange = onValueChange,
        value = value,
        singleLine = false,
        hasClearIcon = hasClearIcon,
        testTag = testTag,
        isError = isError)
    // Display error message below the text field if the input is not valid
    if (isError) {
      Row(
          modifier = Modifier.padding(start = 16.dp, top = 4.dp).testTag("${testTag}ErrorRow"),
          verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.WarningAmber,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text =
                    if (value.length < minCharacters) {
                      stringResource(R.string.input_min_characters, minCharacters, value.length)
                    } else {
                      stringResource(R.string.input_max_characters, maxCharacters, value.length)
                    },
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp),
                testTag = "${testTag}Error")
          }
    }
  }
}
