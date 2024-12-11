package com.github.se.cyrcle.ui.parkingDetails

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.atoms.IconButton

/**
 * Alert dialog to show the image of a parking spot. This composable is displayed when the user
 * clicks on an image of a parking spot, as well as report it when necessary
 *
 * @param onDismiss Callback when the dialog is dismissed.
 * @param imageUrl URL of the image to display.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ParkingDetailsAlertDialogShowImage(
    parkingViewModel: ParkingViewModel,
    userViewModel: UserViewModel,
    onDismiss: () -> Unit,
    imageUrl: String,
    navigationActions: NavigationActions
) {
  val context = LocalContext.current
  val strResToast = stringResource(R.string.view_profile_screen_image_deleted)
  val alertDialogMaxHeight = LocalConfiguration.current.screenHeightDp.dp * 0.8f
  BasicAlertDialog(
      modifier = Modifier.testTag("ParkingDetailsAlertDialogShowImage").wrapContentSize(),
      onDismissRequest = onDismiss,
      content = {
        Box(modifier = Modifier.heightIn(max = alertDialogMaxHeight).wrapContentSize()) {
          // Image
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

          // Back Button (Top Left)
          IconButton(
              modifier = Modifier.align(Alignment.TopStart).padding(6.dp),
              icon = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              onClick = onDismiss,
              inverted = true)

          // Conditional Delete or Report Button (Top Right)
          val isOwner =
              parkingViewModel.selectedImageObject.collectAsState().value?.owner ==
                  userViewModel.currentUser.collectAsState().value?.public?.userId

          if (isOwner) {
            IconButton(
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                icon = Icons.Outlined.RestoreFromTrash,
                contentDescription = "Delete",
                onClick = {
                  parkingViewModel.deleteImageFromParking(
                      parkingViewModel.selectedParking.value?.uid!!,
                      parkingViewModel.selectedImageObject.value?.imagePath!!)
                  navigationActions.navigateTo(Screen.PARKING_DETAILS)
                  Toast.makeText(context, strResToast, Toast.LENGTH_SHORT).show()
                },
                inverted = true)
          } else {
            IconButton(
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                icon = Icons.Outlined.Flag,
                contentDescription = "Report",
                onClick = { navigationActions.navigateTo(Screen.IMAGE_REPORT) },
                inverted = true,
                colorLevel = ColorLevel.ERROR)
          }
        }
      })
}
