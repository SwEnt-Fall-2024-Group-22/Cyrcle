package com.github.se.cyrcle.ui.list

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingAttribute
import com.github.se.cyrcle.model.parking.ParkingCapacity
import com.github.se.cyrcle.model.parking.ParkingProtection
import com.github.se.cyrcle.model.parking.ParkingRackType
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.atoms.ScoreStars
import com.github.se.cyrcle.ui.theme.atoms.SmallFloatingActionButton
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.atoms.ToggleButton
import com.github.se.cyrcle.ui.theme.molecules.BottomNavigationBar
import com.mapbox.turf.TurfMeasurement
import kotlin.math.roundToInt

@Composable
fun SpotListScreen(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel = viewModel(factory = ParkingViewModel.Factory),
    mapViewModel: MapViewModel,
    userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
) {
  val userPosition by mapViewModel.userPosition.collectAsState()

  val filteredParkingSpots by parkingViewModel.closestParkings.collectAsState()

  val selectedProtection by parkingViewModel.selectedProtection.collectAsState()
  val selectedRackTypes by parkingViewModel.selectedRackTypes.collectAsState()
  val selectedCapacities by parkingViewModel.selectedCapacities.collectAsState()
  val onlyWithCCTV by parkingViewModel.onlyWithCCTV.collectAsState()

  val pinnedParkings by parkingViewModel.pinnedParkings.collectAsState()

  LaunchedEffect(userPosition) { parkingViewModel.setCircleCenter(userPosition) }

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
                      parkingViewModel.setSelectedProtection(
                          toggleSelection(selectedProtection, attribute))
                  is ParkingRackType ->
                      parkingViewModel.setSelectedRackTypes(
                          toggleSelection(selectedRackTypes, attribute))
                  is ParkingCapacity ->
                      parkingViewModel.setSelectedCapacities(
                          toggleSelection(selectedCapacities, attribute))
                }
              },
              onlyWithCCTV = onlyWithCCTV,
              onCCTVCheckedChange = { parkingViewModel.setOnlyWithCCTV(it) },
              parkingViewModel = parkingViewModel)

          val listState = rememberLazyListState()
          LazyColumn(
              state = listState,
              contentPadding = PaddingValues(16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.testTag("SpotListColumn")) {
                if (pinnedParkings.isNotEmpty()) {
                  item {
                    Text(
                        text = "Pinned Spots",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.primary)
                  }
                  items(
                      items = filteredParkingSpots.filter { it in pinnedParkings },
                      key = { parking -> parking.uid }) { parking ->
                        val distance =
                            TurfMeasurement.distance(userPosition, parking.location.center)
                        SpotCard(
                            navigationActions = navigationActions,
                            parkingViewModel = parkingViewModel,
                            userViewModel = userViewModel,
                            parking = parking,
                            distance = distance,
                            initialIsPinned = true)
                      }
                  item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "All Spots",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.primary)
                  }
                }

                items(
                    items = filteredParkingSpots.filter { it !in pinnedParkings },
                    key = { parking -> parking.uid }) { parking ->
                      val distance = TurfMeasurement.distance(userPosition, parking.location.center)
                      SpotCard(
                          navigationActions = navigationActions,
                          parkingViewModel = parkingViewModel,
                          userViewModel = userViewModel,
                          parking = parking,
                          distance = distance,
                          initialIsPinned = false)

                      if (filteredParkingSpots.indexOf(parking) == filteredParkingSpots.size - 1) {
                        parkingViewModel.incrementRadius()
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
    onCCTVCheckedChange: (Boolean) -> Unit,
    parkingViewModel: ParkingViewModel = viewModel(factory = ParkingViewModel.Factory)
) {
  var showProtectionOptions by remember { mutableStateOf(false) }
  var showRackTypeOptions by remember { mutableStateOf(false) }
  var showCapacityOptions by remember { mutableStateOf(false) }
  var showFilters by remember { mutableStateOf(false) }
  val radius = parkingViewModel.radius.collectAsState()
  Column(modifier = Modifier.padding(16.dp)) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically) {
          Text(
              text = stringResource(R.string.all_parkings_radius, radius.value.toInt()),
              modifier = Modifier.weight(1f),
              style = MaterialTheme.typography.headlineMedium,
              color = MaterialTheme.colorScheme.onSurface)

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
    userViewModel: UserViewModel,
    parking: Parking,
    distance: Double,
    initialIsPinned: Boolean
) {
  val context = LocalContext.current
  var offsetX by remember { mutableFloatStateOf(0f) }
  val maxSwipeDistance = 150.dp
  val userState by userViewModel.currentUser.collectAsState()
  val userSignedIn = userViewModel.isSignedIn.collectAsState(false)
  val isFavorite = userState?.details?.favoriteParkings.orEmpty().contains(parking.uid)

  Box(modifier = Modifier.fillMaxWidth().height(120.dp).padding(4.dp)) {
    // Background actions
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          ActionCard(
              text = if (initialIsPinned) "Remove pin" else "Pin parking spot",
              icon = Icons.Default.PushPin,
              backgroundColor =
                  if (initialIsPinned) Color.Red.copy(alpha = 0.7f) else Color.LightGray,
              modifier =
                  Modifier.fillMaxHeight()
                      .weight(1f)
                      .testTag(if (initialIsPinned) "UnpinActionCard" else "PinActionCard"))
          if (isFavorite) {
            ActionCard(
                text = "Already in favorites",
                icon = Icons.Default.Star,
                backgroundColor = Color.Gray,
                modifier = Modifier.fillMaxHeight().weight(1f).testTag("AlreadyFavoriteActionCard"))
          } else {
            ActionCard(
                text = "Add to favorites",
                icon = Icons.Default.Star,
                backgroundColor = Color.Yellow,
                modifier = Modifier.fillMaxHeight().weight(1f).testTag("AddToFavoriteActionCard"))
          }
        }

    // Main card
    Card(
        modifier =
            Modifier.fillMaxWidth()
                .height(120.dp)
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                  detectHorizontalDragGestures(
                      onDragEnd = {
                        if (offsetX > maxSwipeDistance.toPx() / 2) {
                          // Swipe right action: Toggle pin status
                          parkingViewModel.togglePinStatus(parking)
                        } else if (offsetX < -maxSwipeDistance.toPx() / 2) {
                          // Swipe left action: Add to favorites
                          if (!isFavorite && userSignedIn.value) {
                            userState?.let {
                              userViewModel.addFavoriteParkingToSelectedUser(parking.uid)
                              userViewModel.getSelectedUserFavoriteParking()
                            }
                          } else if (!userSignedIn.value) {
                            Toast.makeText(
                                    context, "Please sign in to add favorites", Toast.LENGTH_SHORT)
                                .show()
                          } else if (isFavorite) {
                            Toast.makeText(
                                    context, "Parking is already in favorites", Toast.LENGTH_SHORT)
                                .show()
                          }
                        }
                        offsetX = 0f
                      }) { change, dragAmount ->
                        change.consume()
                        offsetX =
                            (offsetX + dragAmount).coerceIn(
                                -maxSwipeDistance.toPx(), maxSwipeDistance.toPx())
                      }
                }
                .clickable(
                    onClick = {
                      parkingViewModel.selectParking(parking)
                      navigationActions.navigateTo(Screen.PARKING_DETAILS)
                    })
                .testTag("SpotListItem"),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 1f)),
        shape = MaterialTheme.shapes.medium) {
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
                  ScoreStars(
                      parking.avgScore,
                      scale = 0.8f,
                      starColor = MaterialTheme.colorScheme.onSurface)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                      text =
                          pluralStringResource(R.plurals.reviews_count, count = parking.nbReviews)
                              .format(parking.nbReviews),
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurface,
                      testTag = "ParkingNbReviews")
                }
              } else {
                Text(
                    text = stringResource(R.string.no_reviews),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    testTag = "ParkingNoReviews")
              }
            }
          }
        }
  }
}

@Composable
fun ActionCard(
    text: String,
    icon: ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
  Box(
      modifier =
          modifier.background(backgroundColor, shape = MaterialTheme.shapes.medium).padding(8.dp),
      contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(icon, contentDescription = null, tint = Color.Black)
          Text(text, color = Color.Black)
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
