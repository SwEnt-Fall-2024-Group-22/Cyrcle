package com.github.se.cyrcle.ui.report

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.ParkingReport
import com.github.se.cyrcle.model.parking.ParkingReportReason
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.addParking.attributes.DESCRIPTION_MAX_LENGTH
import com.github.se.cyrcle.ui.addParking.attributes.DESCRIPTION_MIN_LENGTH
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.Typography
import com.github.se.cyrcle.ui.theme.atoms.ConditionCheckingInputText
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.disabledColor
import com.github.se.cyrcle.ui.theme.molecules.EnumDropDown

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
    var showDialog = remember { mutableStateOf(false) }

    // Define padding as a percentage of screen dimensions
    val horizontalPaddingScaleFactor = screenWidth * 0.03f
    val topBoxHeight = screenHeight * 0.10f // 10% of screen height for top box
    val verticalPaddingScaleFactor = screenHeight * 0.02f

    // State for report inputs
    val selectedReason = rememberSaveable { mutableStateOf<ParkingReportReason>(ParkingReportReason.INEXISTANT) }
    val parkingId = parkingViewModel.selectedParking.value?.uid
    val reportDescription = rememberSaveable { mutableStateOf<String>("") }
    val userId = userViewModel.currentUser.value?.public?.userId

    fun onSubmit() {
        if (selectedReason.value != null) {
            val report = ParkingReport(
                uid = parkingViewModel.getNewUid(),
                reason = selectedReason.value!!,
                userId = if (userId != null) userId else "",
                parking = parkingId!!,
                description = reportDescription.value
            )
            parkingViewModel.addReport(report, userViewModel.currentUser.value!!)
            navigationActions.goBack()
        }
    }

    Scaffold(
        modifier = Modifier.testTag("ParkingReportScreen"),
        topBar = {
            TopAppBar(
                navigationActions,
                title = "Report Parking: ${parkingViewModel.selectedParking.value?.optName ?: parkingId}"
            )
        },
    ) { padding ->
        val scaledPaddingValues = PaddingValues(
            horizontal = horizontalPaddingScaleFactor,
            vertical = verticalPaddingScaleFactor
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaledPaddingValues)
                .verticalScroll(rememberScrollState())
                .testTag("ParkingReportColumn"),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topBoxHeight)
                    .background(MaterialTheme.colorScheme.background)
            )

            // Text Block Section
            Text(
                text = "The following will be submitted in your report:",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 16.dp), // Indent bullet points
                verticalArrangement = Arrangement.spacedBy(8.dp) // Add space between points
            ) {
                BulletPoint("Your User Information")
                BulletPoint("This Parking's information")
                BulletPoint( "Reason + Description below")
            }

            // Select Reason
            EnumDropDown(
                options = ParkingReportReason.entries,
                selectedValue = selectedReason,
                label = "Reason for Reporting"
            )

            // Additional Details Input
            ConditionCheckingInputText(
                value = reportDescription.value,
                onValueChange = { reportDescription.value = it },
                label = "Additional Details (Optional)",
                minCharacters = 0,
                maxCharacters = 256,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPaddingScaleFactor)
            )

            val validInputs = areInputsValid(reportDescription.value)

            if (showDialog.value) {
                ReportScreenAlertDialog(onDismiss = {
                    showDialog.value = false
                }, onAccept = {
                    showDialog.value = false
                    if (validInputs) onSubmit()
                    navigationActions.goBack()
                })
            }

            Button(
                onClick = { showDialog.value = true },
                modifier = Modifier
                    .testTag("submitButton")
                    .padding(top = 16.dp).align(alignment = Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text(
                    text = stringResource(R.string.attributes_picker_bottom_bar_submit_button),
                    color = if (validInputs) MaterialTheme.colorScheme.primary else disabledColor(),
                    fontWeight = FontWeight.Bold,
                    style = Typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun areInputsValid(description: String): Boolean {
    return description.length in DESCRIPTION_MIN_LENGTH..DESCRIPTION_MAX_LENGTH
}