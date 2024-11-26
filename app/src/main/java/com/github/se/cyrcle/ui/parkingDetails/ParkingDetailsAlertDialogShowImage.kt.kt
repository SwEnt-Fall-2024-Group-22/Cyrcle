package com.github.se.cyrcle.ui.parkingDetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ParkingDetailsAlertDialogShowImage(onDismiss: () -> Unit, imageUrl: String) {
  BasicAlertDialog(
      onDismissRequest = onDismiss,
      content = {
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
                    .wrapContentSize()) {
              Image(
                  painter = rememberAsyncImagePainter(imageUrl),
                  contentDescription = "Parking spot image",
                  modifier = Modifier.fillMaxWidth())
            }
      })
}
