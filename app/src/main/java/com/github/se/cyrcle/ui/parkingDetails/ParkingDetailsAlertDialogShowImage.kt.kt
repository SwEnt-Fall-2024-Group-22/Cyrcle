package com.github.se.cyrcle.ui.parkingDetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.github.se.cyrcle.ui.theme.atoms.IconButton
import com.github.se.cyrcle.ui.theme.atoms.Text

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
  val alertDialogMaxHeight = LocalConfiguration.current.screenHeightDp.dp * 0.8f

  BasicAlertDialog(
      modifier = Modifier.testTag("ParkingDetailsAlertDialogShowImage").wrapContentSize(),
      onDismissRequest = onDismiss,
      content = {
        Box(
            modifier =
                Modifier.heightIn(max = alertDialogMaxHeight).wrapContentSize().wrapContentSize()) {
              Image(
                  painter = rememberAsyncImagePainter(imageUrl),
                  contentDescription = "Parking spot image",
                  modifier =
                      Modifier.wrapContentWidth()
                          .background(
                              MaterialTheme.colorScheme.background,
                              MaterialTheme.shapes.small) // Set the background color
                          .padding(4.dp)
                          .testTag("parkingDetailsAlertDialogImage"))
              IconButton(
                  modifier = Modifier.padding(8.dp),
                  icon = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Back",
                  onClick = onDismiss,
                  inverted = true)
            FloatingActionButton ( onClick = {},
                modifier = Modifier.padding(8.dp), content = {
                    Text("REPORT IMAGE")
                })

            }
      })
}
