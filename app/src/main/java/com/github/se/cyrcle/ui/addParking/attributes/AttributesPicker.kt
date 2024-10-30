package com.github.se.cyrcle.ui.addParking.attributes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.model.address.AddressViewModel
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingAttribute
import com.github.se.cyrcle.model.parking.ParkingCapacity
import com.github.se.cyrcle.model.parking.ParkingProtection
import com.github.se.cyrcle.model.parking.ParkingRackType
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.ui.map.MapConfig
import com.github.se.cyrcle.ui.map.drawRectangles
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.TopLevelDestinations
import com.github.se.cyrcle.ui.theme.CyrcleTheme
import com.github.se.cyrcle.ui.theme.atoms.InputText
import com.github.se.cyrcle.ui.theme.molecules.BooleanRadioButton
import com.github.se.cyrcle.ui.theme.molecules.DynamicSelectTextField
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolygonAnnotationManager
import com.mapbox.maps.plugin.gestures.generated.GesturesSettings

@Composable
fun AttributesPicker(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel,
    mapViewModel: MapViewModel,
    addressViewModel: AddressViewModel
) {
  val location = mapViewModel.selectedLocation.collectAsState().value!!
  addressViewModel.search(location.center)
  val suggestedAddress = addressViewModel.address.collectAsState().value
  val title = remember { mutableStateOf(suggestedAddress.displayRelevantFields()) }
  val description = remember { mutableStateOf("") }
  val rackType = remember { mutableStateOf<ParkingAttribute>(ParkingRackType.TWO_TIER) }
  val capacity = remember { mutableStateOf<ParkingAttribute>(ParkingCapacity.XSMALL) }
  val protection = remember { mutableStateOf<ParkingAttribute>(ParkingProtection.INDOOR) }
  val hasSecurity = remember { mutableStateOf(false) }

  fun onSubmit() {
    val parking =
        Parking(
            optName = title.value,
            optDescription = description.value,
            rackType = rackType.value as ParkingRackType,
            capacity = capacity.value as ParkingCapacity,
            protection = protection.value as ParkingProtection,
            hasSecurity = hasSecurity.value,
            location = location,
            images = listOf(),
            price = 0.0,
            uid = parkingViewModel.getNewUid())
    parkingViewModel.addParking(parking)
    navigationActions.navigateTo(TopLevelDestinations.MAP)
  }
  Scaffold(
      topBar = { AttributePickerTopBar(mapViewModel, title) },
      bottomBar = { BottomBarAddAttr(navigationActions) { onSubmit() } }) { padding ->
        Column(
            modifier =
                Modifier.testTag("AttributesPickerScreen")
                    .padding(16.dp, padding.calculateTopPadding() + 16.dp)) {
              InputText(
                  value = title.value,
                  onValueChange = { title.value = it },
                  label = "Title",
                  modifier = Modifier.fillMaxWidth().padding(10.dp),
              )
              DynamicSelectTextField(
                  options = ParkingProtection.entries.toList(),
                  selectedValue = protection,
                  label = "How protected from the weather is the spot?",
              )
              DynamicSelectTextField(
                  options = ParkingCapacity.entries.toList(),
                  selectedValue = capacity,
                  label = "How many bikes can we park ?",
              )
              DynamicSelectTextField(
                  options = ParkingRackType.entries.toList(),
                  selectedValue = rackType,
                  label = "Which type of rack is in the parking spot?",
              )
              Spacer(modifier = Modifier.height(16.dp))
              BooleanRadioButton(
                  question = "Is the parking spot under camera surveillance ?", hasSecurity)
              Spacer(modifier = Modifier.height(16.dp))
              InputText(
                  value = description.value,
                  label = "Description (Optional)",
                  onValueChange = { description.value = it },
                  singleLine = false,
                  minLines = 3,
                  maxLines = 5,
                  modifier = Modifier.fillMaxWidth().padding(16.dp))
            }
      }
}

@Composable
fun BottomBarAddAttr(navigationActions: NavigationActions, onSubmit: () -> Unit) {
  CyrcleTheme {
    Box(Modifier.background(Color.White)) {
      Row(
          Modifier.fillMaxWidth().wrapContentHeight().padding(16.dp).background(Color.White),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically) {
            Button(
                { navigationActions.navigateTo(TopLevelDestinations.MAP) },
                modifier = Modifier.testTag("cancelButton"),
                colors = ButtonDefaults.buttonColors().copy(containerColor = Color.Transparent)) {
                  Text(
                      "Cancel",
                      color = MaterialTheme.colorScheme.primary,
                      fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                      fontWeight = FontWeight.Bold,
                      textAlign = TextAlign.Center)
                }

            VerticalDivider(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.height(32.dp).width(1.dp),
                thickness = 2.dp)
            Button(
                onClick = { onSubmit() },
                modifier = Modifier.testTag("submitButton"),
                colors = ButtonDefaults.buttonColors().copy(containerColor = Color.Transparent)) {
                  Text(
                      "Submit",
                      color = MaterialTheme.colorScheme.primary,
                      fontWeight = FontWeight.Bold,
                      textAlign = TextAlign.Center)
                }
          }
    }
  }
}

@Composable
fun AttributePickerTopBar(mapViewModel: MapViewModel, title: MutableState<String>) {
  var annotationManager by remember { mutableStateOf<PolygonAnnotationManager?>(null) }
  val topBarHeight = remember { mutableIntStateOf(0) }
  val location = mapViewModel.selectedLocation.collectAsState().value!!
  val mapViewportState = rememberMapViewportState {
    setCameraOptions {
      center(Point.fromLngLat(location.center.longitude() - 0.0015, location.center.latitude()))
      zoom(16.0)
      bearing(0.0)
      pitch(0.0)
    }
  }
  // move the camera a bit right

  Box(
      modifier =
          Modifier.fillMaxWidth().height(120.dp).clipToBounds().onGloballyPositioned { coordinates
            ->
            topBarHeight.intValue = coordinates.size.height
          }) {
        val mapState = rememberMapState()
        mapState.gesturesSettings = GesturesSettings {
          scrollEnabled = false
          quickZoomEnabled = false
          pinchToZoomEnabled = false
          doubleTapToZoomInEnabled = false
          doubleTouchToZoomOutEnabled = false
          rotateEnabled = false
          pitchEnabled = false
        }

        MapboxMap(
            mapViewportState = mapViewportState,
            style = { MapConfig.DefaultStyle() },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            mapState = mapState,
            attribution = {},
            scaleBar = {}) {
              DisposableMapEffect { mapView ->
                annotationManager = mapView.annotations.createPolygonAnnotationManager()
                onDispose { annotationManager!!.deleteAll() }
              }
            }

        Box(
            modifier =
                Modifier.matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colors =
                                listOf(
                                    Color.White.copy(alpha = 0.9f),
                                    Color.White.copy(alpha = 0.9f),
                                    Color.White.copy(alpha = 0.5f),
                                    Color.White.copy(alpha = 0.3f)))))
        Column {
          Text(
              "New Parking Spot",
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.primary,
              fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(top = 16.dp, start = 8.dp),
          )
          Text(
              title.value,
              color = MaterialTheme.colorScheme.primary,
              fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(start = 8.dp))
          Text(
              "Set the attributes of the parking spot",
              Modifier.padding(top = 0.dp, start = 8.dp, bottom = 16.dp),
              color = MaterialTheme.colorScheme.tertiary,
              fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
              fontWeight = FontWeight.Bold,
          )
        }
        drawRectangles(annotationManager, listOf(location))
      }
}
