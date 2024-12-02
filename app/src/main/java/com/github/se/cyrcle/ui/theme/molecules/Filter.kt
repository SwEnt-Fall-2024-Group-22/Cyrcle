package com.github.se.cyrcle.ui.theme.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.ParkingCapacity
import com.github.se.cyrcle.model.parking.ParkingProtection
import com.github.se.cyrcle.model.parking.ParkingRackType
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.atoms.SmallFloatingActionButton
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.atoms.ToggleButton
import com.github.se.cyrcle.ui.theme.getCheckBoxColors

@Composable
fun FilterHeader(parkingViewModel: ParkingViewModel, displayHeader: Boolean = true) {
  val showProtectionOptions = remember { mutableStateOf(false) }
  val showRackTypeOptions = remember { mutableStateOf(false) }
  val showCapacityOptions = remember { mutableStateOf(false) }
  var showFilters by remember { mutableStateOf(!displayHeader) }

  val selectedProtection by parkingViewModel.selectedProtection.collectAsState()
  val selectedRackTypes by parkingViewModel.selectedRackTypes.collectAsState()
  val selectedCapacities by parkingViewModel.selectedCapacities.collectAsState()
  val onlyWithCCTV by parkingViewModel.onlyWithCCTV.collectAsState()

  val radius = parkingViewModel.radius.collectAsState()

  Column(modifier = Modifier.padding(if (displayHeader) 16.dp else 0.dp)) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically) {
          if (displayHeader) {
            Text(
                text = stringResource(R.string.all_parkings_radius, radius.value.toInt()),
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface)

            SmallFloatingActionButton(
                onClick = { showFilters = !showFilters },
                icon = if (showFilters) Icons.Default.Close else Icons.Default.FilterList,
                contentDescription = "Filter",
                testTag = "ShowFiltersButton")
          }
        }

    if (showFilters || !displayHeader) {
      Row(
          modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween) {
            ClickableText(
                text = AnnotatedString(stringResource(R.string.list_screen_clear_all)),
                onClick = { parkingViewModel.clearAllFilterOptions() },
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.primary),
                modifier = Modifier.padding(horizontal = 8.dp).testTag("ClearAllFiltersButton"))
            ClickableText(
                text = AnnotatedString(stringResource(R.string.list_screen_apply_all)),
                onClick = { parkingViewModel.selectAllFilterOptions() },
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.primary),
                modifier = Modifier.padding(horizontal = 8.dp).testTag("ApplyAllFiltersButton"))
          }

      // Protection filter
      FilterSection(
          title = stringResource(R.string.list_screen_protection),
          expandedState = showProtectionOptions,
          onReset = { parkingViewModel.clearProtection() },
          onApply = { parkingViewModel.selectAllProtection() }) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().testTag("ProtectionFilter"),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  items(ParkingProtection.entries.toTypedArray()) { option ->
                    ToggleButton(
                        text = option.description,
                        onClick = { parkingViewModel.toggleProtection(option) },
                        value = selectedProtection.contains(option),
                        testTag = "ProtectionFilterItem")
                  }
                }
          }

      // Rack type filter
      FilterSection(
          title = stringResource(R.string.list_screen_rack_type),
          expandedState = showRackTypeOptions,
          onReset = { parkingViewModel.clearRackType() },
          onApply = { parkingViewModel.selectAllRackTypes() }) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().testTag("RackTypeFilter"),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  items(ParkingRackType.entries.toTypedArray()) { option ->
                    ToggleButton(
                        text = option.description,
                        onClick = { parkingViewModel.toggleRackType(option) },
                        value = selectedRackTypes.contains(option),
                        testTag = "RackTypeFilterItem")
                  }
                }
          }

      // Capacity filter
      FilterSection(
          title = stringResource(R.string.list_screen_capacity),
          expandedState = showCapacityOptions,
          onReset = { parkingViewModel.clearCapacity() },
          onApply = { parkingViewModel.selectAllCapacities() }) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().testTag("CapacityFilter"),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  items(ParkingCapacity.entries.toTypedArray()) { option ->
                    ToggleButton(
                        text = option.description,
                        onClick = { parkingViewModel.toggleCapacity(option) },
                        value = selectedCapacities.contains(option),
                        testTag = "CapacityFilterItem")
                  }
                }
          }

      // CCTV filter with checkbox
      Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Checkbox(
                checked = onlyWithCCTV,
                onCheckedChange = { parkingViewModel.setOnlyWithCCTV(it) },
                modifier = Modifier.testTag("CCTVCheckbox"),
                colors = getCheckBoxColors(ColorLevel.PRIMARY))
            Text(
                text = stringResource(R.string.list_screen_display_only_cctv),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
                modifier = Modifier.testTag("CCTVCheckboxLabel"))
          }
    }
  }
}

@Composable
fun FilterSection(
    title: String,
    expandedState: MutableState<Boolean>,
    onReset: () -> Unit,
    onApply: () -> Unit,
    content: @Composable () -> Unit
) {
  Column(
      modifier =
          Modifier.padding(vertical = 8.dp)
              .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.medium)
              .background(
                  MaterialTheme.colorScheme.surfaceBright, shape = MaterialTheme.shapes.medium)
              .padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              ClickableText(
                  text = AnnotatedString(stringResource(R.string.list_screen_clear)),
                  onClick = { onReset() },
                  style =
                      MaterialTheme.typography.labelSmall.copy(
                          color = MaterialTheme.colorScheme.primary),
                  modifier =
                      Modifier.padding(horizontal = 4.dp).testTag("Clear" + title + "Button"))
              Text(
                  text = title,
                  style = MaterialTheme.typography.titleMedium,
                  modifier =
                      Modifier.clickable(onClick = { expandedState.value = !expandedState.value })
                          .padding(6.dp),
                  testTag = title)
              ClickableText(
                  text = AnnotatedString(stringResource(R.string.list_screen_apply)),
                  onClick = { onApply() },
                  style =
                      MaterialTheme.typography.labelSmall.copy(
                          color = MaterialTheme.colorScheme.primary),
                  modifier = Modifier.padding(horizontal = 4.dp).testTag("Apply${title}Button"))
            }

        if (expandedState.value) {
          Box(modifier = Modifier.padding(top = 8.dp)) { content() }
        }
      }
}
