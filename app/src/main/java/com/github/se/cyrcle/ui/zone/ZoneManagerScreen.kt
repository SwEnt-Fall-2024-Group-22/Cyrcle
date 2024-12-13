package com.github.se.cyrcle.ui.zone

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.Location
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.model.zone.Zone
import com.github.se.cyrcle.ui.map.MapConfig
import com.github.se.cyrcle.ui.map.minZoom
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.atoms.IconButton
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

/** Screen where users can manage their downloaded zones. and access the zone selection screen. */
@Composable
fun ZoneManagerScreen(
    mapViewModel: MapViewModel,
    parkingViewModel: ParkingViewModel,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel
) {
  val context = LocalContext.current
  val zones = remember { mutableStateOf<List<Zone>>(emptyList()) }
  val displayOnlineElement = userViewModel.displayOnlineElementFlow.collectAsState(initial = false)
  LaunchedEffect(Unit) {
    zones.value = Zone.loadZones(context)
    Log.d("ZoneManagerScreen", "Zones: ${zones.value}")
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = stringResource(R.string.zone_manager_screen_title),
            navigationActions = navigationActions)
      }) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
          // === Content ===
          Column(Modifier.fillMaxWidth()) {
            ZoneHeader()
            if (zones.value.isEmpty()) {
              Text(
                  text = stringResource(R.string.zone_manager_no_zones),
                  modifier = Modifier.padding(16.dp))
            }
            LazyColumn(modifier = Modifier.fillMaxSize().padding(bottom = 8.dp)) {
              items(zones.value) { zone ->
                ZoneCard(parkingViewModel, mapViewModel, navigationActions, zone, zones)
                if (zone != zones.value.last()) {
                  HorizontalDivider(
                      color = MaterialTheme.colorScheme.onBackground.copy(0.5f),
                      thickness = 1.dp,
                      modifier =
                          Modifier.padding(horizontal = 16.dp)
                              .fillMaxWidth(0.75f)
                              .align(Alignment.CenterHorizontally))
                }
              }
            }
          }
          // === Overlay ===
          if (displayOnlineElement.value) {
            AddZoneButton(mapViewModel, navigationActions)
          }

          // ===  === ===  ===
        }
      }
}
// The overlaid button that allows the user to add a new zone.
@Composable
fun AddZoneButton(mapViewModel: MapViewModel, navigationActions: NavigationActions) {
  Box(modifier = Modifier.fillMaxSize()) {
    IconButton(
        icon = Icons.Default.Add,
        contentDescription = "Add new Zone",
        modifier =
            Modifier.align(Alignment.BottomStart)
                .scale(1.2f)
                .padding(bottom = 25.dp, start = 16.dp),
        onClick = {
          mapViewModel.updateLocationPickerState(MapViewModel.LocationPickerState.NONE_SET)
          navigationActions.navigateTo(Screen.ZONE_SELECTION)
        },
        colorLevel = ColorLevel.PRIMARY,
        testTag = "AddButton")
  }
}
// Display relevant information about a zone. Allows the user to refresh the zone or delete it.
@Composable
fun ZoneCard(
    parkingViewModel: ParkingViewModel,
    mapViewModel: MapViewModel,
    navigationActions: NavigationActions,
    zone: Zone,
    zones: MutableState<List<Zone>>
) {
  val context = LocalContext.current
  Row(
      modifier = Modifier.padding(16.dp).fillMaxWidth().testTag("ZoneCard"),
      horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = zone.name,
            style =
                TextStyle(textDecoration = TextDecoration.Underline, fontStyle = FontStyle.Italic),
            textAlign = TextAlign.Left,
            modifier =
                Modifier.weight(2f).testTag("ZoneCardName").clickable {
                  mapViewModel.updateTrackingMode(false)
                  mapViewModel.updateMapRecentering(true)
                  mapViewModel.zoomOnLocation(Location(zone.boundingBox.southwest()), minZoom)
                  navigationActions.navigateTo(Screen.MAP)
                })
        Text(text = zone.lastRefreshed.toString(), modifier = Modifier.weight(1f))
        Icon(
            Icons.Filled.Refresh,
            contentDescription = "Refresh",
            modifier =
                Modifier.weight(0.2f).clickable {
                  Zone.refreshZone(zone, context)
                  zones.value = Zone.loadZones(context)
                  parkingViewModel.downloadZone(zone, {}, {})
                  MapConfig.downloadZone(zone)
                  // Call functions to update map tiles and parking data here.
                })
        Icon(
            Icons.Outlined.Delete,
            contentDescription = "Delete",
            modifier =
                Modifier.weight(0.2f).clickable {
                  Zone.deleteZone(zone, context)
                  zones.value = Zone.loadZones(context)
                  parkingViewModel.deleteZone(zone, zones.value)
                  MapConfig.deleteZoneFromStorage(zone)
                })
      }
}
// The header of the zone manager screen.
@Composable
fun ZoneHeader() {
  Row(
      modifier = Modifier.padding(16.dp).fillMaxWidth().testTag("ZoneManagerHeader"),
      horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = stringResource(R.string.zone_manager_header_area),
            textAlign = TextAlign.Left,
            modifier = Modifier.weight(2f).testTag("ZoneManagerHeaderArea"),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
        Text(
            text = stringResource(R.string.zone_manager_header_lastRefreshed),
            modifier = Modifier.weight(1f).testTag("ZoneManagerHeaderLastRefreshed"),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
        Icon(
            Icons.Filled.Refresh,
            contentDescription = "Refresh",
            modifier = Modifier.weight(0.2f).alpha(0.0f))
        Icon(
            Icons.Filled.Delete,
            contentDescription = "Delete",
            modifier = Modifier.weight(0.2f).alpha(0.0f))
      }
}
