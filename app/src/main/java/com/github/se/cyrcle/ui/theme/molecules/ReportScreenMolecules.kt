package com.github.se.cyrcle.ui.theme.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.report.ReportScreenAlertDialog
import com.github.se.cyrcle.ui.theme.atoms.BulletPoint
import com.github.se.cyrcle.ui.theme.atoms.ConditionCheckingInputText
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.disabledColor

@Composable
fun ReportTopAppBar(navigationActions: NavigationActions, title: String) {
    TopAppBar(navigationActions, title = title)
}

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

@Composable
fun ReportInputs(
    selectedReasonIfParking: MutableState<ParkingReportReason>? = null,
    selectedReasonIfReview: MutableState<ReviewReportReason>? = null,
    selectedReasonIfImage: MutableState<ImageReportReason>? = null,
    reportedObjectType: ReportedObjectType,
    reportDescription: MutableState<String>,
    horizontalPadding: Dp,
    modifier: Modifier = Modifier
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
