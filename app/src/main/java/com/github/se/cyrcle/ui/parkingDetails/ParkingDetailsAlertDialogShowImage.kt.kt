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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.mapbox.maps.extension.style.expressions.dsl.generated.mod

/**
 * Alert dialog to show the image of a parking spot. This composable is displayed when the user
 * clicks on an image of a parking spot.
 *
 * @param onDismiss Callback when the dialog is dismissed.
 * @param imageUrl URL of the image to display.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ParkingDetailsAlertDialogShowImage(onDismiss: () -> Unit, imageUrl: String) {
  BasicAlertDialog(
      modifier = Modifier.testTag("ParkingDetailsAlertDialogShowImage"),
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
                  modifier = Modifier.fillMaxWidth().testTag("parkingDetailsAlertDialogImage"))
            }
      })
}
