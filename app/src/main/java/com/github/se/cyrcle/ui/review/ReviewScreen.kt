package com.github.se.cyrcle.ui.review

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.review.Review
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel,
    reviewViewModel: ReviewViewModel
) {
  var sliderValue by remember { mutableStateOf(0f) }
  var textValue by remember { mutableStateOf("") }

  val context = LocalContext.current // Get the current context

  val selectedParking =
      parkingViewModel.selectedParking.collectAsState().value
          ?: return Text(text = "No parking selected. Should not happen")

  Scaffold(topBar = { TopAppBar(navigationActions = navigationActions, "Add Your Review") }) {
      paddingValues ->
    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
          // Stars reflecting the slider value (supports half stars)
          Row(
              modifier = Modifier.padding(8.dp).testTag("StarsRow"),
              verticalAlignment = Alignment.CenterVertically) {
                for (i in 1..5) {
                  val starIcon =
                      when {
                        i <= sliderValue -> Icons.Filled.Star // Full star
                        i - 0.5f == sliderValue -> Icons.Filled.StarHalf // Half star
                        else -> Icons.Filled.StarBorder // Empty star
                      }
                  Icon(
                      imageVector = starIcon,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(40.dp).testTag("Star$i"))
                }
              }

          // Display the experience text based on the slider value
          Text(
              text =
                  when (sliderValue) {
                    in 0f..1f -> "Terrible experience!"
                    in 1f..2f -> "Bad experience :("
                    in 2f..3f -> "Poor experience :/"
                    3f -> "Average experience..."
                    in 3.5f..4f -> "Good experience!"
                    in 4.5f..5f -> "Great experience!"
                    else -> ""
                  },
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.padding(top = 8.dp).testTag("ExperienceText"))

          // Slider with step granularity of 0.5
          Text(text = "Rating: ${2*(sliderValue.toInt())/2.0}", style = MaterialTheme.typography.bodyLarge)
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
              onClick = {
                Toast.makeText(context, "Review Added!", Toast.LENGTH_SHORT).show()
                // to avoid problematic castings
                val sliderToValue = (sliderValue * 100).toInt() / 100.0
                reviewViewModel.addReview(
                    Review(
                        owner = "default",
                        text = textValue,
                        parking = selectedParking.uid,
                        rating = sliderValue.toDouble(),
                        uid = reviewViewModel.getNewUid()))
                parkingViewModel.updateReviewScore(sliderValue.toDouble(), selectedParking)
                navigationActions.goBack()
              },
              modifier =
                  Modifier.fillMaxWidth()
                      .height(60.dp)
                      .testTag("AddReviewButton"), // Test tag for Add Review button
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.primary,
                      contentColor = MaterialTheme.colorScheme.secondary)) {
                Text(text = "Save my Review")
              }
        }
  }
}
