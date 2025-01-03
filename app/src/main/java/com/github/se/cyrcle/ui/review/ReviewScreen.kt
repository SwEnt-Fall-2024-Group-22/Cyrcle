package com.github.se.cyrcle.ui.review

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.review.Review
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.user.PARKING_REVIEW_REWARD
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.atoms.Button
import com.github.se.cyrcle.ui.theme.atoms.ConditionCheckingInputText
import com.github.se.cyrcle.ui.theme.atoms.ScoreStars
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

const val REVIEW_MIN_LENGTH = 0
const val REVIEW_MAX_LENGTH = 256

@Composable
fun ReviewScreen(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel,
    reviewViewModel: ReviewViewModel,
    userViewModel: UserViewModel
) {
  val configuration = LocalConfiguration.current
  val scaleFactor =
      configuration.screenWidthDp / 160f // Adjust the divisor based on your preferred scaling

  val selectedParking =
      parkingViewModel.selectedParking.collectAsState().value
          ?: return Text(text = "No parking selected. Should not happen")

  reviewViewModel.getReviewsByParking(selectedParking.uid)
  val ownerHasReviewed =
      reviewViewModel.parkingReviews.collectAsState().value.any {
        it.owner == userViewModel.currentUser.value?.public?.userId
      }
  val matchingReview =
      reviewViewModel.parkingReviews.collectAsState().value.find {
        it.owner == userViewModel.currentUser.value?.public?.userId
      }
  if (matchingReview != null) {
    reviewViewModel.selectReview(matchingReview)
  }

  var sliderValue by remember {
    mutableFloatStateOf(if (ownerHasReviewed) matchingReview?.rating?.toFloat()!! else 0f)
  }
  var textValue by remember { mutableStateOf(if (ownerHasReviewed) matchingReview?.text!! else "") }

  val context = LocalContext.current
  val reviewAddedText = stringResource(R.string.review_screen_submit_toast)
  val reviewRewardText = stringResource(R.string.review_screen_reward_toast, PARKING_REVIEW_REWARD)
  val reviewUpdateText = stringResource(R.string.review_screen_update_toast)
  val combinedToastText = "$reviewAddedText\n$reviewRewardText"

  Scaffold(
      topBar = {
        TopAppBar(
            navigationActions = navigationActions,
            title =
                if (ownerHasReviewed) stringResource(R.string.review_screen_title_edit_review)
                else stringResource(R.string.review_screen_title_new_review))
      }) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()), // Make the entire content scrollable
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
              ScoreStars(sliderValue.toDouble(), scale = scaleFactor) // Use dynamic scale factor
              Text(
                  text =
                      when (sliderValue) {
                        in 0f..1f -> stringResource(R.string.review_screen_terrible_review)
                        in 1f..2f -> stringResource(R.string.review_screen_bad_review)
                        in 2f..3f -> stringResource(R.string.review_screen_average_review)
                        in 3f..4f -> stringResource(R.string.review_screen_good_review)
                        in 4f..5f -> stringResource(R.string.review_screen_great_review)
                        else -> ""
                      },
                  style = MaterialTheme.typography.bodyLarge,
                  modifier = Modifier.padding(top = 8.dp),
                  testTag = "ExperienceText")

              Slider(
                  value = sliderValue,
                  onValueChange = { newValue -> sliderValue = newValue },
                  valueRange = 0f..5f,
                  steps = 9,
                  modifier = Modifier.padding(16.dp).testTag("Slider"))

              Spacer(modifier = Modifier.height(16.dp))

              ConditionCheckingInputText(
                  label = stringResource(R.string.review_screen_write_review_label),
                  onValueChange = { newText -> textValue = newText },
                  value = textValue,
                  minCharacters = REVIEW_MIN_LENGTH,
                  maxCharacters = REVIEW_MAX_LENGTH,
                  hasClearIcon = true,
                  testTag = "ReviewInput")

              Spacer(modifier = Modifier.height(16.dp))

              Button(
                  text = stringResource(R.string.review_screen_submit_button),
                  onClick = {
                    val sliderToValue = (sliderValue * 100).toInt() / 100.0

                    if (!ownerHasReviewed) {
                      parkingViewModel.handleNewReview(newScore = sliderToValue)
                      reviewViewModel.addReview(
                          Review(
                              owner = userViewModel.currentUser.value?.public?.userId ?: "default",
                              text = textValue,
                              parking = selectedParking.uid,
                              rating = sliderToValue,
                              uid = reviewViewModel.getNewUid(),
                              reportingUsers = emptyList()))

                      if (userViewModel.currentUser.value
                          ?.details
                          ?.reviewedParkings
                          ?.contains(selectedParking.uid) != true) {
                        userViewModel.creditCoinsToCurrentUser(PARKING_REVIEW_REWARD)
                        userViewModel.addReviewedParkingToSelectedUser(selectedParking.uid)
                        Toast.makeText(context, combinedToastText, Toast.LENGTH_LONG).show()
                      } else {
                        Toast.makeText(context, reviewAddedText, Toast.LENGTH_LONG).show()
                      }
                    } else {
                      parkingViewModel.handleReviewUpdate(
                          newScore = sliderToValue, oldScore = matchingReview?.rating!!)
                      reviewViewModel.updateReview(
                          Review(
                              owner = userViewModel.currentUser.value?.public?.userId ?: "default",
                              text = textValue,
                              parking = selectedParking.uid,
                              rating = sliderToValue,
                              uid = reviewViewModel.selectedReview.value?.uid!!,
                              reportingUsers = emptyList()))
                      Toast.makeText(context, reviewUpdateText, Toast.LENGTH_SHORT).show()
                    }
                    navigationActions.goBack()
                  },
                  enabled = textValue.length in REVIEW_MIN_LENGTH..REVIEW_MAX_LENGTH,
                  modifier = Modifier.fillMaxWidth().height(60.dp),
                  colorLevel = ColorLevel.PRIMARY,
                  testTag = "AddReviewButton")
            }
      }
}
