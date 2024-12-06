package com.github.se.cyrcle.ui.report

import android.annotation.SuppressLint
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
import com.github.se.cyrcle.model.report.ReportedObjectType
import com.github.se.cyrcle.model.review.ReviewReport
import com.github.se.cyrcle.model.review.ReviewReportReason
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.molecules.ReportInputs
import com.github.se.cyrcle.ui.theme.molecules.ReportTextBlock
import com.github.se.cyrcle.ui.theme.molecules.ReportTopAppBar
import com.github.se.cyrcle.ui.theme.molecules.SubmitButtonWithDialog

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
  val horizontalPadding = screenWidth * 0.03f
  val topBoxHeight = screenHeight * 0.10f
  val verticalPadding = screenHeight * 0.02f

  // State for dialog and inputs
  val showDialog = remember { mutableStateOf(false) }
  val selectedReason = rememberSaveable { mutableStateOf(ReviewReportReason.IRRELEVANT) }
  val reviewId = reviewViewModel.selectedReview.value?.uid
  val reportDescription = rememberSaveable { mutableStateOf("") }
  val userId = userViewModel.currentUser.value?.public?.userId!!
  val context = LocalContext.current

  // Toast messages
  val strResToast = stringResource(R.string.report_already)
  val strResToast2 = stringResource(R.string.report_added)

  fun onSubmit() {
    val report =
        ReviewReport(
            uid = reviewViewModel.getNewUid(),
            reason = selectedReason.value,
            userId = userId,
            review = reviewId!!,
            description = reportDescription.value)
    reviewViewModel.addReport(report, userViewModel.currentUser.value!!)
    if (reviewViewModel.hasAlreadyReported.value) {
      Toast.makeText(context, strResToast, Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(context, strResToast2, Toast.LENGTH_SHORT).show()
    }
    navigationActions.goBack()
  }

  Scaffold(
      modifier = Modifier.testTag("ReviewReportScreen"),
      topBar = {
        ReportTopAppBar(
            navigationActions,
            title =
                stringResource(R.string.report_a_review)
                    .format(reviewViewModel.selectedReview.value?.text?.take(30) ?: reviewId))
      }) { padding ->
        val scaledPaddingValues =
            PaddingValues(horizontal = horizontalPadding, vertical = verticalPadding)

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
              ReportTextBlock(
                  title = stringResource(R.string.report_title),
                  bulletPoints =
                      listOf(
                          stringResource(R.string.report_bullet_point_1),
                          stringResource(R.string.report_bullet_point_2_review),
                          stringResource(R.string.report_bullet_point_3)),
                  modifier = Modifier.testTag("ReportBulletPoints"))

              // Select Reason
              // Select Reason
              ReportInputs(
                  selectedReasonIfParking = null,
                  selectedReasonIfReview = selectedReason,
                  selectedReasonIfImage = null,
                  reportedObjectType = ReportedObjectType.REVIEW,
                  reportDescription = reportDescription,
                  horizontalPadding = horizontalPadding)

              val validInputs = areInputsValid(reportDescription.value)

              // Submit Button with Dialog
              SubmitButtonWithDialog(
                  showDialog = showDialog, validInputs = validInputs, onSubmit = { onSubmit() })
            }
      }
}
