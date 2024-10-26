package com.github.se.cyrcle.ui.add.location

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.map.MapViewModel.LocationPickerState
import com.github.se.cyrcle.ui.theme.CyrcleTheme

@Composable
fun LocationPickerTopBar(mapViewModel: MapViewModel) {
  val locationPickerState by mapViewModel.locationPickerState.collectAsState()
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
              if (locationPickerState == LocationPickerState.NONE_SET) {
                Text(
                    "Set the top-left corner of the parking",
                    color = MaterialTheme.colorScheme.tertiary)
              } else if (locationPickerState == LocationPickerState.TOP_LEFT_SET) {
                Text(
                    "Set the bottom-right corner of the parking",
                    color = MaterialTheme.colorScheme.tertiary)
              }
            }
          }
    }
  }
}
