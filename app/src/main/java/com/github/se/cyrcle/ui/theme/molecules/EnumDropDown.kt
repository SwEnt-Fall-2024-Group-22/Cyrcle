package com.github.se.cyrcle.ui.theme.molecules

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.model.parking.ParkingProtection

/**
 * Interface for enums that can be displayed in a dropdown menu.
 *
 * @property description The description of the element that will be displayed in the dropdown menu.
 */
interface DropDownableEnum {
  val description: String
}

/**
 * A composable that displays a question and a dropdown menu with an item per enum value. The
 * selected value is stored in a MutableState.
 *
 * @param options The list of entries of the enum to display.
 * @param selectedValue The MutableState that stores the selected value.
 * @param label The label to display.
 * @param modifier The modifier to apply to the composable.
 * @param testTag The test tag to apply to the composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : DropDownableEnum> EnumDropDown(
    options: List<T>,
    selectedValue: MutableState<T>,
    label: String,
    modifier: Modifier = Modifier,
    testTag: String = "EnumDropDown"
) {
  var expanded by remember { mutableStateOf(false) }
  ExposedDropdownMenuBox(
      expanded = expanded,
      onExpandedChange = { expanded = !expanded },
      modifier = modifier.padding(10.dp).testTag(testTag)) {
        OutlinedTextField(
            readOnly = true,
            value = selectedValue.value.description,
            onValueChange = {},
            label = { Text(text = label, Modifier.testTag("${testTag}Label")) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(),
            modifier = Modifier.menuAnchor().fillMaxWidth())

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
          options.withIndex().forEach { (i, option: T) ->
            DropdownMenuItem(
                text = {
                  Text(
                      text = option.description,
                      textAlign = TextAlign.Center,
                      modifier = Modifier.testTag("${testTag}${i}Text"))
                },
                onClick = {
                  expanded = false
                  selectedValue.value = option
                },
                Modifier.testTag("${testTag}${i}Item"))
          }
        }
      }
}

@Preview
@Composable
fun EnumDropDownInputPreview() {
  EnumDropDown(
      options = ParkingProtection.entries.toList(),
      remember { mutableStateOf(ParkingProtection.NONE) },
      label = "Protection",
  )
}
