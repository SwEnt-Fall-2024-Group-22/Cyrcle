package com.github.se.cyrcle.ui.report

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.ImageReport
import com.github.se.cyrcle.model.parking.ImageReportReason
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.report.ReportedObjectType
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.molecules.ReportInputs
import com.github.se.cyrcle.ui.theme.molecules.ReportTextBlock
import com.github.se.cyrcle.ui.theme.molecules.SubmitButtonWithDialog
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

const val HORIZONTAL_PADDING = 0.03f
const val VERTICAL_PADDING = 0.02f
const val TOP_BOX_HEIGHT = 0.10f

@Composable
fun ImageReportScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    parkingViewModel: ParkingViewModel
) {
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp
  val screenHeight = configuration.screenHeightDp.dp

  val horizontalPadding = screenWidth * HORIZONTAL_PADDING
  val topBoxHeight = screenHeight * TOP_BOX_HEIGHT
  val verticalPadding = screenHeight * VERTICAL_PADDING

  // State for dialog and inputs
  val showDialog = remember { mutableStateOf(false) }
  val selectedReason = rememberSaveable { mutableStateOf(ImageReportReason.USELESS) }
  val imageId = parkingViewModel.selectedImage.collectAsState().value
  if (imageId == null) {
    Log.e("ImageReportScreen", "No selected image found!")
    return
  }
  Log.d("ImageReportScreen", "Image ID: $imageId")

  val reportDescription = rememberSaveable { mutableStateOf("") }
  val userId = userViewModel.currentUser.value?.public?.userId ?: ""
  if (userId.isEmpty()) {
    Log.e("ImageReportScreen", "User ID is empty!")
    return
  }
  Log.d("ImageReportScreen", "User ID: $userId")

  val context = LocalContext.current

  // Toast messages
  val strResToast = stringResource(R.string.report_already)
  val strResToast2 = stringResource(R.string.report_added)

  fun onSubmit() {
    val report =
        ImageReport(
            uid = parkingViewModel.getNewUid(),
            reason = selectedReason.value,
            userId = userId,
            image = imageId,
            description = reportDescription.value)
    Log.d("ImageReportScreen", "Submitting report: $report")

    if (userViewModel.currentUser.value?.details?.reportedImages?.contains(imageId) == true) {
      Log.d("ImageReportScreen", "Image already reported by the user.")
      Toast.makeText(context, strResToast, Toast.LENGTH_SHORT).show()
    } else {
      Log.d("ImageReportScreen", "Adding image report...")
      parkingViewModel.addImageReport(report, userViewModel.currentUser.value!!)
      userViewModel.addReportedImageToSelectedUser(imageId)
      Toast.makeText(context, strResToast2, Toast.LENGTH_SHORT).show()
    }
    Log.d("ImageReportScreen", "Navigating back after reporting.")
    navigationActions.goBack()
  }

  Scaffold(
      modifier = Modifier.testTag("ImageReportScreen"),
      topBar = {
        TopAppBar(navigationActions, title = stringResource(R.string.report_an_image))
      }) { padding ->
        val scaledPaddingValues =
            PaddingValues(horizontal = horizontalPadding, vertical = verticalPadding)

        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(scaledPaddingValues)
                    .verticalScroll(rememberScrollState())
                    .testTag("ImageReportColumn"),
            horizontalAlignment = Alignment.Start) {
              Box(
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(topBoxHeight)
                          .background(MaterialTheme.colorScheme.background))

              // Text Block Section
              ReportTextBlock(
                  title = stringResource(R.string.report_title),
                  bulletPoints =
                      listOf(
                          stringResource(R.string.report_bullet_point_1),
                          stringResource(R.string.report_bullet_point_2_review),
                          stringResource(R.string.report_bullet_point_3)),
                  modifier = Modifier.testTag("ReportBulletPoints"))

              // Select Reason
              ReportInputs(
                  selectedReasonIfParking = null,
                  selectedReasonIfReview = null,
                  selectedReasonIfImage = selectedReason,
                  reportedObjectType = ReportedObjectType.IMAGE,
                  reportDescription = reportDescription,
                  horizontalPadding = horizontalPadding)

              val validInputs = areInputsValid(reportDescription.value)
              SubmitButtonWithDialog(
                  showDialog = showDialog, validInputs = validInputs, onSubmit = { onSubmit() })
            }
      }
}
