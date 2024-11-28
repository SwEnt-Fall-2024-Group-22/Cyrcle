package com.github.se.cyrcle.ui.report

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.report.ReportedObjectType
import com.github.se.cyrcle.model.report.ReportedObjectViewModel
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.atoms.SmallFloatingActionButton
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

enum class ReportSortingOption {
  Parking,
  Review
}

@Composable
fun FilterSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
  Column(
      modifier =
          Modifier.padding(8.dp)
              .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.medium)
              .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
              .padding(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.clickable(onClick = onToggle).padding(8.dp).fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface,
            testTag = title)

        if (isExpanded) {
          content()
        }
      }
}

@Composable
fun FilterHeader(
    selectedSortingOption: ReportSortingOption,
    onSortingOptionSelected: (ReportSortingOption) -> Unit
) {
  var showFilters by remember { mutableStateOf(false) }

  Column(modifier = Modifier.padding(16.dp)) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically) {
          Text(
              text =
                  stringResource(
                      R.string.choose_parkingorreview), // "Sort Reviews" or similar title
              modifier = Modifier.weight(1f),
              style = MaterialTheme.typography.headlineMedium,
              color = MaterialTheme.colorScheme.onSurface)

          SmallFloatingActionButton(
              onClick = { showFilters = !showFilters },
              icon = if (showFilters) Icons.Default.Close else Icons.Default.FilterList,
              contentDescription = "Filter",
              testTag = "ShowFiltersButton")
        }

    if (showFilters) {
      FilterSection(
          title = stringResource(R.string.view_reports), // "Sort Reviews" title
          isExpanded = true,
          onToggle = { /* No toggle needed for always-visible sorting options */}) {
            SortingOptionSelector(
                selectedSortingOption = selectedSortingOption,
                onOptionSelected = onSortingOptionSelected)
          }
    }
  }
}

@Composable
fun SortingOptionSelector(
    selectedSortingOption: ReportSortingOption,
    onOptionSelected: (ReportSortingOption) -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { onOptionSelected(ReportSortingOption.Parking) }
                .background(
                    color =
                        if (selectedSortingOption == ReportSortingOption.Parking)
                            MaterialTheme.colorScheme.secondaryContainer
                        else Color.Transparent,
                    shape = MaterialTheme.shapes.small)
                .padding(12.dp)) {
          Text(
              text = stringResource(R.string.sort_reported_parkings),
              style = MaterialTheme.typography.bodyMedium,
              color =
                  if (selectedSortingOption == ReportSortingOption.Parking)
                      MaterialTheme.colorScheme.onSecondaryContainer
                  else MaterialTheme.colorScheme.primary)
        }

    Row(
        modifier =
            Modifier.fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { onOptionSelected(ReportSortingOption.Review) }
                .background(
                    color =
                        if (selectedSortingOption == ReportSortingOption.Review)
                            MaterialTheme.colorScheme.secondaryContainer
                        else Color.Transparent,
                    shape = MaterialTheme.shapes.small)
                .padding(12.dp)) {
          Text(
              text = stringResource(R.string.sort_reported_reviews),
              style = MaterialTheme.typography.bodyMedium,
              color =
                  if (selectedSortingOption == ReportSortingOption.Review)
                      MaterialTheme.colorScheme.onSecondaryContainer
                  else MaterialTheme.colorScheme.primary)
        }
  }
}

@Composable
fun AdminScreen(
    navigationActions: NavigationActions,
    reportedObjectViewModel: ReportedObjectViewModel,
    parkingViewModel: ParkingViewModel,
    reviewViewModel: ReviewViewModel
) {
  reportedObjectViewModel.fetchAllReportedObjects()
  val selectedObject by reportedObjectViewModel.selectedObject.collectAsState()
  val reportsList by reportedObjectViewModel.reportsList.collectAsState()
  var selectedCardIndex by remember { mutableStateOf(-1) }
  val context = LocalContext.current
  var selectedSortingOption by remember { mutableStateOf(ReportSortingOption.Parking) }

  // Filter and sort reports based on the selected sorting option
  val sortedReports =
      remember(reportsList, selectedSortingOption) {
        val filteredReports =
            when (selectedSortingOption) {
              ReportSortingOption.Parking ->
                  reportsList.filter { it.objectType == ReportedObjectType.PARKING }
              ReportSortingOption.Review ->
                  reportsList.filter { it.objectType == ReportedObjectType.REVIEW }
            }
        when (selectedSortingOption) {
          ReportSortingOption.Parking -> filteredReports.sortedByDescending { it.nbOfTimesReported }
          ReportSortingOption.Review -> filteredReports.sortedByDescending { it.nbOfTimesReported }
        }
      }

  Scaffold(
      topBar = {
        TopAppBar(
            navigationActions = navigationActions,
            title =
                stringResource(R.string.admin_topappbar_title).format(selectedSortingOption.name),
        )
      },
      modifier = Modifier.testTag("AdminScreen")) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
          // Header Section
          FilterHeader(
              selectedSortingOption = selectedSortingOption,
              onSortingOptionSelected = { selectedOption ->
                selectedSortingOption = selectedOption
              })

          // Scrollable Review Cards
          LazyColumn(
              modifier = Modifier.weight(1f).padding(horizontal = 16.dp).testTag("ReportList"),
              contentPadding = PaddingValues(bottom = 16.dp)) {
                items(items = sortedReports) { curReport ->
                  val index = sortedReports.indexOf(curReport)
                  val isExpanded = selectedCardIndex == index
                  val cardHeight by
                      animateDpAsState(
                          if (isExpanded) 150.dp
                          else 100.dp) // Adjust card height based on selection
                  val cardColor = MaterialTheme.colorScheme.surfaceContainer

                  Card(
                      modifier =
                          Modifier.fillMaxWidth()
                              .padding(8.dp)
                              .height(cardHeight)
                              .clickable { selectedCardIndex = if (isExpanded) -1 else index }
                              .testTag("ReportCard$index"),
                      colors = CardDefaults.cardColors(containerColor = cardColor),
                      shape = MaterialTheme.shapes.medium,
                      elevation = CardDefaults.cardElevation(8.dp)) {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                          Column(
                              modifier =
                                  Modifier.fillMaxWidth()
                                      .align(Alignment.TopStart)
                                      .testTag("ReportCardContent$index")) {
                                Text(
                                    text =
                                        stringResource(R.string.admin_timesbeenmaxreported)
                                            .format(curReport.nbOfTimesMaxSeverityReported),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.testTag("TimesMaxSeverityReported$index"))
                                Text(
                                    text =
                                        stringResource(R.string.admin_timesbeenreported)
                                            .format(curReport.nbOfTimesReported),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.testTag("TimesReported$index"))

                                // Conditionally show the Report Review button only when the card is
                                // expanded
                                if (isExpanded) {
                                  FloatingActionButton(
                                      onClick = {
                                        reportedObjectViewModel.selectObject(curReport)

                                        // Ensure the selectedObject is not null before proceeding
                                        val currentObject =
                                            reportedObjectViewModel.selectedObject.value
                                        if (currentObject != null) {
                                          if (curReport.objectType == ReportedObjectType.PARKING) {
                                            parkingViewModel.getParkingById(
                                                curReport.objectUID,
                                                onSuccess = { parking ->
                                                  parkingViewModel.selectParking(parking)
                                                  navigationActions.navigateTo(Screen.VIEW_REPORTS)
                                                },
                                                onFailure = {})
                                          } else {
                                            reviewViewModel.getReviewById(
                                                curReport.objectUID,
                                                onSuccess = { review ->
                                                  reviewViewModel.selectReviewAdminScreen(review)
                                                  navigationActions.navigateTo(Screen.VIEW_REPORTS)
                                                },
                                                onFailure = {})
                                          }
                                        } else {
                                          Log.e("AdminScreen", "Failed to set selectedObject")
                                        }
                                      },
                                      containerColor = MaterialTheme.colorScheme.errorContainer,
                                      shape = MaterialTheme.shapes.medium,
                                      modifier = Modifier.testTag("CheckReportsButton$index")) {
                                        Text(
                                            text = stringResource(R.string.check_reports),
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            style = MaterialTheme.typography.bodyMedium)
                                      }
                                }
                              }
                        }
                      }
                }
              }
        }
      }
}
