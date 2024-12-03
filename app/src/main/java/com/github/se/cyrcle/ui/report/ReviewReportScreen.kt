package com.github.se.cyrcle.ui.report

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.sp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.review.ReviewReport
import com.github.se.cyrcle.model.review.ReviewReportReason
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.atoms.BulletPoint
import com.github.se.cyrcle.ui.theme.atoms.ConditionCheckingInputText
import com.github.se.cyrcle.ui.theme.disabledColor
import com.github.se.cyrcle.ui.theme.molecules.EnumDropDown
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun ReviewReportScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    reviewViewModel: ReviewViewModel,
) {
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp
  val screenHeight = configuration.screenHeightDp.dp
  val showDialog = remember { mutableStateOf(false) }

  // Define padding as a percentage of screen dimensions
  val horizontalPaddingScaleFactor = screenWidth * 0.03f
  val topBoxHeight = screenHeight * 0.10f // 10% of screen height for top box
  val verticalPaddingScaleFactor = screenHeight * 0.02f

  // State for report inputs
  val selectedReason = rememberSaveable { mutableStateOf(ReviewReportReason.IRRELEVANT) }
  val reviewId = reviewViewModel.selectedReview.value?.uid
  val reportDescription = rememberSaveable { mutableStateOf("") }
  val userId = userViewModel.currentUser.value?.public?.userId!!
  val context = LocalContext.current

  val strResToast = stringResource(R.string.report_added)
  fun onSubmit() {
    val report =
        ReviewReport(
            uid = reviewViewModel.getNewUid(),
            reason = selectedReason.value,
            userId = userId,
            review = reviewId!!,
            description = reportDescription.value)
    reviewViewModel.addReport(report, userViewModel.currentUser.value!!)
    if(reviewViewModel.hasAlreadyReported.value){
        Toast.makeText(context, "You Have Already Reported This Review", Toast.LENGTH_SHORT).show()
        navigationActions.goBack()
    }
    Toast.makeText(context, strResToast, Toast.LENGTH_SHORT).show()
    navigationActions.goBack()
  }


  Scaffold(
      modifier = Modifier.testTag("ReviewReportScreen"),
      topBar = {
        TopAppBar(
            navigationActions,
            title =
                stringResource(R.string.report_a_review)
                    .format(reviewViewModel.selectedReview.value?.text?.take(30) ?: reviewId))
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
                .testTag("ReviewReportColumn"),
        horizontalAlignment = Alignment.Start) {
          Box(
              modifier =
                  Modifier.fillMaxWidth()
                      .height(topBoxHeight)
                      .background(MaterialTheme.colorScheme.background))

          // Text Block Section
          Text(
              text = stringResource(R.string.report_title),
              style =
                  MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.Bold, textAlign = TextAlign.Start),
              modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("ReportTitle"))

          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(start = 16.dp, bottom = 16.dp) // Indent bullet points
                      .testTag("ReportBulletPoints"),
              verticalArrangement = Arrangement.spacedBy(8.dp) // Add space between points
              ) {
                BulletPoint(
                    stringResource(R.string.report_bullet_point_1), testTag = "BulletPoint1")
                BulletPoint(
                    stringResource(R.string.report_bullet_point_2_review), testTag = "BulletPoint2")
                BulletPoint(
                    stringResource(R.string.report_bullet_point_3), testTag = "BulletPoint3")
              }

          // Select Reason
          EnumDropDown(
              options = ReviewReportReason.entries,
              selectedValue = selectedReason,
              label = stringResource(R.string.report_reason),
              modifier = Modifier.testTag("ReasonDropdown"))

          // Additional Details Input
          ConditionCheckingInputText(
              value = reportDescription.value,
              onValueChange = { reportDescription.value = it },
              label = stringResource(R.string.report_details),
              minCharacters = 0,
              maxCharacters = 256,
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(horizontal = horizontalPaddingScaleFactor)
                      .testTag("DetailsInput"))

          val validInputs = areInputsValid(reportDescription.value)

          if (showDialog.value) {
            ReportScreenAlertDialog(
                onDismiss = { showDialog.value = false },
                onAccept = {
                  showDialog.value = false
                  if (validInputs) onSubmit()
                  navigationActions.goBack()
                })
          }

          Button(
              onClick = { showDialog.value = true },
              modifier =
                  Modifier.testTag("SubmitButton")
                      .padding(top = 16.dp)
                      .align(alignment = Alignment.CenterHorizontally),
              colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                Text(
                    text = stringResource(R.string.attributes_picker_bottom_bar_submit_button),
                    color = if (validInputs) MaterialTheme.colorScheme.primary else disabledColor(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center)
              }
        }
  }
}
