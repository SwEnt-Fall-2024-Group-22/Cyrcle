package com.github.se.cyrcle.ui.parkingDetails

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.atoms.Button
import com.github.se.cyrcle.ui.theme.atoms.IconButton
import com.github.se.cyrcle.ui.theme.atoms.ScoreStars
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun ParkingDetailsScreen(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel,
    userViewModel: UserViewModel
) {
  val selectedParking =
      parkingViewModel.selectedParking.collectAsState().value
          ?: return Text(stringResource(R.string.no_selected_parking_error))
  val userSignedIn = userViewModel.isSignedIn.collectAsState(false)

  val context = LocalContext.current

  Scaffold(
      topBar = {
        TopAppBar(
            navigationActions,
            stringResource(R.string.card_screen_description)
                .format(selectedParking.optName ?: stringResource(R.string.default_parking_name)))
      },
      modifier = Modifier.testTag("ParkingDetailsScreen")) { padding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(padding)
                    .padding(32.dp)
                    .verticalScroll(rememberScrollState())
                    .testTag("ParkingDetailsColumn")) {
              // Reviews
              Row(
                  modifier =
                      Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("AverageRatingRow"),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween) {
                    if (selectedParking.nbReviews > 0) {
                      Row {
                        ScoreStars(
                            selectedParking.avgScore,
                            scale = 0.8f,
                            text =
                                pluralStringResource(
                                        R.plurals.reviews_count, count = selectedParking.nbReviews)
                                    .format(selectedParking.nbReviews))
                      }
                    } else {
                      Text(
                          text = stringResource(R.string.no_reviews),
                          style = MaterialTheme.typography.bodyMedium,
                          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                          testTag = "ParkingNoReviews")
                    }
                    Text(
                        text = stringResource(R.string.card_screen_see_review),
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = TextDecoration.Underline),
                        color = MaterialTheme.colorScheme.primary,
                        testTag = "SeeAllReviewsText",
                        modifier =
                            Modifier.clickable { navigationActions.navigateTo(Screen.ALL_REVIEWS) })
                  }

              Spacer(modifier = Modifier.height(16.dp))
              // Images
              Row(
                  modifier = Modifier.fillMaxWidth().testTag("ImagesRow"),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically) {
                    if (selectedParking.images.isEmpty()) {
                      Text(
                          text = stringResource(R.string.card_screen_no_image),
                          color = MaterialTheme.colorScheme.onSurface,
                          style = MaterialTheme.typography.bodyMedium,
                          testTag = "NoImageText")
                      IconButton(
                          icon = Icons.Outlined.AddAPhoto,
                          contentDescription = "Add Image",
                          onClick = {
                            // Temporary toast. Will be replaced by an image picker
                            Toast.makeText(
                                    context,
                                    "A feature to add images will be added later",
                                    Toast.LENGTH_LONG)
                                .show()
                          },
                          enabled = userSignedIn.value,
                          testTag = "AddImageIconButton",
                          modifier = Modifier.padding(start = 8.dp).height(32.dp).fillMaxWidth())
                    } else {
                      LazyRow(
                          modifier = Modifier.fillMaxWidth().testTag("ParkingImagesRow"),
                          horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(selectedParking.images.size) { index ->
                              AsyncImage(
                                  model = selectedParking.images[index],
                                  contentDescription = "Image $index",
                                  modifier =
                                      Modifier.size(170.dp)
                                          .padding(4.dp)
                                          .testTag("ParkingImage$index"),
                                  contentScale = ContentScale.Crop)
                            }
                          }
                    }
                  }

              // Information
              Column(
                  modifier =
                      Modifier.fillMaxWidth().padding(vertical = 32.dp).testTag("InfoColumn"),
                  verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().testTag("RowCapacityRack"),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                          Column(modifier = Modifier.weight(1f).testTag("CapacityColumn")) {
                            Text(
                                text = stringResource(R.string.card_screen_capacity),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground)
                            Text(
                                text = selectedParking.capacity.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface)
                          }
                          Column(modifier = Modifier.weight(1f).testTag("RackTypeColumn")) {
                            Text(
                                text = stringResource(R.string.card_screen_rack_type),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground)
                            Text(
                                text = selectedParking.rackType.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface)
                          }
                        }
                    Row(
                        modifier = Modifier.fillMaxWidth().testTag("RowProtectionPrice"),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                          Column(modifier = Modifier.weight(1f).testTag("ProtectionColumn")) {
                            Text(
                                text = stringResource(R.string.card_screen_protection),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground)
                            Text(
                                text = selectedParking.protection.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface)
                          }
                          Column(modifier = Modifier.weight(1f).testTag("PriceColumn")) {
                            Text(
                                text = stringResource(R.string.card_screen_price),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground)
                            val price = selectedParking.price
                            Text(
                                text =
                                    if (price == 0.0) stringResource(R.string.free) else "$price",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface)
                          }
                        }

                    Row(
                        modifier = Modifier.fillMaxWidth().testTag("RowSecurity"),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                          Column(modifier = Modifier.weight(1f).testTag("SecurityColumn")) {
                            Text(
                                text = stringResource(R.string.card_screen_surveillance),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground)
                            Text(
                                text =
                                    if (selectedParking.hasSecurity) stringResource(R.string.yes)
                                    else stringResource(R.string.no),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface)
                          }
                        }
                  }

              Column(
                  modifier = Modifier.fillMaxWidth().testTag("ButtonsColumn"),
                  verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        text = stringResource(R.string.card_screen_show_map),
                        onClick = {
                          // Temporary toast. Will redirect to the map screen later
                          Toast.makeText(
                                  context,
                                  "A feature to show the parking on the map will be added later",
                                  Toast.LENGTH_LONG)
                              .show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colorLevel = ColorLevel.PRIMARY,
                        testTag = "ShowInMapButton")

                    Button(
                        text = stringResource(R.string.card_screen_report),
                        onClick = {
                          // Temporary toast. Will be replaced by a report system
                          Toast.makeText(
                                  context,
                                  "A report system will be added to the app later",
                                  Toast.LENGTH_LONG)
                              .show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colorLevel = ColorLevel.ERROR,
                        testTag = "ReportButton")
                  }
            }
      }
}
