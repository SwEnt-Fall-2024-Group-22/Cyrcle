package com.github.se.cyrcle.ui.theme.atoms

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.github.se.cyrcle.ui.theme.ColorScheme
import com.github.se.cyrcle.ui.theme.White
import java.io.File

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
    value: String = "",
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    testTag: String = "InputText"
) {
  OutlinedTextField(
      value,
      onValueChange = onValueChange,
      modifier = Modifier.testTag(testTag),
      label = { Text(label) },
      singleLine = singleLine,
      maxLines = maxLines,
      minLines = minLines,
  )
}

/**
 * Create a themed number input, with simplified arguments. The value contained in the number field
 * must be declared as follow before calling this function :
 * ```
 * var num by remember { mutableStateOf(0) }
 * NumberInput(onValueChange = { newNum -> num = newNum })
 * ```
 *
 * WARNING : Not yet implemented, signature may change.
 */
@Composable
fun NumberInput(label: String, initialValue: String, onValueChange: (String) -> Unit) {
  TODO()
}

/**
 * Create a themed file input from phone, with simplified arguments. The value contained in the file
 * field must be declared as follow before calling this function :
 * ```
 * var file by remember { mutableStateOf(???) }
 * InputFileFromPhone(onValueChange = { newFile -> file = newFile })
 * ```
 *
 * WARNING : Not yet implemented, signature may change.
 */
@Composable
fun InputFileFromPhone(text: String, file: MutableState<File>) {
  Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = White)) {
    Text("Open File manager")
  }
}

/**
 * Create a themed camera input, with simplified arguments. The value contained in the camera field
 * must be declared as follow before calling this function :
 * ```
 * var camera by remember { mutableStateOf(???) }
 * InputFromCamera(onValueChange = { newCamera -> camera = newCamera })
 * ```
 *
 * WARNING : Not yet implemented, signature may change.
 */
@Composable
fun InputFromCamera(text: String, file: MutableState<File>) {
  Button(
      onClick = {
        // Ask for Camera permission
        TODO()
        // Open the camera app
        TODO()
      },
      colors = ButtonDefaults.buttonColors(containerColor = White)) {
        Text("Open camera")
      }
}

/**
 * List the different dispositions for a list of checkboxes. WARNING : Not yet implemented,
 * signature may change.
 */
enum class CheckBoxDisposition {
  VERTICAL,
  HORIZONTAL,
  AUTOMATIC,
  FLEX
}

/**
 * Create a themed checkbox input, with simplified arguments. The value contained in the checkbox
 * field must be declared as follow before calling this function :
 * ```
 * var checkboxes by remember { mutableStateOf(???) }
 * CheckBoxes(onValueChange = { newValues -> values = newValues })
 * ```
 *
 * WARNING : Not yet implemented, signature may change. {@see CheckBoxDisposition}
 */
@Composable
fun CheckBoxes(labels: List<String>, onValueChange: (String) -> Unit, colorScheme: ColorScheme) {
  TODO()
}
