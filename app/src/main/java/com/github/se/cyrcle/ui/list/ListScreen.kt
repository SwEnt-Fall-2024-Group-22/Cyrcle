package com.github.se.cyrcle.ui.list

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingAttribute
import com.github.se.cyrcle.model.parking.ParkingCapacity
import com.github.se.cyrcle.model.parking.ParkingProtection
import com.github.se.cyrcle.model.parking.ParkingRackType
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.atoms.ScoreStars
import com.github.se.cyrcle.ui.theme.atoms.SmallFloatingActionButton
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.atoms.ToggleButton
import com.github.se.cyrcle.ui.theme.molecules.BottomNavigationBar
import com.mapbox.turf.TurfMeasurement

@Composable
fun SpotListScreen(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel = viewModel(factory = ParkingViewModel.Factory),
) {

  val referencePoint = TestInstancesParking.EPFLCenter
  val radius = remember { mutableDoubleStateOf(100.0) }

  val parkingSpots by parkingViewModel.closestParkings.collectAsState()

  var selectedProtection by remember { mutableStateOf<Set<ParkingProtection>>(emptySet()) }
  var selectedRackTypes by remember { mutableStateOf<Set<ParkingRackType>>(emptySet()) }
  var selectedCapacities by remember { mutableStateOf<Set<ParkingCapacity>>(emptySet()) }
  var onlyWithCCTV by remember { mutableStateOf(false) }

  // Filter the parking spots based on the selected attributes
  val filteredParkingSpots =
      parkingSpots.filter { parking ->
        (selectedProtection.isEmpty() || selectedProtection.contains(parking.protection)) &&
            (selectedRackTypes.isEmpty() || selectedRackTypes.contains(parking.rackType)) &&
            (selectedCapacities.isEmpty() || selectedCapacities.contains(parking.capacity)) &&
            (!onlyWithCCTV || parking.hasSecurity)
      }

  // Fetch initial parkings if the list is empty
  LaunchedEffect(parkingSpots) {
    if (parkingSpots.isEmpty()) {
      parkingViewModel.getParkingsInRadius(referencePoint, radius.doubleValue)
    }
  }

  LaunchedEffect(selectedProtection, selectedProtection, selectedCapacities, onlyWithCCTV) {
    Log.d("ListScreen", "selectedProtection: $selectedProtection")
    Log.d("ListScreen", "selectedRackTypes: $selectedRackTypes")
    Log.d("ListScreen", "selectedCapacities: $selectedCapacities")
  }

  Scaffold(
      modifier = Modifier.testTag("SpotListScreen"),
      bottomBar = { BottomNavigationBar(navigationActions, selectedItem = Route.LIST) }) {
          innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(bottom = 16.dp)) {
          FilterHeader(
              selectedProtection = selectedProtection,
              selectedRackTypes = selectedRackTypes,
              selectedCapacities = selectedCapacities,
              onAttributeSelected = { attribute ->
                when (attribute) {
                  is ParkingProtection ->
                      selectedProtection = toggleSelection(selectedProtection, attribute)
                  is ParkingRackType ->
                      selectedRackTypes = toggleSelection(selectedRackTypes, attribute)
                  is ParkingCapacity ->
                      selectedCapacities = toggleSelection(selectedCapacities, attribute)
                }
              },
              onlyWithCCTV = onlyWithCCTV,
              onCCTVCheckedChange = { onlyWithCCTV = it })

          val listState = rememberLazyListState()
          LazyColumn(
              state = listState,
              contentPadding = PaddingValues(16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.testTag("SpotListColumn")) {
                items(items = filteredParkingSpots) { parking ->
                  val distance = TurfMeasurement.distance(referencePoint, parking.location.center)
                  SpotCard(navigationActions, parkingViewModel, parking, distance)
                  Log.d("ListScreen", "Filtered parking spots: $filteredParkingSpots")
                  if (filteredParkingSpots.indexOf(parking) == filteredParkingSpots.size - 1) {
                    // This incremental solution could be improved to be dynamic and with a limit
                    radius.doubleValue += 100
                    parkingViewModel.getParkingsInRadius(referencePoint, radius.doubleValue)
                  }
                }
              }
        }
      }
}

@Composable
fun FilterHeader(
    selectedProtection: Set<ParkingProtection>,
    selectedRackTypes: Set<ParkingRackType>,
    selectedCapacities: Set<ParkingCapacity>,
    onAttributeSelected: (ParkingAttribute) -> Unit,
    onlyWithCCTV: Boolean,
    onCCTVCheckedChange: (Boolean) -> Unit
) {
  var showProtectionOptions by remember { mutableStateOf(false) }
  var showRackTypeOptions by remember { mutableStateOf(false) }
  var showCapacityOptions by remember { mutableStateOf(false) }
  var showFilters by remember { mutableStateOf(false) }

  Column(modifier = Modifier.padding(16.dp)) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
      SmallFloatingActionButton(
          onClick = { showFilters = !showFilters },
          icon = if (showFilters) Icons.Default.Close else Icons.Default.FilterList,
          contentDescription = "Filter",
          testTag = "ShowFiltersButton")
    }

    if (showFilters) {
      FilterSection(
          title = stringResource(R.string.list_screen_protection),
          isExpanded = showProtectionOptions,
          onToggle = { showProtectionOptions = !showProtectionOptions }) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().testTag("ProtectionFilter"),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  items(ParkingProtection.entries.toTypedArray()) { option ->
                    ToggleButton(
                        text = option.description,
                        onClick = { onAttributeSelected(option) },
                        value = selectedProtection.contains(option),
                        modifier = Modifier.padding(2.dp),
                        testTag = "ProtectionFilterItem")
                  }
                }
          }

      FilterSection(
          title = stringResource(R.string.list_screen_rack_type),
          isExpanded = showRackTypeOptions,
          onToggle = { showRackTypeOptions = !showRackTypeOptions }) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().testTag("RackTypeFilter"),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  items(ParkingRackType.entries.toTypedArray()) { option ->
                    ToggleButton(
                        text = option.description,
                        onClick = { onAttributeSelected(option) },
                        value = selectedRackTypes.contains(option),
                        modifier = Modifier.padding(2.dp),
                        testTag = "RackTypeFilterItem")
                  }
                }
          }

      FilterSection(
          title = stringResource(R.string.list_screen_capacity),
          isExpanded = showCapacityOptions,
          onToggle = { showCapacityOptions = !showCapacityOptions }) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().testTag("CapacityFilter"),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  items(ParkingCapacity.entries.toTypedArray()) { option ->
                    ToggleButton(
                        text = option.description,
                        onClick = { onAttributeSelected(option) },
                        value = selectedCapacities.contains(option),
                        modifier = Modifier.padding(2.dp),
                        testTag = "CapacityFilterItem")
                  }
                }
          }

      // CCTV filter with checkbox
      Row(
          modifier = Modifier.fillMaxWidth().padding(8.dp),
          verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = onlyWithCCTV,
                onCheckedChange = onCCTVCheckedChange,
                modifier = Modifier.testTag("CCTVCheckbox"))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.list_screen_display_only_cctv),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("CCTVCheckboxLabel"))
          }
    }
  }
}

@Composable
fun FilterSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
  Column(
      modifier =
          Modifier.padding(8.dp)
              .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.medium)
              .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
              .padding(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.clickable(onClick = onToggle).padding(8.dp).fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            testTag = title)

        if (isExpanded) {
          content()
        }
      }
}

@Composable
fun SpotCard(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel,
    parking: Parking,
    distance: Double
) {
  // make a line between the card and the next one
  HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface)
  Card(
      modifier =
          Modifier.fillMaxWidth()
              .height(120.dp)
              .padding(4.dp)
              .clickable(
                  onClick = {
                    parkingViewModel.selectParking(parking)
                    navigationActions.navigateTo(Screen.CARD)
                  })
              .testTag("SpotListItem"),
      colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Box(modifier = Modifier.fillMaxSize()) {
          Column(modifier = Modifier.fillMaxSize().padding(16.dp).testTag("SpotCardContent")) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                  Text(
                      text =
                          parking.optName?.let { if (it.length > 35) it.take(32) + "..." else it }
                              ?: stringResource(R.string.default_parking_name),
                      style = MaterialTheme.typography.bodyLarge,
                      color = MaterialTheme.colorScheme.onSurface,
                      testTag = "ParkingName")
                  Text(
                      text =
                          if (distance < 1)
                              stringResource(R.string.distance_m).format(distance * 1000)
                          else stringResource(R.string.distance_km).format(distance),
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurface,
                      testTag = "ParkingDistance")
                }

            // Rating
            Spacer(modifier = Modifier.height(4.dp))
            if (parking.nbReviews > 0) {
              Row {
                ScoreStars(parking.avgScore, scale = 0.8f) // TODO: Replace with star composable
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text =
                        pluralStringResource(R.plurals.reviews_count, count = parking.nbReviews)
                            .format(parking.nbReviews),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    testTag = "ParkingNbReviews")
              }
            } else {
              Text(
                  text = stringResource(R.string.no_reviews),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                  testTag = "ParkingNoReviews")
            }
          }
        }
      }
}

private fun <T> toggleSelection(set: Set<T>, item: T): Set<T> {
  return if (set.contains(item)) {
    set - item
  } else {
    set + item
  }
}
