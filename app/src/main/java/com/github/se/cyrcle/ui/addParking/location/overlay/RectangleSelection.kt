package com.github.se.cyrcle.ui.addParking.location.overlay

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.mapbox.maps.ScreenCoordinate

/** This composable is used to draw a rectangle on the map to select a region. */
@Composable
fun RectangleSelection(mapViewModel: MapViewModel, paddingValues: PaddingValues) {
  val locationPickerState by mapViewModel.locationPickerState.collectAsState()
  val center = Offset(0.5f, 0.5f)
  var touchPosition by remember { mutableStateOf(Offset.Unspecified) }
  val hasDragged = remember { mutableStateOf(false) }
  val mapGesturesEnabled = remember { mutableStateOf(false) }
  var width by remember { mutableFloatStateOf(0f) }
  var height by remember { mutableFloatStateOf(0f) }
  var canvasCenter by remember { mutableStateOf(Offset.Unspecified) }
  if (locationPickerState == MapViewModel.LocationPickerState.TOP_LEFT_SET) {
    Canvas(
        modifier =
            Modifier.padding(paddingValues)
                .fillMaxSize()
                .then(
                    if (!mapGesturesEnabled.value) {
                      Modifier.pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                          hasDragged.value = true
                          touchPosition += dragAmount
                        }
                      }
                    } else {
                      Modifier
                    })) {
          canvasCenter = Offset(size.width * center.x, size.height * center.y)
          if (touchPosition == Offset.Unspecified) {
            touchPosition = canvasCenter
          }
          width = touchPosition.x - canvasCenter.x
          height = touchPosition.y - canvasCenter.y
          // Convert width o the same unit as the canvas center
          val rectSize = Size(width, height)
          if (hasDragged.value) {
            drawRect(
                color = Color(0xFF22799B).copy(alpha = 0.7f),
                topLeft = canvasCenter,
                size = rectSize)
            drawRect(
                color = Color(0xFF22799B),
                topLeft = canvasCenter,
                size = rectSize,
                style = Stroke(width = 2f))
          }
        }
    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
      Row(
          horizontalArrangement = Arrangement.End,
          modifier = Modifier.fillMaxSize().padding(end = 55.dp),
      ) {
        androidx.compose.material3.Button(
            modifier = Modifier.testTag("toggleRectangleButton"),
            onClick = { mapGesturesEnabled.value = !mapGesturesEnabled.value },
            content = {
              if (mapGesturesEnabled.value) {
                Text(stringResource(R.string.rectangle_selection_lock_map))
              } else {
                Text(stringResource(R.string.rectangle_selection_unlock_map))
              }
            })
      }
    }
  }
  if (locationPickerState == MapViewModel.LocationPickerState.BOTTOM_RIGHT_SET) {
    // get all four coordinates of the rectangle
    val topLeft = ScreenCoordinate((canvasCenter.x).toDouble(), (canvasCenter.y).toDouble())
    val topRight =
        ScreenCoordinate((canvasCenter.x + width).toDouble(), (canvasCenter.y).toDouble())
    val bottomRight =
        ScreenCoordinate((canvasCenter.x + width).toDouble(), (canvasCenter.y + height).toDouble())
    val bottomLeft =
        ScreenCoordinate((canvasCenter.x).toDouble(), (canvasCenter.y + height).toDouble())
    mapViewModel.updateScreenCoordinates(listOf(topLeft, topRight, bottomRight, bottomLeft))
    mapViewModel.updateLocationPickerState(MapViewModel.LocationPickerState.RECTANGLE_SET)
  }
}
