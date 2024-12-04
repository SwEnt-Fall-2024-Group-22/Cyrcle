package com.github.se.cyrcle.ui.report

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.ParkingReport
import com.github.se.cyrcle.model.parking.ParkingReportReason
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.addParking.attributes.DESCRIPTION_MAX_LENGTH
import com.github.se.cyrcle.ui.addParking.attributes.DESCRIPTION_MIN_LENGTH
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.Typography
import com.github.se.cyrcle.ui.theme.atoms.BulletPoint
import com.github.se.cyrcle.ui.theme.atoms.ConditionCheckingInputText
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.disabledColor
import com.github.se.cyrcle.ui.theme.molecules.EnumDropDown
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

const val MAX_CHARACTERS = 256
const val MAX_LINES = 6

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun ParkingReportScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    parkingViewModel: ParkingViewModel,
) {
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp
  val screenHeight = configuration.screenHeightDp.dp
  var showDialog by remember { mutableStateOf(false) }
  val context = LocalContext.current

  val strRes = stringResource(R.string.report_added)

  // Define padding as a percentage of screen dimensions
  val horizontalPaddingScaleFactor = screenWidth * 0.03f
  val topBoxHeight = screenHeight * 0.10f // 10% of screen height for top box
  val verticalPaddingScaleFactor = screenHeight * 0.02f

  val strResToast = stringResource(R.string.report_already)

  // State for report inputs
  val selectedReason = rememberSaveable { mutableStateOf(ParkingReportReason.INEXISTANT) }
  val parkingId = parkingViewModel.selectedParking.value?.uid
  val reportDescription = rememberSaveable { mutableStateOf("") }
  val userId = userViewModel.currentUser.value?.public?.userId!!

  fun onSubmit() {
    val report =
        ParkingReport(
            uid = parkingViewModel.getNewUid(),
            reason = selectedReason.value,
            userId = userId,
            parking = parkingId!!,
            description = reportDescription.value)
    parkingViewModel.addReport(report, userViewModel.currentUser.value!!)
    if (parkingViewModel.hasAlreadyReported.value) {
      Toast.makeText(context, strResToast, Toast.LENGTH_SHORT).show()
    } else {
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
      },
  ) { padding ->
    val scaledPaddingValues =
        PaddingValues(
            horizontal = horizontalPaddingScaleFactor, vertical = verticalPaddingScaleFactor)

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
          Text(
              text = stringResource(R.string.report_title),
              style =
                  MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.Bold, textAlign = TextAlign.Start),
              modifier =
                  Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("ReportTitleText"))

          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(start = 16.dp, bottom = 16.dp)
                      .testTag("ReportBulletPoints"),
              verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BulletPoint(
                    stringResource(R.string.report_bullet_point_1), testTag = "BulletPoint1")
                BulletPoint(
                    stringResource(R.string.report_bullet_point_2_parking),
                    testTag = "BulletPoint2")
                BulletPoint(
                    stringResource(R.string.report_bullet_point_3), testTag = "BulletPoint3")
              }

          // Select Reason
          EnumDropDown(
              options = ParkingReportReason.entries,
              selectedValue = selectedReason,
              label = stringResource(R.string.report_reason),
              modifier = Modifier.testTag("ReasonDropDown"))

          // Additional Details Input
          ConditionCheckingInputText(
              value = reportDescription.value,
              onValueChange = { reportDescription.value = it },
              label = stringResource(R.string.report_details),
              minCharacters = 0,
              maxCharacters = MAX_CHARACTERS,
              maxLines = MAX_LINES,
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(horizontal = horizontalPaddingScaleFactor)
                      .testTag("ReportDetailsInput"))

          val validInputs = areInputsValid(reportDescription.value)

          if (showDialog) {
            ReportScreenAlertDialog(
                onDismiss = { showDialog = false },
                onAccept = {
                  showDialog = false
                  if (validInputs) onSubmit()
                  navigationActions.goBack()
                })
          }

          Button(
              onClick = { showDialog = true },
              modifier =
                  Modifier.testTag("SubmitButton")
                      .padding(top = 16.dp)
                      .align(alignment = Alignment.CenterHorizontally),
              colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                Text(
                    text = stringResource(R.string.attributes_picker_bottom_bar_submit_button),
                    color = if (validInputs) MaterialTheme.colorScheme.primary else disabledColor(),
                    fontWeight = FontWeight.Bold,
                    style = Typography.headlineMedium,
                    textAlign = TextAlign.Center)
              }
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
