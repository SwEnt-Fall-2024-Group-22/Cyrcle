package com.github.se.cyrcle.ui.parkingDetails

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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
import com.github.se.cyrcle.ui.theme.molecules.DeleteConfirmationDialog

/**
 * Alert dialog to show the image of a parking spot. This composable is displayed when the user
 * clicks on an image of a parking spot, allowing the owner to delete or other users to report it.
 *
 * @param parkingViewModel ViewModel handling parking-related operations.
 * @param userViewModel ViewModel handling user-related operations.
 * @param onDismiss Callback when the dialog is dismissed.
 * @param imageUrl URL of the image to display.
 * @param navigationActions Navigation actions to navigate to different screens.
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
  val imageMinWidth = LocalConfiguration.current.screenWidthDp.dp * 0.8f

  // State for delete confirmation dialog
  val showDeleteDialog = remember { mutableStateOf(false) }

  BasicAlertDialog(
      modifier = Modifier.testTag("ParkingDetailsAlertDialogShowImage").wrapContentSize(),
      onDismissRequest = onDismiss,
      content = {
        Box(modifier = Modifier.wrapContentSize()) {
          // Image
          Image(
              painter = rememberAsyncImagePainter(imageUrl),
              contentDescription = "Parking spot image",
              contentScale = ContentScale.FillWidth,
              modifier =
                  Modifier.width(imageMinWidth)
                      .background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.small)
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
            // Show Delete button for the owner
            IconButton(
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                icon = Icons.Outlined.Delete,
                contentDescription = "Delete",
                onClick = { showDeleteDialog.value = true }, // Show confirmation dialog
                inverted = true)
          } else {
            // Show Report button for non-owners
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

  // Delete Confirmation Dialog
  if (showDeleteDialog.value) {
    DeleteConfirmationDialog(
        onConfirm = {
          parkingViewModel.deleteImageFromParking(
              parkingViewModel.selectedParking.value?.uid!!,
              parkingViewModel.selectedImageObject.value?.imagePath!!)
          userViewModel.removeImageFromUserImages(
              parkingViewModel.selectedImageObject.value?.imagePath!!)
          Toast.makeText(context, strResToast, Toast.LENGTH_SHORT).show()
          navigationActions.navigateTo(Screen.LIST)
          showDeleteDialog.value = false
        },
        onDismiss = { showDeleteDialog.value = false },
        showDialog = showDeleteDialog)
  }
}
