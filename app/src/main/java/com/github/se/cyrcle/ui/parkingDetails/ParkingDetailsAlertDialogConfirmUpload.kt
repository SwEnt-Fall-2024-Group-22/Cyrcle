package com.github.se.cyrcle.ui.parkingDetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.github.se.cyrcle.R
import com.github.se.cyrcle.ui.theme.atoms.Text

/**
 * Alert dialog to confirm the upload of a new parking image.
 *
 * @param onDismiss Callback when the dialog is dismissed.
 * @param onAccept Callback when the dialog is accepted.
 * @param newParkingImageLocalPath Local path of the new parking image.
 */
@Composable
fun ParkingDetailsAlertDialogConfirmUpload(
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
    newParkingImageLocalPath: String,
) {
  AlertDialog(
      modifier = Modifier.testTag("parkingDetailsAlertDialog"),
      onDismissRequest = { onDismiss() },
      title = { Text(stringResource(R.string.card_screen_confirm_upload)) },
      containerColor = MaterialTheme.colorScheme.surfaceBright,
      text = {
        Image(
            painter = rememberAsyncImagePainter(newParkingImageLocalPath),
            contentDescription = stringResource(R.string.view_profile_screen_profile_picture),
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop)
      },
      confirmButton = {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
          // Cancel button (No)
          TextButton(
              onClick = { onDismiss() },
              modifier = Modifier.padding(start = 8.dp, end = 8.dp).testTag("cancelButton")) {
                Text(
                    stringResource(R.string.no),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp))
              }
          // Confirmation button (Yes)
          TextButton(
              onClick = { onAccept() },
              modifier = Modifier.padding(start = 8.dp, end = 8.dp).testTag("acceptButton")) {
                Text(
                    stringResource(R.string.yes),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp))
              }
        }
      })
}
