package com.github.se.cyrcle.ui.add

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.CyrcleTheme
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LocationPicker(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel,
    mapViewModel: MapViewModel = MapViewModel()
) {
  LaunchedEffect(Unit) { mapViewModel.updateSelectedPoint(null) }
  var annotationManager by remember { mutableStateOf<PointAnnotationManager?>(null) }
  val context = LocalContext.current
  val mapViewportState = rememberMapViewportState {
    setCameraOptions {
      val camPos = mapViewModel.cameraPosition.value!!
      center(camPos.center)
      zoom(camPos.zoom)
      bearing(camPos.bearing)
      pitch(camPos.pitch)
    }
  }

  Scaffold(
      bottomBar = { BottomBarAdd(navigationActions, mapViewModel) }, topBar = { TopBarAdd() }) {
          padding ->
        MapboxMap(
            mapViewportState = mapViewportState,
            style = { MapStyle("mapbox://styles/seanprz/cm27wh9ff00jl01r21jz3hcb1") },
            modifier = Modifier.testTag("LocationPickerScreen"),
            onMapLongClickListener = { point ->
              mapViewModel.updateSelectedPoint(point)
              true
            }) {
              DisposableMapEffect { mapView ->
                annotationManager = mapView.annotations.createPointAnnotationManager()
                onDispose { annotationManager!!.deleteAll() }
              }
            }
        val selectedPoint by mapViewModel.selectedPoint.collectAsState()
        LaunchedEffect(selectedPoint) {
          val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.red_marker)
          val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 100, 150, false)

          annotationManager?.deleteAll()
          if (selectedPoint != null) {
            annotationManager?.create(
                PointAnnotationOptions()
                    .withIconOffset(listOf(0.0, -25.0))
                    .withPoint(selectedPoint!!)
                    .withIconSize(1.0)
                    .withIconImage(resizedBitmap))
          }
        }
      }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun BottomBarAdd(navigationActions: NavigationActions, mapViewModel: MapViewModel) {
  val selectedPoint by mapViewModel.selectedPoint.collectAsState()

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

            if (selectedPoint != null) {
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
            } else {
              Button(
                  {},
                  modifier = Modifier.testTag("nextButton"),
                  colors = ButtonDefaults.buttonColors().copy(containerColor = Color.Transparent)) {
                    Text(
                        "Next",
                        modifier = Modifier.width(100.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center)
                  }
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
