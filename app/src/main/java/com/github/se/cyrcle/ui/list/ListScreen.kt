package com.github.se.cyrcle.ui.list

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingCapacity
import com.github.se.cyrcle.model.parking.ParkingProtection
import com.github.se.cyrcle.model.parking.ParkingRackType
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.molecules.BottomNavigationBar
import com.mapbox.turf.TurfMeasurement

@Composable
fun SpotListScreen(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel = viewModel(factory = ParkingViewModel.Factory),
) {
  val parkingSpots by parkingViewModel.kClosestParkings.collectAsState()

  var selectedProtection by remember { mutableStateOf<Set<ParkingProtection>>(emptySet()) }
  var selectedRackTypes by remember { mutableStateOf<Set<ParkingRackType>>(emptySet()) }
  var selectedCapacities by remember { mutableStateOf<Set<ParkingCapacity>>(emptySet()) }

  val sortedFilteredParkingSpots =
      parkingSpots
          .map { parking ->
            // TODO: Use the user's location instead of a hardcoded reference point
            parking to
                TurfMeasurement.distance(
                    TestInstancesParking.referencePoint, parking.location.center)
          }
          .sortedWith(
              compareByDescending<Pair<Parking, Double>> { (parking, _) ->
                    listOf(
                            selectedProtection.contains(parking.protection),
                            selectedRackTypes.contains(parking.rackType),
                            selectedCapacities.contains(parking.capacity))
                        .count { it }
                  }
                  .thenBy { (_, distance) -> distance })

  Scaffold(
      modifier = Modifier.testTag("SpotListScreen"),
      bottomBar = {
        BottomNavigationBar(
            navigationActions,
            selectedItem = Screen.LIST)
      }) { innerPadding ->
        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
                    .padding(innerPadding)
                    .padding(bottom = 16.dp)
                    .testTag("SpotListColumn"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              item {
                FilterHeader(
                    selectedProtection = selectedProtection,
                    onProtectionSelected = { protection ->
                      selectedProtection =
                          if (selectedProtection.contains(protection)) {
                            selectedProtection - protection
                          } else {
                            selectedProtection + protection
                          }
                    },
                    selectedRackTypes = selectedRackTypes,
                    onRackTypeSelected = { rackType ->
                      selectedRackTypes =
                          if (selectedRackTypes.contains(rackType)) {
                            selectedRackTypes - rackType
                          } else {
                            selectedRackTypes + rackType
                          }
                    },
                    selectedCapacities = selectedCapacities,
                    onCapacitySelected = { capacity ->
                      selectedCapacities =
                          if (selectedCapacities.contains(capacity)) {
                            selectedCapacities - capacity
                          } else {
                            selectedCapacities + capacity
                          }
                    })
              }
              items(sortedFilteredParkingSpots) { (parking, distance) ->
                val matchedCriteria = mutableListOf<String>()

                if (selectedProtection.isNotEmpty() &&
                    selectedProtection.contains(parking.protection)) {
                  matchedCriteria.add("Protection: ${parking.protection.description}")
                }
                if (selectedRackTypes.isNotEmpty() &&
                    selectedRackTypes.contains(parking.rackType)) {
                  matchedCriteria.add("Rack Type: ${parking.rackType.description}")
                }
                if (selectedCapacities.isNotEmpty() &&
                    selectedCapacities.contains(parking.capacity)) {
                  matchedCriteria.add("Capacity: ${parking.capacity.description}")
                }

                SpotCard(navigationActions, parkingViewModel, parking, distance, matchedCriteria)
              }
            }
      }
}

@Composable
fun FilterHeader(
    selectedProtection: Set<ParkingProtection>,
    onProtectionSelected: (ParkingProtection) -> Unit,
    selectedRackTypes: Set<ParkingRackType>,
    onRackTypeSelected: (ParkingRackType) -> Unit,
    selectedCapacities: Set<ParkingCapacity>,
    onCapacitySelected: (ParkingCapacity) -> Unit
) {
  var showProtectionOptions by remember { mutableStateOf(false) }
  var showRackTypeOptions by remember { mutableStateOf(false) }
  var showCapacityOptions by remember { mutableStateOf(false) }
  var showFilters by remember { mutableStateOf(false) }

  Column(modifier = Modifier.padding(16.dp)) {
    Button(
        onClick = { showFilters = !showFilters },
        modifier = Modifier.fillMaxWidth().testTag("ShowFiltersButton"),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
          Text(if (showFilters) "Hide Filters" else "Show Filters")
        }

    if (showFilters) {
      Spacer(modifier = Modifier.height(8.dp))

      FilterSection(
          title = "Protection",
          isExpanded = showProtectionOptions,
          onToggle = { showProtectionOptions = !showProtectionOptions }) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().testTag("ProtectionFilter"),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  items(ParkingProtection.entries) { protection ->
                    Button(
                        onClick = { onProtectionSelected(protection) },
                        modifier = Modifier.padding(2.dp).testTag("ProtectionFilterItem"),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    if (selectedProtection.contains(protection))
                                        MaterialTheme.colorScheme.primary
                                    else Color.LightGray)) {
                          Text(protection.name)
                        }
                  }
                }
          }

      Spacer(modifier = Modifier.height(8.dp))

      FilterSection(
          title = "Rack Type",
          isExpanded = showRackTypeOptions,
          onToggle = { showRackTypeOptions = !showRackTypeOptions }) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().testTag("RackTypeFilter"),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  items(ParkingRackType.entries) { rackType ->
                    Button(
                        onClick = { onRackTypeSelected(rackType) },
                        modifier = Modifier.padding(2.dp).testTag("RackTypeFilterItem"),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    if (selectedRackTypes.contains(rackType))
                                        MaterialTheme.colorScheme.primary
                                    else Color.LightGray)) {
                          Text(rackType.name)
                        }
                  }
                }
          }

      Spacer(modifier = Modifier.height(8.dp))

      FilterSection(
          title = "Capacity",
          isExpanded = showCapacityOptions,
          onToggle = { showCapacityOptions = !showCapacityOptions }) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().testTag("CapacityFilter"),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  items(ParkingCapacity.entries) { capacity ->
                    Button(
                        onClick = { onCapacitySelected(capacity) },
                        modifier = Modifier.padding(2.dp).testTag("CapacityFilterItem"),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    if (selectedCapacities.contains(capacity))
                                        MaterialTheme.colorScheme.primary
                                    else Color.LightGray)) {
                          Text(capacity.name)
                        }
                  }
                }
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
  Column {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier =
            Modifier.clickable(onClick = onToggle)
                .padding(8.dp)
                .background(Color.LightGray)
                .fillMaxWidth()
                .testTag(title),
        color = MaterialTheme.colorScheme.tertiary)

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
    distance: Double,
    matchedCriteria: List<String>
) {
  Card(
      modifier =
          Modifier.fillMaxWidth()
              .height(120.dp)
              .shadow(8.dp, shape = MaterialTheme.shapes.medium)
              .padding(4.dp)
              .clickable(
                  onClick = {
                    parkingViewModel.selectParking(parking)
                    navigationActions.navigateTo(Screen.CARD)
                  })
              .testTag("SpotListItem"),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
      shape = MaterialTheme.shapes.medium,
      elevation = CardDefaults.cardElevation(8.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
          Column(modifier = Modifier.fillMaxSize().padding(16.dp).testTag("SpotCardContent")) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                  Text(
                      text = parking.optName ?: "Unnamed Parking",
                      style = MaterialTheme.typography.titleLarge,
                      color = MaterialTheme.colorScheme.secondary,
                      modifier = Modifier.testTag("ParkingName"))
                  Text(
                      text = String.format("%.2f km", distance),
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.secondary,
                      modifier = Modifier.testTag("ParkingDistance"))
                }

            if (matchedCriteria.isNotEmpty()) {
              Spacer(modifier = Modifier.height(4.dp))
              matchedCriteria.forEach { criterion ->
                Text(
                    text = criterion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.testTag("MatchedCriterionItem"))
              }
            }
          }

          Text(
              text = "${parking.price} $",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).testTag("ParkingPrice"))
        }
      }
}
