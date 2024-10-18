package com.github.se.cyrcle.ui.map.overlay.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.github.se.cyrcle.ui.map.overlay.AddButton
import com.github.se.cyrcle.ui.map.overlay.ZoomControls

// Zoom controls preview
@Preview
@Composable
fun ZoomControlsPreview() {
  ZoomControls(onZoomIn = {}, onZoomOut = {})
}

@Preview
@Composable
fun AddButtonPreview() {
  AddButton({})
}
