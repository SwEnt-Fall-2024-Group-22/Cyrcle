package com.github.se.cyrcle.ui.report

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
import com.github.se.cyrcle.model.parking.ParkingReport
import com.github.se.cyrcle.model.parking.ParkingReportReason
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.report.ReportedObjectType
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.addParking.attributes.DESCRIPTION_MAX_LENGTH
import com.github.se.cyrcle.ui.addParking.attributes.DESCRIPTION_MIN_LENGTH
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.molecules.ReportInputs
import com.github.se.cyrcle.ui.theme.molecules.ReportTextBlock
import com.github.se.cyrcle.ui.theme.molecules.SubmitButtonWithDialog
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

@Composable
fun ParkingReportScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    parkingViewModel: ParkingViewModel,
) {
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp
  val screenHeight = configuration.screenHeightDp.dp
  val horizontalPadding = screenWidth * HORIZONTAL_PADDING
  val topBoxHeight = screenHeight * TOP_BOX_HEIGHT
  val verticalPadding = screenHeight * VERTICAL_PADDING

  val showDialog = remember { mutableStateOf(false) }
  val context = LocalContext.current

  val strRes = stringResource(R.string.report_added)
  val strResToast = stringResource(R.string.report_already)

  // State for report inputs
  val selectedReason = rememberSaveable { mutableStateOf(ParkingReportReason.INEXISTANT) }
  val parkingId = parkingViewModel.selectedParking.collectAsState().value?.uid
  val reportDescription = rememberSaveable { mutableStateOf("") }
  val userId = userViewModel.currentUser.collectAsState().value?.public?.userId!!

  fun onSubmit() {
    val report =
        ParkingReport(
            uid = parkingViewModel.getNewUid(),
            reason = selectedReason.value,
            userId = userId,
            parking = parkingId!!,
            description = reportDescription.value)

    if (userViewModel.currentUser.value!!.details?.reportedParkings?.contains(parkingId) == true) {
      Toast.makeText(context, strResToast, Toast.LENGTH_SHORT).show()
    } else {
      parkingViewModel.addReport(report)
      userViewModel.addReportedParkingToSelectedUser(parkingId)
      Toast.makeText(context, strRes, Toast.LENGTH_SHORT).show()
    }
    navigationActions.goBack()
  }

  Scaffold(
      modifier = Modifier.testTag("ParkingReportScreen"),
      topBar = {
        TopAppBar(
            navigationActions,
            title =
                stringResource(R.string.report_parking)
                    .format(parkingViewModel.selectedParking.value?.optName ?: parkingId))
      }) { padding ->
        val scaledPaddingValues =
            PaddingValues(horizontal = horizontalPadding, vertical = verticalPadding)

        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(scaledPaddingValues)
                    .verticalScroll(rememberScrollState())
                    .testTag("ParkingReportColumn"),
            horizontalAlignment = Alignment.Start) {
              Box(
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(topBoxHeight)
                          .background(MaterialTheme.colorScheme.background)
                          .testTag("TopBoxPadding"))

              // Text Block Section
              ReportTextBlock(
                  title = stringResource(R.string.report_title),
                  bulletPoints =
                      listOf(
                          stringResource(R.string.report_bullet_point_1),
                          stringResource(R.string.report_bullet_point_2_parking),
                          stringResource(R.string.report_bullet_point_3)),
                  modifier = Modifier.testTag("ReportBulletPoints"))

              // Select Reason
              ReportInputs(
                  selectedReasonIfParking = selectedReason,
                  selectedReasonIfReview = null,
                  selectedReasonIfImage = null,
                  reportedObjectType = ReportedObjectType.PARKING,
                  reportDescription = reportDescription,
                  horizontalPadding = horizontalPadding)

              val validInputs = areInputsValid(reportDescription.value)

              // Submit Button with Dialog
              SubmitButtonWithDialog(
                  showDialog = showDialog, validInputs = validInputs, onSubmit = { onSubmit() })
            }
      }
}
/**
 * Checks if the description
 *
 * @param description element to check the size of
 * @return true if it respects size requirements
 */
fun areInputsValid(description: String): Boolean {
  return description.length in DESCRIPTION_MIN_LENGTH..DESCRIPTION_MAX_LENGTH
}
