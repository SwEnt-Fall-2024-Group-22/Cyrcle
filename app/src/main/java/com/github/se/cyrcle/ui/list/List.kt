package com.github.se.cyrcle.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.model.parking.*
import com.github.se.cyrcle.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.DarkBlue
import com.github.se.cyrcle.ui.theme.molecules.BottomNavigationBar
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// EXAMPLES

val referencePoint1 =
    Point(0.0, 0.0) // Hardcoded. Will be the location via GPS of the user of the app.
val parkingSpots1 =
    listOf(
        Parking(
            uid = "1",
            optName = "City Center Parking",
            optDescription = "Covered parking near the city center",
            location = Location(Point(0.3, 0.3)),
            images = listOf(),
            capacity = ParkingCapacity.MEDIUM,
            rackType = ParkingRackType.U_RACK,
            protection = ParkingProtection.COVERED,
            price = 2.0,
            hasSecurity = true),
        Parking(
            uid = "2",
            optName = "Bike Hub Downtown",
            optDescription = "Secure indoor bike hub with surveillance",
            location = Location(Point(0.5, 0.5)),
            images = listOf(),
            capacity = ParkingCapacity.LARGE,
            rackType = ParkingRackType.TWO_TIER,
            protection = ParkingProtection.INDOOR,
            price = 3.5,
            hasSecurity = true),
        Parking(
            uid = "3",
            optName = "Mall Parking",
            optDescription = "Outdoor parking at the shopping mall",
            location = Location(Point(1.2, 1.2)),
            images = listOf(),
            capacity = ParkingCapacity.SMALL,
            rackType = ParkingRackType.POST_AND_RING,
            protection = ParkingProtection.NONE,
            price = 1.0,
            hasSecurity = false),
        Parking(
            uid = "4",
            optName = "Green Park Bike Racks",
            optDescription = "Bike parking near the park entrance",
            location = Location(Point(0.8, 0.8)),
            images = listOf(),
            capacity = ParkingCapacity.XSMALL,
            rackType = ParkingRackType.WALL_BUTTERFLY,
            protection = ParkingProtection.NONE,
            price = 0.5,
            hasSecurity = false),
        Parking(
            uid = "5",
            optName = "Office Tower Garage",
            optDescription = "Indoor parking with two-tier racks",
            location = Location(Point(2.1, 2.1)),
            images = listOf(),
            capacity = ParkingCapacity.LARGE,
            rackType = ParkingRackType.TWO_TIER,
            protection = ParkingProtection.INDOOR,
            price = 4.0,
            hasSecurity = true),
        Parking(
            uid = "6",
            optName = "Station Parking",
            optDescription = "Covered parking next to the train station",
            location = Location(Point(1.5, 1.5)),
            images = listOf(),
            capacity = ParkingCapacity.MEDIUM,
            rackType = ParkingRackType.GRID,
            protection = ParkingProtection.COVERED,
            price = 2.5,
            hasSecurity = true),
        Parking(
            uid = "7",
            optName = "University Bike Shelter",
            optDescription = "Secure parking for students and staff",
            location = Location(Point(3.0, 3.0)),
            images = listOf(),
            capacity = ParkingCapacity.XLARGE,
            rackType = ParkingRackType.WAVE,
            protection = ParkingProtection.COVERED,
            price = 3.0,
            hasSecurity = true),
        Parking(
            uid = "8",
            optName = "Residential Parking",
            optDescription = "Outdoor parking for residents",
            location = Location(Point(0.9, 0.9)),
            images = listOf(),
            capacity = ParkingCapacity.SMALL,
            rackType = ParkingRackType.U_RACK,
            protection = ParkingProtection.NONE,
            price = 1.0,
            hasSecurity = false),
        Parking(
            uid = "9",
            optName = "Beachfront Bike Racks",
            optDescription = "Bike parking near the beach with no cover",
            location = Location(Point(1.8, 1.8)),
            images = listOf(),
            capacity = ParkingCapacity.XSMALL,
            rackType = ParkingRackType.GRID,
            protection = ParkingProtection.NONE,
            price = 0.8,
            hasSecurity = false),
        Parking(
            uid = "10",
            optName = "Suburban Garage",
            optDescription = "Spacious indoor parking in suburban areas",
            location = Location(Point(2.5, 2.5)),
            images = listOf(),
            capacity = ParkingCapacity.MEDIUM,
            rackType = ParkingRackType.VERTICAL,
            protection = ParkingProtection.INDOOR,
            price = 2.8,
            hasSecurity = true))
// ------------------------------------------------------------

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
        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)) {
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
                        modifier =
                            Modifier.padding(2.dp).testTag("ProtectionButton_${protection.name}"),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    if (selectedProtection.contains(protection)) DarkBlue
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
                  items(ParkingRackType.values().toList()) { rackType ->
                    Button(
                        onClick = { onRackTypeSelected(rackType) },
                        modifier =
                            Modifier.padding(2.dp).testTag("RackTypeButton_${rackType.name}"),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    if (selectedRackTypes.contains(rackType)) DarkBlue
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
                  items(ParkingCapacity.values().toList()) { capacity ->
                    Button(
                        onClick = { onCapacitySelected(capacity) },
                        modifier =
                            Modifier.padding(2.dp).testTag("CapacityButton_${capacity.name}"),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    if (selectedCapacities.contains(capacity)) DarkBlue
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
                .fillMaxWidth(),
        color = Color.Black)

    if (isExpanded) {
      content()
    }
  }
}

@Composable
fun SpotCard(
    navigationActions: NavigationActions,
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
              .clickable(onClick = { navigationActions.navigateTo(Screen.CARD) })
              .testTag("SpotCard_${parking.optName ?: "Unnamed"}"),
      colors = CardDefaults.cardColors(containerColor = DarkBlue),
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
                      color = Color.Black,
                      modifier = Modifier.testTag("ParkingName"))
                  Text(
                      text = String.format("%.2f km", distance),
                      style = MaterialTheme.typography.bodySmall,
                      color = Color.Black,
                      modifier = Modifier.testTag("ParkingDistance"))
                }

            if (matchedCriteria.isNotEmpty()) {
              Spacer(modifier = Modifier.height(4.dp))
              matchedCriteria.forEach { criterion ->
                Text(
                    text = criterion,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.testTag("MatchedCriterion_$criterion"))
              }
            }
          }

          Text(
              text = "${parking.price} $",
              style = MaterialTheme.typography.labelSmall,
              color = Color.Black,
              modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).testTag("ParkingPrice"))
        }
      }
}

@Composable
fun SpotListScreen(
    navigationActions: NavigationActions,
    parkingSpots: List<Parking> = parkingSpots1,
    referencePoint: Point = referencePoint1
) {
  val sortedParkingSpots =
      parkingSpots.map { parking ->
        parking to calculateDistance(referencePoint, parking.location.center!!)
      }

  var selectedProtection by remember { mutableStateOf<Set<ParkingProtection>>(emptySet()) }
  var selectedRackTypes by remember { mutableStateOf<Set<ParkingRackType>>(emptySet()) }
  var selectedCapacities by remember { mutableStateOf<Set<ParkingCapacity>>(emptySet()) }

  val sortedFilteredParkingSpots =
      sortedParkingSpots.sortedWith(
          compareByDescending<Pair<Parking, Double>> { (parking, _) ->
                val protectionMatch =
                    if (selectedProtection.isEmpty()) 0
                    else if (selectedProtection.contains(parking.protection)) 1 else 0
                val rackTypeMatch =
                    if (selectedRackTypes.isEmpty()) 0
                    else if (selectedRackTypes.contains(parking.rackType)) 1 else 0
                val capacityMatch =
                    if (selectedCapacities.isEmpty()) 0
                    else if (selectedCapacities.contains(parking.capacity)) 1 else 0

                protectionMatch + rackTypeMatch + capacityMatch
              }
              .thenBy { (_, distance) -> distance })

  Scaffold(
      bottomBar = {
        BottomNavigationBar(
            onTabSelect = { navigationActions.navigateTo(it) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Screen.LIST)
      }) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(bottom = 16.dp),
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
                  matchedCriteria.add("Protection: ${parking.protection}")
                }
                if (selectedRackTypes.isNotEmpty() &&
                    selectedRackTypes.contains(parking.rackType)) {
                  matchedCriteria.add("Rack Type: ${parking.rackType}")
                }
                if (selectedCapacities.isNotEmpty() &&
                    selectedCapacities.contains(parking.capacity)) {
                  matchedCriteria.add("Capacity: ${parking.capacity}")
                }

                SpotCard(navigationActions, parking, distance, matchedCriteria)
              }
            }
      }
}

// Extension fuction to toggle selection
fun <T> Set<T>.toggle(item: T): Set<T> {
  return if (this.contains(item)) this - item else this + item
}

fun calculateDistance(point1: Point, point2: Point): Double {
  val R = 6371.0 // Radius of the Earth in kilometers
  val latDistance = Math.toRadians(point2.latitude - point1.latitude)
  val lonDistance = Math.toRadians(point2.longitude - point1.longitude)

  val a =
      sin(latDistance / 2) * sin(latDistance / 2) +
          cos(Math.toRadians(point1.latitude)) *
              cos(Math.toRadians(point2.latitude)) *
              sin(lonDistance / 2) *
              sin(lonDistance / 2)

  val c = 2 * atan2(sqrt(a), sqrt(1 - a))
  return R * c // Distance in kilometers
}
