package com.github.se.cyrcle.ui.addParking.location

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import com.github.se.cyrcle.ui.theme.Typography
import com.github.se.cyrcle.ui.theme.atoms.Text

@Composable
fun LocationPickerTopBar(mapViewModel: MapViewModel) {
  val locationPickerState by mapViewModel.locationPickerState.collectAsState()
  Box(Modifier.background(Color.White).testTag("LocationPickerTopBar")) {
    Row(
        Modifier.fillMaxWidth().wrapContentHeight().padding(16.dp).background(Color.White),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically) {
          Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(R.string.location_picker_top_bar_where),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                style = Typography.bodyLarge)
            if (locationPickerState == LocationPickerState.NONE_SET) {
              Text(
                  stringResource(R.string.location_picker_top_bar_select_top_left),
                  color = MaterialTheme.colorScheme.tertiary)
            } else if (locationPickerState == LocationPickerState.TOP_LEFT_SET) {
              Text(
                  stringResource(R.string.location_picker_top_bar_select_bottom_right),
                  color = MaterialTheme.colorScheme.tertiary)
            }
          }
        }
  }
}
