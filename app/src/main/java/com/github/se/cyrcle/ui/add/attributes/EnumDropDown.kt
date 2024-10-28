package com.github.se.cyrcle.ui.add.attributes

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.model.parking.ParkingAttribute
import com.github.se.cyrcle.model.parking.ParkingProtection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicSelectTextField(
    options: List<ParkingAttribute>,
    selectedValue: MutableState<ParkingAttribute>,
    label: String,
    modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }
  ExposedDropdownMenuBox(
      expanded = expanded,
      onExpandedChange = { expanded = !expanded },
      modifier = modifier.padding(10.dp)) {
        OutlinedTextField(
            readOnly = true,
            value = selectedValue.value.description,
            onValueChange = {},
            label = { Text(text = label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(),
            modifier = Modifier.menuAnchor().fillMaxWidth())

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
          options.forEach { option: ParkingAttribute ->
            DropdownMenuItem(
                text = { Text(text = option.description, textAlign = TextAlign.Center) },
                onClick = {
                  expanded = false
                  selectedValue.value = option
                })
          }
        }
      }
}

@Preview
@Composable
fun EnumDropdownInputPreview() {
  DynamicSelectTextField(
      options = ParkingProtection.entries.toList(),
      remember { mutableStateOf(ParkingProtection.NONE) },
      label = "Protection",
  )
}
