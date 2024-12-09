package com.github.se.cyrcle.ui.report

import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.ParkingReport
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.report.Report
import com.github.se.cyrcle.model.report.ReportedObjectType
import com.github.se.cyrcle.model.report.ReportedObjectViewModel
import com.github.se.cyrcle.model.review.ReviewReport
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

@Composable
fun ViewReportsScreen(
    navigationActions: NavigationActions,
    reportedObjectViewModel: ReportedObjectViewModel,
    parkingViewModel: ParkingViewModel,
    reviewViewModel: ReviewViewModel
) {
  val currentObject by reportedObjectViewModel.selectedObject.collectAsState()
  val context = LocalContext.current
  val selType = currentObject?.objectType ?: ReportedObjectType.PARKING
  val successDeleteText = stringResource(R.string.object_deleted)

  Scaffold(
      topBar = {
        TopAppBar(
            navigationActions,
            title =
                if (selType == ReportedObjectType.PARKING) {
                  stringResource(R.string.parking_reports)
                } else {
                  stringResource(R.string.review_reports)
                })
      },
      floatingActionButton = {
        FloatingActionButton(
            onClick = {
              val uidOfObject = currentObject?.objectUID
              if (uidOfObject != null) {
                reportedObjectViewModel.deleteReportedObject(uidOfObject)
                if (selType == ReportedObjectType.PARKING) {
                  parkingViewModel.deleteParkingByUid(uidOfObject)
                } else if (selType == ReportedObjectType.REVIEW){
                  reviewViewModel.deleteReviewById(uidOfObject)
                } else {
                    val uidOfParking = parkingViewModel.getParkingFromImagePath(uidOfObject)
                    Log.d("${uidOfParking}","${uidOfObject}")
                    parkingViewModel.deleteImageFromParking(uidOfParking,uidOfObject)
                }
                Toast.makeText(context, successDeleteText, Toast.LENGTH_LONG).show()
                navigationActions.navigateTo(Screen.ADMIN)
              }
            },
            modifier = Modifier.padding(16.dp).testTag("DeleteFloatingActionButton"),
            containerColor = MaterialTheme.colorScheme.errorContainer) {
              Text(
                  text = stringResource(R.string.delete_object),
                  color = MaterialTheme.colorScheme.onPrimary,
                  style = MaterialTheme.typography.bodyLarge)
            }
      },
      floatingActionButtonPosition = FabPosition.End) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
          val selParkRep by parkingViewModel.selectedParkingReports.collectAsState()
          val selRevRep by reviewViewModel.selectedReviewReports.collectAsState()
          val selImgRep by reviewViewModel.selectedReviewReports.collectAsState()
          val reports =
              when (selType) {
                ReportedObjectType.PARKING -> selParkRep
                ReportedObjectType.REVIEW -> selRevRep
                ReportedObjectType.IMAGE -> selImgRep
              }

          Column(modifier = Modifier.fillMaxSize().padding(16.dp).testTag("ReportsContent")) {
            if (reports.isEmpty()) {
              Text(
                  text = stringResource(R.string.no_reports_available),
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.align(Alignment.CenterHorizontally).testTag("NoReportsText"))
            } else {
              LazyColumn(
                  modifier = Modifier.fillMaxSize().testTag("ReportsList"),
                  contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(reports) { report ->
                      val reportType =
                          when (selType) {
                            ReportedObjectType.PARKING -> Report.Parking(report as ParkingReport)
                            ReportedObjectType.REVIEW -> Report.Review(report as ReviewReport)
                            ReportedObjectType.IMAGE -> Report.Review(report as ReviewReport)
                          }
                      ReportCard(reportType)
                    }
                  }
            }
          }
        }
      }
}

@Composable
fun ReportCard(report: Report) {
  val (reason, userId, description) =
      when (report) {
        is Report.Parking ->
            Triple(
                report.parkingReport.reason.description,
                report.parkingReport.userId,
                report.parkingReport.description)
        is Report.Review ->
            Triple(
                report.reviewReport.reason.description,
                report.reviewReport.userId,
                report.reviewReport.description)
        is Report.Image ->
            Triple(
                report.imageReport.reason.description,
                report.imageReport.userId,
                report.imageReport.description)
      }

  Card(
      modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("ReportCard"),
      elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
          ReportText(label = R.string.admin_reason, value = reason)
          ReportText(label = R.string.admin_reportedBy, value = userId)
          ReportText(label = R.string.admin_description, value = description)
        }
      }
}

@Composable
fun ReportText(@StringRes label: Int, value: String) {
  Text(
      text = stringResource(label).format(value),
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier.padding(top = 4.dp).testTag("ReportText_$label"))
}
