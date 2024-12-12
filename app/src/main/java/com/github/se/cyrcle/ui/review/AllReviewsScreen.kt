package com.github.se.cyrcle.ui.review

import android.graphics.Color.parseColor
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
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.github.se.cyrcle.model.user.UserLevelDisplay
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.Black
import com.github.se.cyrcle.ui.theme.atoms.OptionsMenu
import com.github.se.cyrcle.ui.theme.atoms.ScoreStars
import com.github.se.cyrcle.ui.theme.atoms.SmallFloatingActionButton
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

enum class ReviewSortingOption {
  ReviewScore,
  DateTime,
  Interactions,
  Helpful
}

@Composable
fun SortHeader(
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
      Column(
          modifier =
              Modifier.padding(vertical = 8.dp)
                  .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.medium)
                  .background(
                      MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                  .padding(8.dp)) {
            Text(
                text = stringResource(R.string.sort_reviews),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.onSurface,
                testTag = stringResource(R.string.sort_reviews))
            Column(modifier = Modifier.fillMaxWidth()) {
              ReviewSortingOption.entries.forEach { option ->
                SortingOptionItem(
                    option = option,
                    isSelected = selectedSortingOption == option,
                    onOptionSelected = onSortingOptionSelected)
              }
            }
          }
    }
  }
}

@Composable
fun SortingOptionItem(
    option: ReviewSortingOption,
    isSelected: Boolean,
    onOptionSelected: (ReviewSortingOption) -> Unit
) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 4.dp)
              .clickable { onOptionSelected(option) }
              .background(
                  color =
                      if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                      else Color.Transparent,
                  shape = MaterialTheme.shapes.small)
              .padding(12.dp)) {
        Text(
            text = getSortingOptionText(option),
            style = MaterialTheme.typography.bodyMedium,
            color =
                if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.primary)
      }
}

@Composable
fun getSortingOptionText(option: ReviewSortingOption): String {
  return when (option) {
    ReviewSortingOption.ReviewScore -> stringResource(R.string.all_reviews_sort_by_rating)
    ReviewSortingOption.DateTime -> stringResource(R.string.all_reviews_sort_by_date)
    ReviewSortingOption.Interactions -> stringResource(R.string.all_reviews_sort_by_interactions)
    ReviewSortingOption.Helpful -> stringResource(R.string.all_reviews_sort_by_helpful)
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
  val reviewsList by reviewViewModel.parkingReviews.collectAsState()
  LaunchedEffect(Unit) {
    reviewViewModel.clearReviews()
    selectedParking?.let { reviewViewModel.getReviewsByParking(it.uid) }
  }

  val currentUser by userViewModel.currentUser.collectAsState()
  val userSignedIn by userViewModel.isSignedIn.collectAsState(false)
  val currentUserHasReviewed = remember { mutableStateOf(false) }
  LaunchedEffect(reviewsList, currentUser) {
    currentUserHasReviewed.value = reviewsList.any { it.owner == currentUser?.public?.userId }
  }

  // Sort reviews based on the selected sorting option
  var selectedSortingOption by remember { mutableStateOf(ReviewSortingOption.DateTime) }
  val sortedReviews =
      remember(reviewsList, selectedSortingOption) {
        when (selectedSortingOption) {
          ReviewSortingOption.DateTime -> reviewsList.sortedByDescending { it.time.seconds }
          ReviewSortingOption.ReviewScore -> reviewsList.sortedByDescending { it.rating }
          ReviewSortingOption.Interactions ->
              reviewsList.sortedByDescending { it.likedBy.size + it.dislikedBy.size }
          ReviewSortingOption.Helpful ->
              reviewsList.sortedByDescending { it.likedBy.size - it.dislikedBy.size }
        }
      }

  var selectedCardIndex by remember { mutableIntStateOf(-1) }
  val context = LocalContext.current

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
          SortHeader(
              selectedSortingOption = selectedSortingOption,
              onSortingOptionSelected = { selectedOption ->
                selectedSortingOption = selectedOption
              })

          val defaultUsername = stringResource(R.string.undefined_username)
          // Scrollable Review Cards
          LazyColumn(
              modifier = Modifier.weight(1f).testTag("ReviewList"),
              contentPadding = PaddingValues(16.dp)) {
                // Only display "Your Review" section if user is signed in
                if (userSignedIn) {
                  item {
                    Column {
                      Text(
                          text = stringResource(R.string.all_reviews_your_review),
                          style = MaterialTheme.typography.titleMedium,
                          color = MaterialTheme.colorScheme.primary,
                          testTag = "YourReviewTitle")

                      if (currentUserHasReviewed.value) {
                        val userReview =
                            reviewsList.find { it.owner == currentUser?.public?.userId }
                        userReview?.let {
                          ReviewCard(
                              review = it,
                              title = currentUser?.public?.username ?: defaultUsername,
                              index = -1,
                              isExpanded = true,
                              onCardClick = {},
                              options =
                                  mapOf(
                                      // Edit review option
                                      stringResource(R.string.all_reviews_edit_review) to
                                          {
                                            reviewViewModel.selectReview(it)
                                            navigationActions.navigateTo(Screen.ADD_REVIEW)
                                          },
                                      // Delete review option
                                      stringResource(R.string.all_reviews_delete_review) to
                                          {
                                            reviewViewModel.deleteReviewById(it.uid)
                                            parkingViewModel.handleReviewDeletion(
                                                oldScore = it.rating)
                                            navigationActions.goBack()
                                          }),
                              userViewModel = userViewModel,
                              reviewViewModel = reviewViewModel,
                              ownerReputationScore = currentUser?.public?.userReputationScore)
                        }
                      } else {
                        // Add review button
                        FloatingActionButton(
                            onClick = { navigationActions.navigateTo(Screen.ADD_REVIEW) },
                            containerColor = MaterialTheme.colorScheme.primary,
                            modifier =
                                Modifier.fillMaxWidth()
                                    .height(56.dp)
                                    .padding(top = 8.dp)
                                    .testTag("AddReviewButton")) {
                              Text(
                                  text = stringResource(R.string.all_reviews_add_review),
                                  color = MaterialTheme.colorScheme.onPrimary,
                                  style = MaterialTheme.typography.bodyMedium)
                            }
                      }

                      Spacer(modifier = Modifier.height(16.dp))
                    }
                  }
                }

                item {
                  Text(
                      text =
                          if (userSignedIn) stringResource(R.string.all_reviews_other_reviews)
                          else stringResource(R.string.all_reviews_all_reviews),
                      style = MaterialTheme.typography.titleMedium,
                      color = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.padding(top = 16.dp),
                      testTag = "OtherReviewsTitle")
                }
                items(
                    items = sortedReviews.filter { it.owner != currentUser?.public?.userId },
                    key = { it.uid }) { curReview ->
                      val index = sortedReviews.indexOf(curReview)
                      val isExpanded = selectedCardIndex == index
                      val signInToReport = stringResource(R.string.sign_in_to_report_review)

                      var ownerUsername by remember { mutableStateOf(defaultUsername) }
                      var ownerReputationScore by remember { mutableDoubleStateOf(0.0) }

                      userViewModel.getUserById(
                          curReview.owner,
                          onSuccess = {
                            ownerUsername = it.public.username
                            ownerReputationScore = it.public.userReputationScore
                          })

                      ReviewCard(
                          review = curReview,
                          title = ownerUsername,
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
                                          Toast.makeText(
                                                  context, signInToReport, Toast.LENGTH_SHORT)
                                              .show()
                                        }
                                      }),
                          userViewModel = userViewModel,
                          reviewViewModel = reviewViewModel,
                          ownerReputationScore = ownerReputationScore)
                    }
              }
        }
      }
}

@Composable
fun ReviewCard(
    review: Review,
    title: String,
    index: Int,
    isExpanded: Boolean,
    onCardClick: () -> Unit,
    options: Map<String, () -> Unit>,
    userViewModel: UserViewModel,
    reviewViewModel: ReviewViewModel,
    ownerReputationScore: Double?
) {
  val currentUser by userViewModel.currentUser.collectAsState()
  val userSignedIn by userViewModel.isSignedIn.collectAsState(false)

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
          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(horizontal = 16.dp, vertical = 8.dp)
                      .testTag("ReviewCardContent$index")) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                      if (ownerReputationScore != null) {
                        val range = UserLevelDisplay.getLevelRange(ownerReputationScore)
                        val level = ownerReputationScore.toInt()
                        Text(
                            text =
                                stringResource(
                                    R.string.display_user_tag_format, range.symbol, level, title),
                            style = MaterialTheme.typography.bodySmall,
                            color =
                                if (range.color == stringResource(R.string.rainbow_text_color)) {
                                  MaterialTheme.colorScheme.onSurface // TODO color rainbow
                                } else {
                                  Color(parseColor(range.color))
                                },
                            modifier = Modifier.weight(1f).testTag("ReviewTitle$index"),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                      } else {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f).testTag("ReviewTitle$index"),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                      }

                      // Icon buttons for like, dislike, and more options
                      Row(
                          horizontalArrangement = Arrangement.spacedBy(4.dp),
                          modifier = Modifier.testTag("ReviewActions$index")) {
                            val currentUserHasLiked =
                                currentUser?.public?.userId?.let { review.likedBy.contains(it) }
                                    ?: false
                            val currentUserHasDisliked =
                                currentUser?.public?.userId?.let { review.dislikedBy.contains(it) }
                                    ?: false

                            // Like button and count
                            Row(verticalAlignment = Alignment.CenterVertically) {
                              IconButton(
                                  onClick = {
                                    if (userSignedIn) {
                                      currentUser?.public?.let {
                                        reviewViewModel.handleInteraction(review, it.userId, true)
                                      }
                                    }
                                  },
                                  modifier = Modifier.testTag("LikeButton$index"),
                                  enabled = userSignedIn) {
                                    Icon(
                                        imageVector = Icons.Outlined.ThumbUp,
                                        contentDescription = "Like",
                                        tint =
                                            if (currentUserHasLiked)
                                                MaterialTheme.colorScheme.primary
                                            else Black)
                                  }
                              Text(
                                  text = review.likedBy.size.toString(),
                                  style = MaterialTheme.typography.bodySmall,
                                  color =
                                      if (currentUserHasLiked) MaterialTheme.colorScheme.primary
                                      else Black,
                                  testTag = "LikeCount$index")
                            }
                            // Dislike button and count
                            Row(verticalAlignment = Alignment.CenterVertically) {
                              IconButton(
                                  onClick = {
                                    if (userSignedIn) {
                                      reviewViewModel.handleInteraction(
                                          review, currentUser?.public?.userId ?: "", false)
                                    }
                                  },
                                  modifier = Modifier.testTag("DislikeButton$index"),
                                  enabled = userSignedIn) {
                                    Icon(
                                        imageVector = Icons.Outlined.ThumbDown,
                                        contentDescription = "Dislike",
                                        tint =
                                            if (currentUserHasDisliked)
                                                MaterialTheme.colorScheme.primary
                                            else Black)
                                  }
                              Text(
                                  text = review.dislikedBy.size.toString(),
                                  style = MaterialTheme.typography.bodySmall,
                                  color =
                                      if (currentUserHasDisliked) MaterialTheme.colorScheme.primary
                                      else Black,
                                  testTag = "DislikeCount$index")
                            }

                            // More options button
                            if (options.isNotEmpty())
                                OptionsMenu(options = options, testTag = "MoreOptions$index")
                          }
                    }

                Spacer(modifier = Modifier.height(2.dp))
                ScoreStars(
                    review.rating,
                    scale = 0.6f,
                    starColor = MaterialTheme.colorScheme.primary,
                    text = review.time.toFormattedDate(),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
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

/** Converts a [Timestamp] to a formatted date string. */
fun Timestamp?.toFormattedDate(): String {
  return if (this != null) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    dateFormat.format(this.toDate())
  } else {
    "Date not available"
  }
}
