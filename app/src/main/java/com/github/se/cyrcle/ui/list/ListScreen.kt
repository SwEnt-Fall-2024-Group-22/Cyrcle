package com.github.se.cyrcle.ui.list

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.address.Address
import com.github.se.cyrcle.model.address.AddressViewModel
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingCapacity
import com.github.se.cyrcle.model.parking.ParkingProtection
import com.github.se.cyrcle.model.parking.ParkingRackType
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.permission.PermissionHandler
import com.github.se.cyrcle.ui.map.MapConfig
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.atoms.ScoreStars
import com.github.se.cyrcle.ui.theme.atoms.SmallFloatingActionButton
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.atoms.ToggleButton
import com.github.se.cyrcle.ui.theme.defaultOnColor
import com.github.se.cyrcle.ui.theme.getCheckBoxColors
import com.github.se.cyrcle.ui.theme.getOutlinedTextFieldColorsSearchBar
import com.github.se.cyrcle.ui.theme.molecules.BottomNavigationBar
import com.github.se.cyrcle.ui.theme.molecules.FilterPanel
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfMeasurement
import kotlin.math.roundToInt
import kotlinx.coroutines.runBlocking

const val CARD_HEIGHT = 120
const val MAX_SWIPE_DISTANCE = 150
const val maxSuggestionDisplayNameLengthList = 50

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun SpotListScreen(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel,
    mapViewModel: MapViewModel,
    userViewModel: UserViewModel,
    addressViewModel: AddressViewModel,
    permissionHandler: PermissionHandler
) {

  val userPosition by mapViewModel.userPosition.collectAsState()

  val filteredParkingSpots by parkingViewModel.closestParkings.collectAsState()
  val pinnedParkings by parkingViewModel.pinnedParkings.collectAsState()

  // chosen location by the user for the list Screen
  val chosenLocation: MutableState<Address> = remember {
    mutableStateOf(
        Address(
            latitude = MapConfig.defaultCameraState().center.latitude().toString(),
            longitude = MapConfig.defaultCameraState().center.longitude().toString()))
  }

  // value that says wether the user clicked on my Location Suggestion or not
  val myLocation = remember { mutableStateOf(true) }

  // location permission from location manager
  val locPermission = permissionHandler.getLocalisationPerm().value

  LaunchedEffect(userPosition, myLocation, chosenLocation) {
    if (locPermission && myLocation.value) parkingViewModel.setCircleCenter(userPosition)
    else
        parkingViewModel.setCircleCenter(
            Point.fromLngLat(
                chosenLocation.value.longitude.toDouble(),
                chosenLocation.value.latitude.toDouble()))
  }

  Scaffold(
      modifier = Modifier.testTag("SpotListScreen"),
      bottomBar = { BottomNavigationBar(navigationActions, selectedItem = Route.LIST) }) {
          innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(bottom = 16.dp)) {
          FilterPanel(parkingViewModel = parkingViewModel, displayHeader = true)
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

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun FilterHeader(
    parkingViewModel: ParkingViewModel,
    addressViewModel: AddressViewModel,
    myLocation: MutableState<Boolean>,
    chosenLocation: MutableState<Address>,
    permissionHandler: PermissionHandler
) {
  val showProtectionOptions = remember { mutableStateOf(false) }
  val showRackTypeOptions = remember { mutableStateOf(false) }
  val showCapacityOptions = remember { mutableStateOf(false) }
  var showFilters by remember { mutableStateOf(false) }

  val selectedProtection by parkingViewModel.selectedProtection.collectAsState()
  val selectedRackTypes by parkingViewModel.selectedRackTypes.collectAsState()
  val selectedCapacities by parkingViewModel.selectedCapacities.collectAsState()
  val onlyWithCCTV by parkingViewModel.onlyWithCCTV.collectAsState()

  // Text field visibility
  val isTextFieldVisible = remember { mutableStateOf(false) }

  // Text field value
  val textFieldValue = remember { mutableStateOf("") }

  // Initialize FocusManager
  val focusManager = LocalFocusManager.current

  // Initialize the keyboard controller
  val virtualKeyboardManager = LocalSoftwareKeyboardController.current

  // List of suggestions from NominatimAPI
  val listOfSuggestions = addressViewModel.addressList.collectAsState()

  val uniqueSuggestions = remember { mutableStateOf(listOf<Address>()) }

  // Show suggestions screen
  val showSuggestions = remember { mutableStateOf(false) }

  val textFieldSize = remember { mutableStateOf(IntSize.Zero) }

  // Animation values
  val slideOffset by
      animateDpAsState(targetValue = if (isTextFieldVisible.value) 0.dp else (-200).dp)
  val alpha by animateFloatAsState(targetValue = if (isTextFieldVisible.value) 1f else 0f)

  val radius = parkingViewModel.radius.collectAsState()

  Column(modifier = Modifier.padding(16.dp)) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically) {

          // Button transforms into a close button
          SmallFloatingActionButton(
              icon = if (isTextFieldVisible.value) Icons.Default.Close else Icons.Default.Search,
              onClick = { isTextFieldVisible.value = !isTextFieldVisible.value },
              modifier = Modifier,
              contentDescription = "Search List Screen",
          )

          // Text field slides out to the right of the button
          if (isTextFieldVisible.value) {

            Box {
              OutlinedTextField(
                  value = textFieldValue.value,
                  onValueChange = { textFieldValue.value = it },
                  placeholder = { Text(text = stringResource(R.string.search_bar_placeholder)) },
                  modifier =
                      Modifier.padding(start = 8.dp)
                          .offset(x = slideOffset)
                          .alpha(alpha)
                          .onGloballyPositioned { coordinates ->
                            textFieldSize.value = coordinates.size
                          },
                  colors = getOutlinedTextFieldColorsSearchBar(ColorLevel.PRIMARY),
                  trailingIcon = {
                    if (textFieldValue.value.isNotEmpty()) {
                      Icon(
                          imageVector = Icons.Filled.Clear,
                          contentDescription = "Clear search",
                          tint = defaultOnColor(),
                          modifier =
                              Modifier.clickable {
                                textFieldValue.value = ""
                                showSuggestions.value = false
                              })
                    }
                  },
                  singleLine = true,
                  keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                  keyboardActions =
                      KeyboardActions(
                          onSearch = {
                            virtualKeyboardManager?.hide()

                            runBlocking { addressViewModel.search(textFieldValue.value) }

                            showSuggestions.value = true
                          }))

              if (listOfSuggestions.value.size + 1 > 0) {
                DropdownMenu(
                    expanded = showSuggestions.value,
                    onDismissRequest = { showSuggestions.value = false },
                    modifier =
                        Modifier.width(
                            with(LocalDensity.current) { textFieldSize.value.width.toDp() })) {
                      if (permissionHandler.getLocalisationPerm().value) {
                        DropdownMenuItem(
                            text = { Text("My Location") },
                            onClick = {
                              myLocation.value = true
                              showSuggestions.value = false
                              textFieldValue.value = "My Location"
                              isTextFieldVisible.value = false
                              focusManager.clearFocus()
                            })
                      }

                      val seenNames = mutableSetOf<String>()
                      uniqueSuggestions.value =
                          listOfSuggestions.value.filter { suggestion ->
                            val displayName =
                                suggestion.suggestionFormatDisplayName(
                                    maxSuggestionDisplayNameLengthList)
                            if (displayName in seenNames) {
                              false
                            } else {
                              seenNames.add(displayName)
                              true
                            }
                          }

                      for (address in uniqueSuggestions.value.take(6)) {
                        DropdownMenuItem(
                            text = {
                              Text(
                                  address.suggestionFormatDisplayName(
                                      maxSuggestionDisplayNameLengthList))
                            },
                            onClick = {
                              chosenLocation.value = address
                              showSuggestions.value = false
                              myLocation.value = false
                              isTextFieldVisible.value = false
                              textFieldValue.value =
                                  address.suggestionFormatDisplayName(
                                      maxSuggestionDisplayNameLengthList)
                              focusManager.clearFocus()
                            })
                      }
                    }
              }
            }
          } else {
            Text(
                text = stringResource(R.string.all_parkings_radius, radius.value.toInt()),
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface)
          }

          SmallFloatingActionButton(
              onClick = { showFilters = !showFilters },
              icon = if (showFilters) Icons.Default.Close else Icons.Default.FilterList,
              contentDescription = "Filter",
              testTag = "ShowFiltersButton")
        }

    if (showFilters) {
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
                        modifier = Modifier.padding(2.dp),
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
                        modifier = Modifier.padding(2.dp),
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
                        modifier = Modifier.padding(2.dp),
                        testTag = "CapacityFilterItem")
                  }
                }
          }

      // CCTV filter with checkbox
      Row(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = onlyWithCCTV,
                onCheckedChange = { parkingViewModel.setOnlyWithCCTV(it) },
                modifier = Modifier.testTag("CCTVCheckbox"),
                colors = getCheckBoxColors(ColorLevel.PRIMARY))
            Spacer(modifier = Modifier.width(8.dp))
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
                              userViewModel.addFavoriteParkingToSelectedUser(parking)
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
