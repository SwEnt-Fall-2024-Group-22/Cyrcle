package com.github.se.cyrcle.ui.zone

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.address.AddressViewModel
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.map.MapViewModel.LocationPickerState
import com.github.se.cyrcle.model.zone.Zone
import com.github.se.cyrcle.ui.addParking.location.overlay.Crosshair
import com.github.se.cyrcle.ui.addParking.location.overlay.RectangleSelection
import com.github.se.cyrcle.ui.map.MapConfig
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.atoms.Button
import com.github.se.cyrcle.ui.theme.atoms.InputText
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar
import com.mapbox.geojson.BoundingBox
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.plugin.gestures.gestures
import kotlinx.coroutines.channels.ticker
import java.io.File

/** Screen where users can select a new zone to download. */
@Composable
fun ZoneSelectionScreen(navigationActions: NavigationActions, mapViewModel: MapViewModel, addressViewModel: AddressViewModel) {
    val showAlertDialogPickName = remember { mutableStateOf(false) }
    val boundingBox = remember { mutableStateOf<BoundingBox?>(null) }
    val zoneName = remember { mutableStateOf("") }
  val mapView = remember { mutableStateOf<MapView?>(null) }
  val locationPickerState by mapViewModel.locationPickerState.collectAsState() // state representing where the user is in the location selection process
  mapViewModel.unZoomActualCamera() // automatically zoom out and reset bearing, because no one want to download a zone that is too zoomed in.
  val mapViewportState = MapConfig.createMapViewPortStateFromViewModel(mapViewModel)
    LaunchedEffect(Unit) {
        // Needs to be in a launchedEffect to not be called every time the locationPickerState changes, otherwise it would override the locationPickerState.
        mapViewModel.updateLocation(null) // This is not related to the camera position.
        mapViewModel.updateLocationPickerState(LocationPickerState.NONE_SET) // reset the location picker state
    }
  LaunchedEffect(locationPickerState) {
      // Once the composabe RectangleSelection is done setting the rectangle, we show the alert dialog to pick the name of the zone.
      if (locationPickerState == LocationPickerState.RECTANGLE_SET) {
          // Firs get the screen Coords from the viewmodel, computed by the RectangleSelection composable.
          val screenCoordsList = mapViewModel.screenCoordinates.value
          // Then convert the screen coords to map coords.
          val pointsList = mapView.value?.mapboxMap?.coordinatesForPixels(screenCoordsList)!!
          // Then find the bottom left and top right points of the bounding box.
            val bottomLeft = pointsList.minByOrNull { it.latitude() + it.longitude() }!! // The point with the smallest latitude is the bottom left.
            val topRight = pointsList.maxByOrNull { it.latitude() + it.longitude()}!! // The point with the biggest latitude is the top right.
          // Store the bounding box in a state.
          boundingBox.value = BoundingBox.fromPoints(bottomLeft, topRight)
          // Show the alert dialog to pick the name of the zone.
            showAlertDialogPickName.value = true
    }
  }

    if(showAlertDialogPickName.value) {
        val context = LocalContext.current
        AlertDialogPickZoneName(
            onConfirm = { zoneName.value = it; createZone(boundingBox.value!!, it,context, navigationActions) },
            onDismiss = { showAlertDialogPickName.value = false; mapViewModel.updateLocationPickerState(LocationPickerState.NONE_SET) }
        )
    }

  Scaffold(
      topBar = {
        TopAppBar(navigationActions, title = stringResource(R.string.zone_selection_screen_title))
      }) { padding ->
        MapboxMap(
            mapViewportState = mapViewportState,
            style = { MapConfig.DefaultStyle() },
            modifier = Modifier.testTag("LocationPickerScreen").padding(padding),
        ) {
          DisposableMapEffect { mapViewInstance ->
            mapView.value = mapViewInstance
            mapViewInstance.gestures.getGesturesManager().rotateGestureDetector.isEnabled = false
            onDispose {}
          }
        }
        RectangleSelection(mapViewModel, padding, mapView.value, restrictSelectionSize = false)
        Crosshair(mapViewModel, padding)
      ZoneSelectionButton(mapViewModel, locationPickerState)
      }
}

// Make a button that first say "Set top left corner" and then "Set bottom right corner" when clicked, then navigate to the next screen (or alert dialog), where the user name the zone. then he will finally click on download.
@Composable
fun ZoneSelectionButton(mapViewModel: MapViewModel, locationPickerState: LocationPickerState) {
    // Define the click action for the button of the zone selection screen.
    fun onClick() {
        when (locationPickerState) {
            LocationPickerState.NONE_SET ->
                { mapViewModel.updateLocationPickerState(LocationPickerState.TOP_LEFT_SET) }
            LocationPickerState.TOP_LEFT_SET ->
                { mapViewModel.updateLocationPickerState(LocationPickerState.BOTTOM_RIGHT_SET) }
            else ->
                {}
        }
    }
    // Define the text of the button depending on the state of the location picker.
    val buttonText = when (locationPickerState) {
        LocationPickerState.NONE_SET -> "Set top left corner"
        LocationPickerState.TOP_LEFT_SET -> "Download Area "
        else -> "Please wait..."
    }
    // The button itself
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            text=buttonText,
            onClick = { onClick() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 32.dp)
                .testTag("SetTopLeftCornerButton")
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDialogPickZoneName(defaultName : String ="", onConfirm: (zoneName : String) -> Unit = {}, onDismiss : () -> Unit = {}) {
    val zoneName = remember { mutableStateOf(defaultName) }
    BasicAlertDialog(
        onDismissRequest = {onDismiss()},
        content =
        {
            Column(
                modifier = Modifier.padding(16.dp).background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.small)
            ) {
                Text(
                    text = "Please name the Area you are about to download",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.padding(8.dp))
                InputText(
                    value = zoneName.value,
                    onValueChange = { zoneName.value = it },
                    label = "Zone name",
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        text = "Cancel",
                        onClick = { onDismiss() },
                    )
                    Button(
                        text = "Confirm",
                        onClick = { onConfirm(zoneName.value) }
                    )
                }
            }
        })
}


private fun createZone(boundingBox: BoundingBox, zoneName: String, context: Context, navigationActions: NavigationActions) {
    val zone = Zone(boundingBox, zoneName)
    Log.d("ZoneSelectionScreen", "Zone created: $zone")
   val zoneDir = File(context.filesDir, "zones")
    Zone.storeZone(zone, zoneDir)
    // Add calls to function to download the tiles corresponding and the data corresponding to the zone.
    navigationActions.goBack()
}



