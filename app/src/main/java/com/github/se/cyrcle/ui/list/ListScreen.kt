package com.github.se.cyrcle.ui.list

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingCapacity
import com.github.se.cyrcle.model.parking.ParkingProtection
import com.github.se.cyrcle.model.parking.ParkingRackType
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.atoms.ScoreStars
import com.github.se.cyrcle.ui.theme.atoms.SmallFloatingActionButton
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.atoms.ToggleButton
import com.github.se.cyrcle.ui.theme.getCheckBoxColors
import com.github.se.cyrcle.ui.theme.molecules.BottomNavigationBar
import com.mapbox.turf.TurfMeasurement
import kotlin.math.roundToInt

const val CARD_HEIGHT = 120
const val MAX_SWIPE_DISTANCE = 150

@Composable
fun SpotListScreen(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel,
    mapViewModel: MapViewModel,
    userViewModel: UserViewModel
) {
  val userPosition by mapViewModel.userPosition.collectAsState()

  val filteredParkingSpots by parkingViewModel.closestParkings.collectAsState()
  val pinnedParkings by parkingViewModel.pinnedParkings.collectAsState()

  LaunchedEffect(userPosition) { parkingViewModel.setCircleCenter(userPosition) }

  Scaffold(
      modifier = Modifier.testTag("SpotListScreen"),
      bottomBar = { BottomNavigationBar(navigationActions, selectedItem = Route.LIST) }) {
          innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(bottom = 16.dp)) {
          FilterHeader(parkingViewModel = parkingViewModel)
          val listState = rememberLazyListState()
          LazyColumn(state = listState, modifier = Modifier.testTag("SpotListColumn")) {
            // Pinned parking spots if any (includes titles and dividers)
            if (pinnedParkings.isNotEmpty()) {
              item {
                Text(
                    text = stringResource(R.string.pinned_spots),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    testTag = "PinnedSpotsTitle")
                HorizontalDivider(
                    thickness = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
              }
              items(
                  items = filteredParkingSpots.filter { it in pinnedParkings },
                  key = { parking -> parking.uid + "pin" }) { parking ->
                    val distance = TurfMeasurement.distance(userPosition, parking.location.center)
                    SpotCard(
                        navigationActions = navigationActions,
                        parkingViewModel = parkingViewModel,
                        userViewModel = userViewModel,
                        parking = parking,
                        distance = distance)
                  }
              item { Spacer(modifier = Modifier.height(32.dp)) }
            }

            item {
              Text(
                  text = stringResource(R.string.all_spots),
                  style = MaterialTheme.typography.titleMedium,
                  modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp),
                  color = MaterialTheme.colorScheme.primary,
                  testTag = "AllSpotsTitle")
              HorizontalDivider(
                  thickness = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            }

            // All parking spots
            items(items = filteredParkingSpots, key = { parking -> parking.uid }) { parking ->
              val distance = TurfMeasurement.distance(userPosition, parking.location.center)
              SpotCard(
                  navigationActions = navigationActions,
                  parkingViewModel = parkingViewModel,
                  userViewModel = userViewModel,
                  parking = parking,
                  distance = distance)

              if (filteredParkingSpots.indexOf(parking) == filteredParkingSpots.size - 1) {
                parkingViewModel.incrementRadius()
              }
            }
          }
        }
      }
}

@Composable
fun FilterHeader(parkingViewModel: ParkingViewModel) {
  val showProtectionOptions = remember { mutableStateOf(false) }
  val showRackTypeOptions = remember { mutableStateOf(false) }
  val showCapacityOptions = remember { mutableStateOf(false) }
  var showFilters by remember { mutableStateOf(false) }

  val selectedProtection by parkingViewModel.selectedProtection.collectAsState()
  val selectedRackTypes by parkingViewModel.selectedRackTypes.collectAsState()
  val selectedCapacities by parkingViewModel.selectedCapacities.collectAsState()
  val onlyWithCCTV by parkingViewModel.onlyWithCCTV.collectAsState()

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
      // Protection filter
      FilterSection(
          title = stringResource(R.string.list_screen_protection),
          expandedState = showProtectionOptions,
      ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth().testTag("ProtectionFilter"),
            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              items(ParkingProtection.entries.toTypedArray()) { option ->
                ToggleButton(
                    text = option.description,
                    onClick = { parkingViewModel.toggleProtection(option) },
                    value = selectedProtection.contains(option),
                    modifier = Modifier.padding(2.dp),
                    testTag = "ProtectionFilterItem")
              }
            }
      }

      // Rack type filter
      FilterSection(
          title = stringResource(R.string.list_screen_rack_type),
          expandedState = showRackTypeOptions) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().testTag("RackTypeFilter"),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  items(ParkingRackType.entries.toTypedArray()) { option ->
                    ToggleButton(
                        text = option.description,
                        onClick = { parkingViewModel.toggleRackType(option) },
                        value = selectedRackTypes.contains(option),
                        modifier = Modifier.padding(2.dp),
                        testTag = "RackTypeFilterItem")
                  }
                }
          }

      // Capacity filter
      FilterSection(
          title = stringResource(R.string.list_screen_capacity),
          expandedState = showCapacityOptions) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().testTag("CapacityFilter"),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  items(ParkingCapacity.entries.toTypedArray()) { option ->
                    ToggleButton(
                        text = option.description,
                        onClick = { parkingViewModel.toggleCapacity(option) },
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
                onCheckedChange = { parkingViewModel.setOnlyWithCCTV(it) },
                modifier = Modifier.testTag("CCTVCheckbox"),
                colors = getCheckBoxColors(ColorLevel.PRIMARY))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.list_screen_display_only_cctv),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("CCTVCheckboxLabel"))
          }
    }
  }
}

@Composable
fun FilterSection(
    title: String,
    expandedState: MutableState<Boolean>,
    content: @Composable () -> Unit
) {
  Column(
      modifier =
          Modifier.padding(8.dp)
              .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.medium)
              .background(
                  MaterialTheme.colorScheme.surfaceBright, shape = MaterialTheme.shapes.medium)
              .padding(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier =
                Modifier.clickable(onClick = { expandedState.value = !expandedState.value })
                    .padding(8.dp)
                    .fillMaxWidth(),
            testTag = title)

        if (expandedState.value) {
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
    distance: Double
) {
  val context = LocalContext.current

  val userState by userViewModel.currentUser.collectAsState()
  val userSignedIn = userViewModel.isSignedIn.collectAsState(false)
  val isFavorite = userState?.details?.favoriteParkings.orEmpty().contains(parking.uid)

  val isPinned = parking in parkingViewModel.pinnedParkings.collectAsState().value

  Box(modifier = Modifier.fillMaxWidth().height(CARD_HEIGHT.dp)) {
    // Background actions
    Row(modifier = Modifier.fillMaxSize()) {
      // Left action: Pin/Unpin
      ActionCard(
          text =
              if (isPinned) stringResource(R.string.remove_pin)
              else stringResource(R.string.pin_parking_spot),
          icon = Icons.Default.PushPin,
          backgroundColor = if (isPinned) Color.Red.copy(alpha = 0.7f) else Color.LightGray,
          left = true,
          modifier =
              Modifier.fillMaxHeight()
                  .weight(1f)
                  .testTag(if (isPinned) "UnpinActionCard" else "PinActionCard"))

      // Right action: Add to favorites or already in favorites
      if (isFavorite) {
        ActionCard(
            text = stringResource(R.string.already_in_favorites),
            icon = Icons.Default.Favorite,
            backgroundColor = Color.Gray,
            left = false,
            modifier = Modifier.fillMaxHeight().weight(1f).testTag("AlreadyFavoriteActionCard"))
      } else {
        ActionCard(
            text = stringResource(R.string.add_to_favorites),
            icon = Icons.Default.Favorite,
            backgroundColor = Color.Red.copy(alpha = 0.5f),
            left = false,
            modifier = Modifier.fillMaxHeight().weight(1f).testTag("AddToFavoriteActionCard"))
      }
    }

    // Main card
    var offsetX by remember { mutableFloatStateOf(0f) }
    Card(
        modifier =
            Modifier.fillMaxWidth()
                .height(CARD_HEIGHT.dp)
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                  detectHorizontalDragGestures(
                      onDragEnd = {
                        if (offsetX > MAX_SWIPE_DISTANCE.dp.toPx() / 2) {
                          // Swipe right action: Toggle pin status
                          parkingViewModel.togglePinStatus(parking)
                        } else if (offsetX < -MAX_SWIPE_DISTANCE.dp.toPx() / 2) {
                          // Swipe left action: Add to favorites
                          if (!isFavorite && userSignedIn.value) {
                            userState?.let {
                              userViewModel.addFavoriteParkingToSelectedUser(parking.uid)
                              userViewModel.getSelectedUserFavoriteParking()
                            }
                          } else if (!userSignedIn.value) {
                            Toast.makeText(
                                    context,
                                    context.getString(R.string.sign_in_to_add_favorites),
                                    Toast.LENGTH_SHORT)
                                .show()
                          } else if (isFavorite) {
                            Toast.makeText(
                                    context,
                                    context.getString(R.string.already_in_favorites_toast),
                                    Toast.LENGTH_SHORT)
                                .show()
                          }
                        }
                        offsetX = 0f
                      }) { change, dragAmount ->
                        change.consume()
                        offsetX =
                            (offsetX + dragAmount).coerceIn(
                                -MAX_SWIPE_DISTANCE.dp.toPx(), MAX_SWIPE_DISTANCE.dp.toPx())
                      }
                }
                .clickable(
                    onClick = {
                      parkingViewModel.selectParking(parking)
                      navigationActions.navigateTo(Screen.PARKING_DETAILS)
                    })
                .testTag("SpotListItem"),
        shape = RectangleShape,
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isPinned) MaterialTheme.colorScheme.surfaceBright
                    else MaterialTheme.colorScheme.surface)) {
          Row(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(horizontal = 32.dp, vertical = 12.dp)
                      .testTag("SpotCardContent"),
              verticalAlignment = Alignment.Top,
              horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                  Text(
                      text =
                          parking.optName?.let { if (it.length > 35) it.take(32) + "..." else it }
                              ?: stringResource(R.string.default_parking_name),
                      style = MaterialTheme.typography.bodyLarge,
                      testTag = "ParkingName")

                  Spacer(modifier = Modifier.height(8.dp))
                  if (parking.nbReviews > 0) {
                    ScoreStars(
                        parking.avgScore,
                        scale = 0.7f,
                        text =
                            pluralStringResource(R.plurals.reviews_count, count = parking.nbReviews)
                                .format(parking.nbReviews))
                  } else {
                    Text(
                        text = stringResource(R.string.no_reviews),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        testTag = "ParkingNoReviews")
                  }
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text(
                      text =
                          if (distance < 1)
                              stringResource(R.string.distance_m).format(distance * 1000)
                          else stringResource(R.string.distance_km).format(distance),
                      style = MaterialTheme.typography.bodySmall,
                      testTag = "ParkingDistance")
                  if (isPinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "PinnedParking",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier =
                            Modifier.padding(top = 8.dp)
                                .size(20.dp)
                                .rotate(45f)
                                .testTag("PinnedIcon")
                                .align(Alignment.End))
                  }
                }
              }
        }
  }
  HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
}

@Composable
fun ActionCard(
    text: String,
    icon: ImageVector,
    backgroundColor: Color,
    left: Boolean,
    modifier: Modifier = Modifier
) {
  Box(
      modifier = modifier.background(backgroundColor).padding(8.dp),
      contentAlignment = if (left) Alignment.CenterStart else Alignment.CenterEnd) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(icon, contentDescription = null, tint = Color.Black)
              Text(
                  text = text,
                  color = Color.Black,
                  modifier = Modifier.width(100.dp),
              )
            }
      }
}
