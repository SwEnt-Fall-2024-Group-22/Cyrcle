package com.github.se.cyrcle.ui.addParking.location

import android.util.Log
import android.widget.Toast
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
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.map.MapViewModel.LocationPickerState
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.Typography
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.disabledColor
import com.mapbox.maps.MapView

@Composable
fun LocationPickerBottomBar(
    navigationActions: NavigationActions,
    mapViewModel: MapViewModel,
    mapView: MutableState<MapView?>,
) {
  val locationPickerState by mapViewModel.locationPickerState.collectAsState()
  val isLocationValid by mapViewModel.isLocationValid.collectAsState(true)
  Box(Modifier.background(Color.White).height(100.dp).testTag("LocationPickerBottomBar")) {
    Row(
        Modifier.fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background),
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
                    stringResource(R.string.location_picker_bottom_bar_cancel_button),
                    modifier = Modifier.width(100.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = Typography.headlineMedium,
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
                      stringResource(R.string.location_picker_bottom_bar_next_button),
                      modifier = Modifier.width(100.dp),
                      color = MaterialTheme.colorScheme.primary,
                      style = Typography.headlineMedium,
                      textAlign = TextAlign.Center)
                }
          } else if (locationPickerState == LocationPickerState.TOP_LEFT_SET) {
            Button(
                {
                  if (isLocationValid) {
                    onBottomRightSelected(mapViewModel)
                  } else {
                    Toast.makeText(
                            mapView.value?.context,
                            R.string.location_picker_invalid_area,
                            Toast.LENGTH_SHORT)
                        .show()
                  }
                },
                modifier = Modifier.testTag("nextButton"),
                colors = ButtonDefaults.buttonColors().copy(containerColor = Color.Transparent)) {
                  Log.d("LocationPickerBottomBar", "isLocationValid: $isLocationValid")
                  Text(
                      stringResource(R.string.location_picker_bottom_bar_next_button),
                      modifier = Modifier.width(100.dp),
                      color =
                          if (isLocationValid) MaterialTheme.colorScheme.primary
                          else disabledColor(),
                      style = Typography.headlineMedium,
                      textAlign = TextAlign.Center)
                }
          }
        }
  }
}
