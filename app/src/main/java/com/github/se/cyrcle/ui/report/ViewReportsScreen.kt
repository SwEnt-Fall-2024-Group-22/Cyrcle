package com.github.se.cyrcle.ui.report

import android.annotation.SuppressLint
import android.widget.Toast
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.report.Report
import com.github.se.cyrcle.model.report.ReportedObjectType
import com.github.se.cyrcle.model.report.ReportedObjectViewModel
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun ViewReportsScreen(
    navigationActions: NavigationActions,
    reportedObjectViewModel: ReportedObjectViewModel,
    parkingViewModel: ParkingViewModel,
    reviewViewModel: ReviewViewModel
) {
  val currentObject = reportedObjectViewModel.selectedObject.value
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
                } else {
                  reviewViewModel.deleteReviewById(uidOfObject)
                }
                Toast.makeText(context, successDeleteText, Toast.LENGTH_LONG).show()
                navigationActions.navigateTo(Screen.ADMIN)
              }
            },
            modifier = Modifier.padding(16.dp),
            containerColor = MaterialTheme.colorScheme.errorContainer) {
              Text(
                  text = stringResource(R.string.delete_object),
                  color = MaterialTheme.colorScheme.onPrimary,
                  style = MaterialTheme.typography.bodyLarge)
            }
      },
      floatingActionButtonPosition = FabPosition.End) { paddingValues ->
        // Handle Parking or Review reports rendering
        Box(modifier = Modifier.padding(paddingValues)) {
          if (selType == ReportedObjectType.PARKING) {
            val reports = parkingViewModel.selectedParkingReports.value
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
              if (reports.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_reports_available),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally))
              } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)) {
                      items(reports) { report -> ReportCard(Report.Parking(report)) }
                    }
              }
            }
          } else {
            val reports = reviewViewModel.selectedReviewReports.value
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
              if (reports.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_reports_available),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally))
              } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)) {
                      items(reports) { report -> ReportCard(Report.Review(report)) }
                    }
              }
            }
          }
        }
      }
}

@Composable
fun ReportCard(report: Report) {
  when (report) {
    is Report.Parking -> {
      Card(
          modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
          elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                  text = stringResource(R.string.admin_reason).format(report.parkingReport.reason),
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.padding(top = 4.dp))
              Text(
                  text =
                      stringResource(R.string.admin_reportedBy).format(report.parkingReport.userId),
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.padding(top = 4.dp))
              Text(
                  text =
                      stringResource(R.string.admin_description)
                          .format(report.parkingReport.description),
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.padding(top = 4.dp))
            }
          }
    }
    is Report.Review -> {
      Card(
          modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
          elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                  text = stringResource(R.string.admin_reason).format(report.reviewReport.reason),
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.padding(top = 4.dp))
              Text(
                  text =
                      stringResource(R.string.admin_reportedBy).format(report.reviewReport.userId),
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.padding(top = 4.dp))
              Text(
                  text =
                      stringResource(R.string.admin_description)
                          .format(report.reviewReport.description),
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.padding(top = 4.dp))
            }
          }
    }
  }
}
