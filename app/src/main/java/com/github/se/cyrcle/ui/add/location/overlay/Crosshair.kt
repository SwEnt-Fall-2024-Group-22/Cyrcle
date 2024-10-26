package com.github.se.cyrcle.ui.add.location.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.model.map.MapViewModel

@Composable
fun Crosshair(mapViewModel: MapViewModel, paddingvalues: PaddingValues) {
  val locationPickerState by mapViewModel.locationPickerState.collectAsState()

  Box(modifier = Modifier.fillMaxSize().padding(paddingvalues).wrapContentSize(Alignment.Center)) {
    if (locationPickerState == MapViewModel.LocationPickerState.NONE_SET) {
      Box(
          modifier =
              Modifier.width(20.dp).height(2.dp).background(Color.Black).align(Alignment.Center))
      Box(
          modifier =
              Modifier.width(2.dp).height(20.dp).background(Color.Black).align(Alignment.Center))
    }
  }
}
