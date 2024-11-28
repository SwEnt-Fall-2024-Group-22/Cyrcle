package com.github.se.cyrcle.ui.review

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.review.Review
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.atoms.OptionsMenu
import com.github.se.cyrcle.ui.theme.atoms.ScoreStars
import com.github.se.cyrcle.ui.theme.atoms.SmallFloatingActionButton
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

fun Timestamp?.toFormattedDate(): String {
  return if (this != null) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    dateFormat.format(this.toDate())
  } else {
    "Date not available"
  }
}

enum class ReviewSortingOption {
  Rating,
  DateTime
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
    selectedSortingOption: ReviewSortingOption,
    onSortingOptionSelected: (ReviewSortingOption) -> Unit
) {
  var showFilters by remember { mutableStateOf(false) }

  Column(modifier = Modifier.padding(16.dp)) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically) {
          SmallFloatingActionButton(
              onClick = { showFilters = !showFilters },
              icon = if (showFilters) Icons.Default.Close else Icons.Default.FilterList,
              contentDescription = "Filter",
              testTag = "ShowFiltersButton")
        }

    if (showFilters) {
      FilterSection(
          title = stringResource(R.string.sort_reviews), // "Sort Reviews" title
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
    selectedSortingOption: ReviewSortingOption,
    onOptionSelected: (ReviewSortingOption) -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { onOptionSelected(ReviewSortingOption.Rating) }
                .background(
                    color =
                        if (selectedSortingOption == ReviewSortingOption.Rating)
                            MaterialTheme.colorScheme.secondaryContainer
                        else Color.Transparent,
                    shape = MaterialTheme.shapes.small)
                .padding(12.dp)) {
          Text(
              text = stringResource(R.string.sort_by_rating),
              style = MaterialTheme.typography.bodyMedium,
              color =
                  if (selectedSortingOption == ReviewSortingOption.Rating)
                      MaterialTheme.colorScheme.onSecondaryContainer
                  else MaterialTheme.colorScheme.primary)
        }

    Row(
        modifier =
            Modifier.fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { onOptionSelected(ReviewSortingOption.DateTime) }
                .background(
                    color =
                        if (selectedSortingOption == ReviewSortingOption.DateTime)
                            MaterialTheme.colorScheme.secondaryContainer
                        else Color.Transparent,
                    shape = MaterialTheme.shapes.small)
                .padding(12.dp)) {
          Text(
              text = stringResource(R.string.sort_by_date),
              style = MaterialTheme.typography.bodyMedium,
              color =
                  if (selectedSortingOption == ReviewSortingOption.DateTime)
                      MaterialTheme.colorScheme.onSecondaryContainer
                  else MaterialTheme.colorScheme.primary)
        }
  }
}

@Composable
fun AllReviewsScreen(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel,
    reviewViewModel: ReviewViewModel,
    userViewModel: UserViewModel
) {
  val selectedParking by parkingViewModel.selectedParking.collectAsState()
  val currentUser by userViewModel.currentUser.collectAsState()
  val userSignedIn by userViewModel.isSignedIn.collectAsState(false)
  val reviewsList by reviewViewModel.parkingReviews.collectAsState()
  val ownerHasReviewed = remember { mutableStateOf(false) }
  var selectedCardIndex by remember { mutableIntStateOf(-1) }
  val context = LocalContext.current
  var selectedSortingOption by remember { mutableStateOf(ReviewSortingOption.DateTime) }

  LaunchedEffect(Unit) {
    reviewViewModel.clearReviews()
    selectedParking?.let { reviewViewModel.getReviewsByParking(it.uid) }
  }

  LaunchedEffect(reviewsList) {
    ownerHasReviewed.value =
        reviewsList.any { it.owner == userViewModel.currentUser.value?.public?.userId }
  }

  // Sort reviews based on the selected sorting option
  val sortedReviews =
      remember(reviewsList, selectedSortingOption) {
        when (selectedSortingOption) {
          ReviewSortingOption.DateTime -> reviewsList.sortedByDescending { it.time.nanoseconds }
          ReviewSortingOption.Rating -> reviewsList.sortedByDescending { it.rating }
        }
      }

  Scaffold(
      topBar = {
        TopAppBar(
            navigationActions,
            stringResource(R.string.all_review_title)
                .format(selectedParking?.optName ?: stringResource(R.string.default_parking_name)))
      },
      modifier = Modifier.testTag("AllReviewsScreen")) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
          // Header Section
          FilterHeader(
              selectedSortingOption = selectedSortingOption,
              onSortingOptionSelected = { selectedOption ->
                selectedSortingOption = selectedOption
              })

          // Scrollable Review Cards
          LazyColumn(
              modifier = Modifier.weight(1f).padding(horizontal = 16.dp).testTag("ReviewList"),
              contentPadding = PaddingValues(bottom = 16.dp)) {
                items(items = sortedReviews) { curReview ->
                  val index = sortedReviews.indexOf(curReview)
                  val isExpanded = selectedCardIndex == index
                  val signInToReport = stringResource(R.string.sign_in_to_report_review)
                  ReviewCard(
                      review = curReview,
                      index = index,
                      isExpanded = isExpanded,
                      onCardClick = { selectedCardIndex = if (isExpanded) -1 else index },
                      options =
                          mapOf(
                              stringResource(R.string.all_review_report_review_option) to
                                  {
                                    if (userSignedIn) {
                                      reviewViewModel.selectReview(curReview)
                                      navigationActions.navigateTo(Screen.REVIEW_REPORT)
                                    } else {
                                      Toast.makeText(context, signInToReport, Toast.LENGTH_SHORT)
                                          .show()
                                    }
                                  }),
                      userViewModel = userViewModel)
                }
              }

          // Fixed Bottom Buttons
          if (userSignedIn) {
            val userReview = reviewsList.find { it.owner == currentUser?.public?.userId }
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween) {
                  if (userReview != null) {
                    FloatingActionButton(
                        onClick = {
                          reviewViewModel.deleteReviewById(userReview.uid)
                          parkingViewModel.handleReviewDeletion(oldScore = userReview.rating)
                          Toast.makeText(context, "Review Deleted!", Toast.LENGTH_SHORT).show()
                          navigationActions.goBack()
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier =
                            Modifier.weight(1f)
                                .padding(end = 8.dp)
                                .height(56.dp)
                                .testTag("DeleteReviewButton")) {
                          Text(
                              text = stringResource(R.string.delete_review),
                              color = MaterialTheme.colorScheme.onPrimary,
                              style = MaterialTheme.typography.bodyMedium)
                        }
                  }

                  FloatingActionButton(
                      onClick = { navigationActions.navigateTo(Screen.ADD_REVIEW) },
                      containerColor = MaterialTheme.colorScheme.primary,
                      modifier =
                          Modifier.weight(1f)
                              .padding(start = 8.dp)
                              .height(56.dp)
                              .testTag("AddOrEditReviewButton")) {
                        Text(
                            text =
                                if (ownerHasReviewed.value) stringResource(R.string.edit_review)
                                else stringResource(R.string.add_review),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodyMedium)
                      }
                }
          }
        }
      }
}

@Composable
fun ReviewCard(
    review: Review,
    index: Int,
    isExpanded: Boolean,
    onCardClick: () -> Unit,
    options: Map<String, () -> Unit>,
    userViewModel: UserViewModel
) {
  Card(
      modifier =
          Modifier.fillMaxWidth()
              .padding(8.dp)
              .clickable(onClick = onCardClick)
              .testTag("ReviewCard$index"),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = MaterialTheme.shapes.medium,
      elevation = CardDefaults.cardElevation(4.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
          OptionsMenu(
              options = options,
              modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
              testTag = "MoreOptions$index")

          Column(
              modifier =
                  Modifier.fillMaxWidth().padding(16.dp).testTag("ReviewCardContent$index")) {
                val defaultUsername = stringResource(R.string.undefined_username)
                val ownerUsername = remember { mutableStateOf(defaultUsername) }
                userViewModel.getUserById(
                    review.owner, onSuccess = { ownerUsername.value = it.public.username })

                Text(
                    text = stringResource(R.string.by_text).format(ownerUsername.value),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("ReviewOwner$index"))
                Spacer(modifier = Modifier.height(4.dp))
                ScoreStars(
                    review.rating,
                    scale = 0.6f,
                    starColor = MaterialTheme.colorScheme.primary,
                    text = review.time.toFormattedDate(),
                )
                Spacer(modifier = Modifier.height(4.dp))
                androidx.compose.material3.Text(
                    text = review.text,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Justify,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("ReviewText$index"))
              }
        }
      }
}
