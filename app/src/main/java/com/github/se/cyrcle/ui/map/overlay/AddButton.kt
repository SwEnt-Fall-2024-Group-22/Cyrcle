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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.ui.theme.CyrcleTheme

@Composable
fun AddButton(onClick: () -> Unit) {
  CyrcleTheme {
    Box(Modifier.padding(16.dp)) {
      IconButton(
          onClick = onClick,
          modifier =
              Modifier.size(48.dp)
                  .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
                  .testTag("AddButton")) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
          }
    }
  }
}
