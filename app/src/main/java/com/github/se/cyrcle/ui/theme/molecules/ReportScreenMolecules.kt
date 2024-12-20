package com.github.se.cyrcle.ui.theme.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.ImageReportReason
import com.github.se.cyrcle.model.parking.ParkingReportReason
import com.github.se.cyrcle.model.report.ReportedObjectType
import com.github.se.cyrcle.model.review.ReviewReportReason
import com.github.se.cyrcle.ui.report.ReportScreenAlertDialog
import com.github.se.cyrcle.ui.theme.atoms.BulletPoint
import com.github.se.cyrcle.ui.theme.atoms.ConditionCheckingInputText
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.disabledColor

/**
 * Displays a formatted text block with a title and a list of bullet points.
 *
 * This composable is useful for presenting structured information in a clear and concise format. It
 * ensures consistent styling for the title and bullet points and provides spacing for better
 * readability.
 *
 * @param title The title of the text block, displayed prominently at the top.
 * @param bulletPoints A list of strings representing the bullet points to be displayed under the
 *   title.
 * @param modifier A [Modifier] to customize the appearance and layout of the text block.
 */
@Composable
fun ReportTextBlock(title: String, bulletPoints: List<String>, modifier: Modifier = Modifier) {
  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
        text = title,
        style =
            MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Start),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
    Column(
        modifier = Modifier.padding(start = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
          bulletPoints.forEachIndexed { index, point ->
            BulletPoint(point, testTag = "BulletPoint$index")
          }
        }
  }
}

/**
 * Renders input fields for reporting various object types, including a dropdown for selecting the
 * reason and a description input field.
 *
 * This composable dynamically adjusts its input fields based on the type of the object being
 * reported (e.g., parking, review, image). It ensures consistent styling and validation for the
 * report details input field.
 *
 * @param selectedReasonIfParking A mutable state holding the selected reason if the object type is
 *   parking.
 * @param selectedReasonIfReview A mutable state holding the selected reason if the object type is a
 *   review.
 * @param selectedReasonIfImage A mutable state holding the selected reason if the object type is an
 *   image.
 * @param reportedObjectType The type of the object being reported, determining which dropdown is
 *   displayed.
 * @param reportDescription A mutable state holding the description provided by the user.
 * @param horizontalPadding Padding applied to the input fields for consistent alignment.
 */
@Composable
fun ReportInputs(
    selectedReasonIfParking: MutableState<ParkingReportReason>?,
    selectedReasonIfReview: MutableState<ReviewReportReason>?,
    selectedReasonIfImage: MutableState<ImageReportReason>?,
    reportedObjectType: ReportedObjectType,
    reportDescription: MutableState<String>,
    horizontalPadding: Dp,
) {
  when (reportedObjectType) {
    ReportedObjectType.PARKING -> {
      EnumDropDown(
          options = ParkingReportReason.entries,
          selectedValue = selectedReasonIfParking!!,
          label = stringResource(R.string.report_reason),
          modifier = Modifier.testTag("ReasonDropdown"))
    }
    ReportedObjectType.REVIEW -> {
      EnumDropDown(
          options = ReviewReportReason.entries,
          selectedValue = selectedReasonIfReview!!,
          label = stringResource(R.string.report_reason),
          modifier = Modifier.testTag("ReasonDropdown"))
    }
    ReportedObjectType.IMAGE -> {
      EnumDropDown(
          options = ImageReportReason.entries,
          selectedValue = selectedReasonIfImage!!,
          label = stringResource(R.string.report_reason),
          modifier = Modifier.testTag("ReasonDropdown"))
    }
  }
  ConditionCheckingInputText(
      value = reportDescription.value,
      onValueChange = { reportDescription.value = it },
      label = stringResource(R.string.report_details),
      minCharacters = 0,
      maxCharacters = 256,
      modifier =
          Modifier.fillMaxWidth().padding(horizontal = horizontalPadding).testTag("DetailsInput"))
}

/**
 * Displays a submit button with a confirmation dialog.
 *
 * When the button is clicked, a confirmation dialog is displayed. If the user confirms, the
 * provided onSubmit callback is executed. The button is styled to visually indicate whether the
 * inputs are valid or not.
 *
 * @param showDialog A mutable state controlling the visibility of the confirmation dialog.
 * @param validInputs A boolean indicating whether the form inputs are valid. This affects the
 *   button's visual state.
 * @param onSubmit A callback function to be executed when the user confirms the submission.
 */
@Composable
fun SubmitButtonWithDialog(
    showDialog: MutableState<Boolean>,
    validInputs: Boolean,
    onSubmit: () -> Unit
) {
  if (showDialog.value) {
    ReportScreenAlertDialog(
        onDismiss = { showDialog.value = false },
        onAccept = {
          showDialog.value = false
          if (validInputs) onSubmit()
        })
  }
  Box(
      modifier = Modifier.fillMaxWidth() // Ensures alignment works within this BoxScope
      ) {
        Button(
            onClick = { showDialog.value = true },
            modifier = Modifier.padding(top = 16.dp).align(Alignment.Center),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
              Text(
                  text = stringResource(R.string.attributes_picker_bottom_bar_submit_button),
                  color = if (validInputs) MaterialTheme.colorScheme.primary else disabledColor(),
                  textAlign = TextAlign.Center)
            }
      }
}

/**
 * A reusable alert dialog to confirm deletion.
 *
 * @param onConfirm The function to call when the user confirms the deletion.
 * @param onDismiss The function to call when the dialog is dismissed.
 * @param showDialog A mutable state controlling the visibility of the dialog.
 */
@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    showDialog: MutableState<Boolean>
) {
  if (showDialog.value) {
    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        confirmButton = {
          TextButton(
              onClick = {
                showDialog.value = false
                onConfirm()
              }) {
                Text(stringResource(id = R.string.delete_dialog_confirm_button))
              }
        },
        dismissButton = {
          TextButton(
              onClick = {
                showDialog.value = false
                onDismiss()
              }) {
                Text(stringResource(id = R.string.delete_dialog_cancel_button))
              }
        },
        title = { Text(stringResource(id = R.string.delete_dialog_title)) },
        text = { Text(stringResource(id = R.string.delete_dialog_message)) })
  }
}
