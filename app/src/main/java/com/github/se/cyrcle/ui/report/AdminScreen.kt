package com.github.se.cyrcle.ui.report

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import coil.compose.rememberImagePainter
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.report.ReportedObjectType
import com.github.se.cyrcle.model.report.ReportedObjectViewModel
import com.github.se.cyrcle.model.review.Review
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.atoms.BulletPoint
import com.github.se.cyrcle.ui.theme.atoms.SmallFloatingActionButton
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

/** Enum class representing the available sorting options for reports. */
enum class ReportSortingOption {
  Parking,
  Review,
  Image
}

/**
 * A reusable composable that represents an expandable section with a title and content.
 *
 * @param title The title of the filter section.
 * @param isExpanded A flag indicating if the section should show its content.
 * @param onToggle A callback to handle expanding or collapsing the section.
 * @param content The composable content to display when the section is expanded.
 */
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

/**
 * Displays details of a reported object based on its type: REVIEW, PARKING, or IMAGE.
 *
 * @param objectType The type of the reported object (REVIEW, PARKING, or IMAGE).
 * @param objectUID The unique identifier of the reported object.
 * @param userUID The unique identifier of the user associated with the report.
 * @param reviewViewModel The ViewModel used to fetch review-related data.
 * @param parkingViewModel The ViewModel used to fetch parking or image data.
 */
@Composable
fun ReportDetailsContent(
    objectType: ReportedObjectType,
    objectUID: String,
    userUID: String,
    reviewViewModel: ReviewViewModel,
    parkingViewModel: ParkingViewModel
) {
  when (objectType) {
    ReportedObjectType.REVIEW -> {
      var review by remember { mutableStateOf<Review?>(null) }
      LaunchedEffect(objectUID) {
        reviewViewModel.getReviewById(objectUID, { result -> review = result }, {})
      }
      review?.let {
        Column(modifier = Modifier.testTag("ReportDetailsContentReview")) {
          BulletPoint(
              text = stringResource(R.string.report_details_review_uid).format(objectUID),
              testTag = "BulletPointReviewUID")
          BulletPoint(
              text = stringResource(id = R.string.report_details_review_parking, it.parking),
              testTag = "BulletPointReviewParking")
          BulletPoint(
              text = stringResource(id = R.string.report_details_review_text, it.text),
              testTag = "BulletPointReviewText")
        }
      }
          ?: Text(
              text = stringResource(R.string.loading_review_details),
              modifier = Modifier.testTag("LoadingReviewDetails"))
    }
    ReportedObjectType.PARKING -> {
      var parking by remember { mutableStateOf<Parking?>(null) }
      LaunchedEffect(objectUID) {
        parkingViewModel.getParkingById(objectUID, { result -> parking = result }, {})
      }
      parking?.let {
        Column(modifier = Modifier.testTag("ReportDetailsContentParking")) {
          BulletPoint(
              text = stringResource(R.string.report_details_parking_uid).format(objectUID),
              testTag = "BulletPointParkingUID")
          BulletPoint(
              text = stringResource(id = R.string.report_details_parking_owner, it.owner),
              testTag = "BulletPointParkingOwner")
        }
      }
          ?: Text(
              text = stringResource(R.string.loading_parking_details),
              modifier = Modifier.testTag("LoadingParkingDetails"))
    }
    ReportedObjectType.IMAGE -> {
      var imageUrl by remember { mutableStateOf<String?>(null) }
      LaunchedEffect(objectUID) {
        parkingViewModel.getImageUrlFromImagePath(objectUID) { url -> imageUrl = url }
      }
      Column(modifier = Modifier.testTag("ReportDetailsContentImage")) {
        Image(
            painter = rememberImagePainter(data = imageUrl),
            contentDescription = stringResource(R.string.reported_image_description),
            modifier = Modifier.fillMaxWidth().height(200.dp).testTag("ImageContent"))
        BulletPoint(
            text = stringResource(R.string.report_details_image_owner).format(userUID),
            testTag = "BulletPointImageOwner")
        BulletPoint(
            text = stringResource(R.string.report_details_image_uid).format(objectUID),
            testTag = "BulletPointImageID")
      }
    }
  }
}

/**
 * Displays a filter header containing a toggleable sorting options menu.
 *
 * @param selectedSortingOption The currently selected sorting option.
 * @param onSortingOptionSelected A callback invoked when a new sorting option is selected.
 */
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
              text = stringResource(R.string.choose_parking_review_or_image),
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
          title = stringResource(R.string.view_reports),
          isExpanded = true,
          onToggle = { /* No toggle needed for always-visible sorting options */}) {
            SortingOptionSelector(
                selectedSortingOption = selectedSortingOption,
                onOptionSelected = onSortingOptionSelected)
          }
    }
  }
}

/**
 * Displays a selectable list of sorting options for reports.
 *
 * @param selectedSortingOption The currently selected sorting option.
 * @param onOptionSelected A callback invoked when a sorting option is selected.
 */
@Composable
fun SortingOptionSelector(
    selectedSortingOption: ReportSortingOption,
    onOptionSelected: (ReportSortingOption) -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
    ReportSortingOption.values().forEach { sortingOption ->
      Row(
          modifier =
              Modifier.fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .clickable { onOptionSelected(sortingOption) }
                  .background(
                      color =
                          if (selectedSortingOption == sortingOption)
                              MaterialTheme.colorScheme.secondaryContainer
                          else Color.Transparent,
                      shape = MaterialTheme.shapes.small)
                  .padding(12.dp)) {
            Text(
                text =
                    stringResource(
                        id =
                            when (sortingOption) {
                              ReportSortingOption.Parking -> R.string.sort_reported_parkings
                              ReportSortingOption.Review -> R.string.sort_reported_reviews
                              ReportSortingOption.Image -> R.string.sort_reported_images
                            }),
                style = MaterialTheme.typography.bodyMedium,
                color =
                    if (selectedSortingOption == sortingOption)
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.primary)
          }
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
  LaunchedEffect(Unit) { reportedObjectViewModel.fetchAllReportedObjects() }
  val selectedObject by reportedObjectViewModel.selectedObject.collectAsState()
  val reportsList by reportedObjectViewModel.reportsList.collectAsState()
  var selectedCardIndex by remember { mutableStateOf(-1) }
  val context = LocalContext.current
  var selectedSortingOption by remember { mutableStateOf(ReportSortingOption.Parking) }
  val moreOptions = stringResource(R.string.more_options)
  val optionsForReport = stringResource(R.string.options_for_report)
  val chooseActionForReport = stringResource(R.string.choose_action_for_report)
  val confirm = stringResource(R.string.Return)
  val cancel = stringResource(R.string.go_to_parking)

  // Filter and sort reports based on the selected sorting option
  val sortedReports =
      remember(reportsList, selectedSortingOption) {
        val filteredReports =
            when (selectedSortingOption) {
              ReportSortingOption.Parking ->
                  reportsList.filter { it.objectType == ReportedObjectType.PARKING }
              ReportSortingOption.Review ->
                  reportsList.filter { it.objectType == ReportedObjectType.REVIEW }
              ReportSortingOption.Image ->
                  reportsList.filter { it.objectType == ReportedObjectType.IMAGE }
            }
        filteredReports.sortedByDescending { it.nbOfTimesReported }
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
                // Inside LazyColumn -> items section
                items(items = sortedReports) { curReport ->
                  val index = sortedReports.indexOf(curReport)
                  val isExpanded = selectedCardIndex == index
                  val cardHeight by animateDpAsState(if (isExpanded) 150.dp else 100.dp)
                  val cardColor = MaterialTheme.colorScheme.surfaceContainer
                  var showDialog by remember { mutableStateOf(false) } // Dialog visibility state

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
                                  IconButton(
                                      onClick = { showDialog = true },
                                      modifier =
                                          Modifier.align(Alignment.End)
                                              .testTag("MoreOptionsButton$index")) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = moreOptions)
                                      }

                                  if (showDialog) {
                                    reportedObjectViewModel.selectObject(curReport)
                                    AlertDialog(
                                        onDismissRequest = { showDialog = false },
                                        confirmButton = {
                                          Text(
                                              text = confirm,
                                              modifier = Modifier.clickable { showDialog = false },
                                              color = MaterialTheme.colorScheme.primary)
                                        },
                                        dismissButton = {
                                          Text(
                                              text = cancel,
                                              modifier =
                                                  Modifier.clickable {
                                                    when (selectedObject?.objectType) {
                                                      ReportedObjectType.PARKING -> {
                                                        parkingViewModel.getParkingById(
                                                            selectedObject?.objectUID!!,
                                                            onSuccess = { parking ->
                                                              parkingViewModel.selectParking(
                                                                  parking)
                                                              navigationActions.navigateTo(
                                                                  Screen.PARKING_DETAILS)
                                                            },
                                                            onFailure = { error ->
                                                              // Handle failure for PARKING
                                                              // retrieval
                                                              Log.e(
                                                                  "ParkingError",
                                                                  "Failed to get parking: ${error.message}")
                                                            })
                                                      }
                                                      ReportedObjectType.IMAGE -> {
                                                        val parkID =
                                                            parkingViewModel
                                                                .getParkingFromImagePath(
                                                                    selectedObject?.objectUID!!)
                                                        parkingViewModel.getParkingById(
                                                            parkID,
                                                            onSuccess = { parking ->
                                                              parkingViewModel.selectParking(
                                                                  parking)
                                                              navigationActions.navigateTo(
                                                                  Screen.PARKING_DETAILS)
                                                            },
                                                            onFailure = { error ->
                                                              // Handle failure for IMAGE-based
                                                              // parking retrieval
                                                              Log.e(
                                                                  "ImageError",
                                                                  "Failed to get parking from image: ${error.message}")
                                                            })
                                                      }
                                                      else -> {
                                                        reviewViewModel.getReviewById(
                                                            selectedObject?.objectUID!!,
                                                            onSuccess = { review ->
                                                              parkingViewModel.getParkingById(
                                                                  review.parking,
                                                                  onSuccess = { parking ->
                                                                    parkingViewModel.selectParking(
                                                                        parking)
                                                                    navigationActions.navigateTo(
                                                                        Screen.PARKING_DETAILS)
                                                                  },
                                                                  onFailure = { error ->
                                                                    // Handle failure for parking
                                                                    // retrieval from review
                                                                    Log.e(
                                                                        "ParkingFromReviewError",
                                                                        "Failed to get parking: ${error.message}")
                                                                  })
                                                            },
                                                            onFailure = { error ->
                                                              // Handle failure for review retrieval
                                                              Log.e(
                                                                  "ReviewError",
                                                                  "Failed to get review: ${error.message}")
                                                            })
                                                      }
                                                    }
                                                  },
                                              color = MaterialTheme.colorScheme.secondary)
                                        },
                                        title = {
                                          Text(
                                              text = optionsForReport,
                                              style = MaterialTheme.typography.titleMedium)
                                        },
                                        text = {
                                          val selectedObject =
                                              reportedObjectViewModel.selectedObject
                                                  .collectAsState()
                                                  .value
                                          Column {
                                            if (selectedObject != null) {
                                              ReportDetailsContent(
                                                  selectedObject.objectType,
                                                  selectedObject.objectUID,
                                                  selectedObject.userUID,
                                                  reviewViewModel,
                                                  parkingViewModel)
                                            }
                                          }
                                        })
                                  }

                                  FloatingActionButton(
                                      onClick = {
                                        reportedObjectViewModel.selectObject(curReport)
                                        val currentObject =
                                            reportedObjectViewModel.selectedObject.value
                                        if (currentObject != null) {
                                          when (curReport.objectType) {
                                            ReportedObjectType.PARKING -> {
                                              parkingViewModel.getParkingById(
                                                  curReport.objectUID,
                                                  onSuccess = { parking ->
                                                    parkingViewModel.selectParking(parking)
                                                    navigationActions.navigateTo(
                                                        Screen.VIEW_REPORTS)
                                                  },
                                                  onFailure = {})
                                            }
                                            ReportedObjectType.REVIEW -> {
                                              reviewViewModel.getReviewById(
                                                  curReport.objectUID,
                                                  onSuccess = { review ->
                                                    reviewViewModel.selectReviewAdminScreen(review)
                                                    navigationActions.navigateTo(
                                                        Screen.VIEW_REPORTS)
                                                  },
                                                  onFailure = {})
                                            }
                                            ReportedObjectType.IMAGE -> {
                                              val parkingUID =
                                                  parkingViewModel.getParkingFromImagePath(
                                                      curReport.objectUID)
                                              parkingViewModel.getParkingById(
                                                  parkingUID,
                                                  { parking ->
                                                    parkingViewModel.selectParking(parking)
                                                    parkingViewModel.selectImage(
                                                        curReport.objectUID)
                                                    navigationActions.navigateTo(
                                                        Screen.VIEW_REPORTS)
                                                  },
                                                  {})
                                            }
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
