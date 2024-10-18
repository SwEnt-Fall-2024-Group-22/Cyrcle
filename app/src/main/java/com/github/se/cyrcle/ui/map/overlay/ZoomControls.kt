package com.github.se.cyrcle.ui.map.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.ui.theme.CyrcleTheme

@Composable
fun ZoomControls(onZoomIn: () -> Unit, onZoomOut: () -> Unit) {
  CyrcleTheme {
    Column(
        modifier =
            Modifier.padding(15.dp)
                .background(Color.Black.copy(alpha = 0.70f), shape = RoundedCornerShape(8.dp))
                .sizeIn(maxWidth = 35.dp)
                .testTag("ZoomControls")) {
          IconButton(onClick = onZoomIn, modifier = Modifier.testTag("ZoomControlsIn")) {
            Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White)
          }

          HorizontalDivider(thickness = 1.dp, color = Color.Gray)

          IconButton(onClick = onZoomOut, modifier = Modifier.testTag("ZoomControlsOut")) {
            Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White)
          }
        }
  }
}
