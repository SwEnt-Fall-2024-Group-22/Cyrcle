package com.github.se.cyrcle.ui.add.location

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.map.MapViewModel.LocationPickerState
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.CyrcleTheme
import com.mapbox.maps.MapView

@Composable
fun LocationPickerBottomBar(
    navigationActions: NavigationActions,
    mapViewModel: MapViewModel,
    mapView: MutableState<MapView?>,
) {
  val locationPickerState by mapViewModel.locationPickerState.collectAsState()
  CyrcleTheme {
    Box(Modifier.background(Color.White).height(100.dp)) {
      Row(
          Modifier.fillMaxWidth().wrapContentHeight().padding(16.dp).background(Color.White),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically) {
            Button(
                {
                  mapViewModel.updateCameraPositionWithoutBearing(
                      mapView.value?.mapboxMap?.cameraState!!)
                  navigationActions.navigateTo(Screen.MAP)
                },
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

            if (locationPickerState == LocationPickerState.NONE_SET) {
              Button(
                  { onTopLeftSelected(mapViewModel) },
                  modifier = Modifier.testTag("nextButton"),
                  colors = ButtonDefaults.buttonColors().copy(containerColor = Color.Transparent)) {
                    Text(
                        "Next",
                        modifier = Modifier.width(100.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center)
                  }
            } else if (locationPickerState == LocationPickerState.TOP_LEFT_SET) {
              Button(
                  { onBottomRightSelected(mapViewModel) },
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
}