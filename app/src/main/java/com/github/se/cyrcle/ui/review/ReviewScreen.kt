package com.github.se.cyrcle.ui.review

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.review.Review
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.atoms.Button
import com.github.se.cyrcle.ui.theme.atoms.ScoreStars
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun ReviewScreen(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel,
    reviewViewModel: ReviewViewModel,
    userViewModel: UserViewModel
) {

  val selectedParking =
      parkingViewModel.selectedParking.collectAsState().value
          ?: return Text(text = "No parking selected. Should not happen")

  reviewViewModel.getReviewsByParking(selectedParking.uid)
  val ownerHasReviewed =
      reviewViewModel.parkingReviews.value.any {
        it.owner == userViewModel.currentUser?.value?.userId
      }
  val matchingReview =
      reviewViewModel.parkingReviews.value.find {
        it.owner == userViewModel.currentUser?.value?.userId
      }
  if (matchingReview != null) {
    reviewViewModel.selectReview(matchingReview)
  }

  var sliderValue by remember {
    mutableStateOf(if (ownerHasReviewed) matchingReview?.rating?.toFloat()!! else 0f)
  }
  var textValue by remember { mutableStateOf(if (ownerHasReviewed) matchingReview?.text!! else "") }

  val context = LocalContext.current // Get the current context

  Scaffold(
      topBar = {
        TopAppBar(
            navigationActions = navigationActions,
            if (ownerHasReviewed) "Edit Your Review" else "Add Your Review")
      }) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
              ScoreStars(sliderValue.toDouble(), scale = 2.5f)
              // Display the experience text based on the slider value
              Text(
                  text =
                      when (sliderValue) {
                        in 1f..2f -> stringResource(R.string.review_screen_poor_review)
                        in 2f..3f -> stringResource(R.string.review_screen_bad_review)
                        3f -> stringResource(R.string.review_screen_average_review)
                        in 3.5f..4f -> stringResource(R.string.review_screen_good_review)
                        in 4.5f..5f -> stringResource(R.string.review_screen_great_review)
                        else -> ""
                      },
                  style = MaterialTheme.typography.bodyLarge,
                  modifier = Modifier.padding(top = 8.dp),
                  testTag = "ExperienceText")

              // Slider with step granularity of 0.5
              Text(
                  text = "Rating: ${sliderValue.toDouble()}",
                  style = MaterialTheme.typography.bodyLarge)
              Slider(
                  value = sliderValue,
                  onValueChange = { newValue -> sliderValue = newValue },
                  valueRange = 0f..5f,
                  steps = 9, // Steps for granularity of 0.5 between 0 and 5
                  modifier = Modifier.padding(16.dp).testTag("Slider"))

              Spacer(modifier = Modifier.height(16.dp))

              // OutlinedTextField for text input
              OutlinedTextField(
                  value = textValue,
                  onValueChange = { newText -> textValue = newText },
                  label = { Text("Write your review") },
                  modifier = Modifier.fillMaxWidth().testTag("ReviewInput"))

              Spacer(modifier = Modifier.height(16.dp))

              // Add Review Button
              Button(
                  text = "Save my Review",
                  onClick = {
                    Toast.makeText(context, "Review Added!", Toast.LENGTH_SHORT).show()
                    // to avoid problematic castings
                    val sliderToValue = (sliderValue * 100).toInt() / 100.0

                    if (!ownerHasReviewed) {
                      parkingViewModel.updateReviewScore(
                          newScore = sliderToValue, parking = selectedParking, isNewReview = true)
                      reviewViewModel.addReview(
                          Review(
                              owner = userViewModel.currentUser.value?.userId ?: "default",
                              text = textValue,
                              parking = selectedParking.uid,
                              rating = sliderToValue,
                              uid = reviewViewModel.getNewUid()))
                    } else {
                      parkingViewModel.updateReviewScore(
                          newScore = sliderToValue,
                          oldScore = matchingReview?.rating!!,
                          parking = selectedParking,
                          isNewReview = false)

                      reviewViewModel.updateReview(
                          Review(
                              owner = userViewModel.currentUser.value?.userId ?: "default",
                              text = textValue,
                              parking = selectedParking.uid,
                              rating = sliderToValue,
                              uid = reviewViewModel.selectedReview.value?.uid!!))
                    }
                    navigationActions.goBack()
                  },
                  modifier = Modifier.fillMaxWidth().height(60.dp),
                  colorLevel = ColorLevel.PRIMARY,
                  testTag = "AddReviewButton")
            }
      }
}
