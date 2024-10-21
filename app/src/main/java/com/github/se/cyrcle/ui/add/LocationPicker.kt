package com.github.se.cyrcle.ui.add

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.CyrcleTheme
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LocationPicker(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel,
    mapViewModel: MapViewModel = MapViewModel()
) {
  val mapViewportState = rememberMapViewportState {
    setCameraOptions {
      val camPos = mapViewModel.cameraPosition.value!!
      center(camPos.center)
      zoom(camPos.zoom)
      bearing(camPos.bearing)
      pitch(camPos.pitch)
    }
  }

  Scaffold(bottomBar = { BottomBarAdd(navigationActions) }, topBar = { TopBarAdd() }) { padding ->
    MapboxMap(
        mapViewportState = mapViewportState,
        style = { MapStyle("mapbox://styles/seanprz/cm27wh9ff00jl01r21jz3hcb1") },
        modifier = Modifier.testTag("LocationPickerScreen"))
  }
}

@Composable
fun BottomBarAdd(navigationActions: NavigationActions) {
  CyrcleTheme {
    Box(Modifier.background(Color.White)) {
      Row(
          Modifier.fillMaxWidth().wrapContentHeight().padding(16.dp).background(Color.White),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically) {
            Button(
                { navigationActions.navigateTo(Screen.MAP) },
                modifier = Modifier.testTag("cancelButton"),
                colors = ButtonDefaults.buttonColors().copy(containerColor = Color.Transparent)) {
                  Text(
                      "Cancel",
                      modifier = Modifier.width(100.dp),
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
                { navigationActions.navigateTo(Screen.ATTRIBUTES_PICKER) },
                modifier = Modifier.testTag("nextButton"),
                colors = ButtonDefaults.buttonColors().copy(containerColor = Color.Transparent)) {
                  Text(
                      "Next",
                      modifier = Modifier.width(100.dp),
                      color = MaterialTheme.colorScheme.primary,
                      fontWeight = FontWeight.Bold,
                      textAlign = TextAlign.Center)
                }
          }
    }
  }
}

@Preview
@Composable
fun TopBarAdd() {
  CyrcleTheme {
    Box(Modifier.background(Color.White)) {
      Row(
          Modifier.fillMaxWidth().wrapContentHeight().padding(16.dp).background(Color.White),
          horizontalArrangement = Arrangement.SpaceAround,
          verticalAlignment = Alignment.CenterVertically) {
            Column {
              Text(
                  "Where is the Parking ?",
                  color = MaterialTheme.colorScheme.primary,
                  fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                  fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                  modifier = Modifier.padding(start = 16.dp),
                  fontWeight = FontWeight.Bold)
              Text("Click on the location of the new parking")
            }
          }
    }
  }
}
