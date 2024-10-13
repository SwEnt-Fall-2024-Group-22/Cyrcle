package com.github.se.cyrcle.ui.map.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.ui.theme.Cerulean

@Composable
fun AddButton(onClick: () -> Unit) {
  Box(Modifier.padding(16.dp)) {
    IconButton(
        onClick = onClick,
        modifier =
            Modifier.size(48.dp)
                .background(color = Cerulean, shape = CircleShape)
                .testTag("AddButton")) {
          Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
        }
  }
}

@Preview
@Composable
fun AddButtonPreview() {
  AddButton({})
}
