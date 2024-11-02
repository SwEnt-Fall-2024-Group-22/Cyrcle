package com.github.se.cyrcle.ui.list

import android.annotation.SuppressLint
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.atoms.Button
import com.github.se.cyrcle.ui.theme.atoms.SmallFloatingActionButton
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.BottomNavigationBar
import com.mapbox.turf.TurfMeasurement

@Composable
fun SpotListScreen(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel = viewModel(factory = ParkingViewModel.Factory),
) {

  val referencePoint = TestInstancesParking.EPFLCenter

  val parkingSpots by parkingViewModel.kClosestParkings.collectAsState()

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

  Scaffold(
      modifier = Modifier.testTag("SpotListScreen"),
      bottomBar = { BottomNavigationBar(navigationActions, selectedItem = Route.LIST) }) {
          innerPadding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(innerPadding)
                    .padding(bottom = 16.dp)
                    .testTag("SpotListColumn")) {
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
              LazyColumn(
                  contentPadding = PaddingValues(16.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items = filteredParkingSpots) { parking ->
                      val distance =
                          TurfMeasurement.distance(referencePoint, parking.location.center)
                      SpotCard(navigationActions, parkingViewModel, parking, distance)
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
      )
    }

    if (showFilters) {
      FilterSection(
          title = "Protection",
          isExpanded = showProtectionOptions,
          onToggle = { showProtectionOptions = !showProtectionOptions }) {
            FilterOptions(
                options = ParkingProtection.entries.toTypedArray(),
                onOptionSelected = onAttributeSelected,
                selectedOptions = selectedProtection,
                getDescription = { it.description },
                testTag = "ProtectionFilter")
          }
      FilterSection(
          title = "Rack Type",
          isExpanded = showRackTypeOptions,
          onToggle = { showRackTypeOptions = !showRackTypeOptions }) {
            FilterOptions(
                options = ParkingRackType.entries.toTypedArray(),
                onOptionSelected = onAttributeSelected,
                selectedOptions = selectedRackTypes,
                getDescription = { it.description },
                testTag = "RackTypeFilter")
          }
      FilterSection(
          title = "Capacity",
          isExpanded = showCapacityOptions,
          onToggle = { showCapacityOptions = !showCapacityOptions }) {
            FilterOptions(
                options = ParkingCapacity.entries.toTypedArray(),
                onOptionSelected = onAttributeSelected,
                selectedOptions = selectedCapacities,
                getDescription = { it.description },
                testTag = "CapacityFilter")
          }
      Row(
          modifier = Modifier.fillMaxWidth().padding(8.dp),
          verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = onlyWithCCTV,
                onCheckedChange = onCCTVCheckedChange,
                modifier = Modifier.testTag("CCTVCheckbox"))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Only display parkings with CCTV camera",
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
fun <T : Enum<T>> FilterOptions(
    options: Array<T>,
    onOptionSelected: (T) -> Unit,
    selectedOptions: Set<T>,
    getDescription: (T) -> String,
    testTag: String
) {
  LazyRow(
      modifier = Modifier.fillMaxWidth().testTag(testTag),
      horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        items(options) { option ->
          Button(
              text = getDescription(option),
              onClick = { onOptionSelected(option) },
              modifier = Modifier.padding(2.dp),
              colorLevel = ColorLevel.PRIMARY,
              disabled = remember { mutableStateOf(selectedOptions.contains(option)) },
              testTag = "${testTag}Item")
        }
      }
}

@SuppressLint("DefaultLocale")
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
                  androidx.compose.material3.Text(
                      text = parking.optName ?: "Unnamed Parking",
                      style = MaterialTheme.typography.bodyLarge,
                      color = MaterialTheme.colorScheme.onSurface,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.testTag("ParkingName"),
                  )
                  Text(
                      text =
                          if (distance < 1) String.format("%.0f m", distance * 1000)
                          else String.format("%.2f km", distance),
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurface,
                      testTag = "ParkingDistance")
                }

            // Rating
            Spacer(modifier = Modifier.height(4.dp))
            if (parking.nbReviews > 0) {
              Row {
                Text(
                    text = "Rating: ${parking.avgScore}", // TODO: Replace with star composable
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    testTag = "ParkingRating")
                // Number of reviews
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text =
                        "(${parking.nbReviews} review" + if (parking.nbReviews > 1) "s)" else ")",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    testTag = "ParkingNbReviews")
              }
            } else {
              Text(
                  text = "No reviews yet",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                  testTag = "ParkingNoReviews")
            }
          }

          Text(
              text = "${parking.price} $",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
              testTag = "ParkingPrice")
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
