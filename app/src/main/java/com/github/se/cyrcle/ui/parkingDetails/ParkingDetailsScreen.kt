package com.github.se.cyrcle.ui.parkingDetails

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.ParkingReport
import com.github.se.cyrcle.model.parking.ParkingReportReason
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.Black
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.Red
import com.github.se.cyrcle.ui.theme.atoms.Button
import com.github.se.cyrcle.ui.theme.atoms.IconButton
import com.github.se.cyrcle.ui.theme.atoms.ScoreStars
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar
import com.mapbox.maps.extension.style.expressions.dsl.generated.mod

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
  val scrollState = rememberScrollState()
  val context = LocalContext.current
  val toastMessage = stringResource(R.string.sign_in_to_add_favorites)
  // === States for the images ===
  var newParkingImageLocalPath by remember { mutableStateOf("") }
  val showDialog = remember { mutableStateOf(false) }
  val imagesUrls by parkingViewModel.selectedParkingImagesUrls.collectAsState()
  // === === === === === === ===

  LaunchedEffect(Unit) {
    // On first load of the screen, request the images
    // This will update the imagesUrls state and trigger a recomposition
    parkingViewModel.loadSelectedParkingImages()
  }
  // Copied from the editProfileScreen. This is the image picker launcher that set the Uri state
  // with the selected image by the user.
  // It also change the showDialog state to true to show the dialog with the user selected image.
  val imagePickerLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { newParkingImageLocalPath = it.toString() }
        showDialog.value = newParkingImageLocalPath.isNotEmpty()
      }
  // Dialog to confirm the image upload with the user.
  if (showDialog.value) {
    AlertDialog(
        onDismissRequest = {
          showDialog.value = false
          newParkingImageLocalPath = ""
        },
        title = { Text(stringResource(R.string.card_screen_confirm_upload)) },
        text = {
          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .wrapContentSize()
                      .background(MaterialTheme.colorScheme.surfaceBright),
              horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Image(
                painter = rememberAsyncImagePainter(newParkingImageLocalPath),
                contentDescription = stringResource(R.string.view_profile_screen_profile_picture),
                modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Crop)
          }
        },
        confirmButton = {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            // Cancel button (No)
            TextButton(
                onClick = { showDialog.value = false },
                modifier = Modifier.padding(start = 8.dp, end = 8.dp)) {
                  Text(
                      stringResource(R.string.card_screen__cancel_upload),
                      style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp))
                }
            // Confirmation button (Yes)
            TextButton(
                onClick = {
                  showDialog.value = false
                  parkingViewModel.uploadImage(newParkingImageLocalPath, context)
                },
                modifier = Modifier.padding(start = 8.dp, end = 8.dp)) {
                  Text(
                      stringResource(R.string.card_screen__accept_upload),
                      style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp))
                }
          }
        })
  }

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
                    .verticalScroll(scrollState)
                    .testTag("ParkingDetailsColumn")) {
              Box(
                  modifier =
                      Modifier.fillMaxWidth().height(48.dp).testTag("PinAndFavoriteIconContainer"),
                  contentAlignment = Alignment.TopEnd) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      val isPinned =
                          parkingViewModel.pinnedParkings
                              .collectAsState()
                              .value
                              .contains(selectedParking)

                      Icon(
                          imageVector =
                              if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                          contentDescription = "Pin",
                          tint = Black,
                          modifier =
                              Modifier.testTag("PinIcon")
                                  .clickable { parkingViewModel.togglePinStatus(selectedParking) }
                                  .rotate(45f) // Rotate the push pin icon
                          )

                      Spacer(modifier = Modifier.width(8.dp))

                      val isFavorite =
                          userSignedIn.value &&
                              userViewModel.favoriteParkings
                                  .collectAsState()
                                  .value
                                  .contains(selectedParking)

                      Icon(
                          imageVector =
                              if (isFavorite) Icons.Default.Favorite
                              else Icons.Outlined.FavoriteBorder,
                          contentDescription = "Favorite",
                          tint = if (isFavorite) Red else Black,
                          modifier =
                              Modifier.testTag(
                                      if (isFavorite) "RedFilledFavoriteIcon"
                                      else "BlackOutlinedFavoriteIcon")
                                  .clickable {
                                    if (userSignedIn.value) {
                                      if (isFavorite) {
                                        userViewModel.removeFavoriteParkingFromSelectedUser(
                                            selectedParking.uid)
                                        Toast.makeText(
                                                context,
                                                "Removed From Favorites!",
                                                Toast.LENGTH_SHORT)
                                            .show()
                                      } else {
                                        userViewModel.addFavoriteParkingToSelectedUser(
                                            selectedParking.uid)
                                        Toast.makeText(
                                                context, "Added To Favorites!", Toast.LENGTH_SHORT)
                                            .show()
                                      }
                                      userViewModel.getSelectedUserFavoriteParking()
                                    } else {
                                      Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT)
                                          .show()
                                    }
                                  })
                    }
                  }

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
                          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
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
                    // No images
                    if (selectedParking.images.isEmpty()) {
                      Text(
                          text = stringResource(R.string.card_screen_no_image),
                          color = MaterialTheme.colorScheme.onSurface,
                          style = MaterialTheme.typography.bodyMedium,
                          testTag = "NoImageText")
                      IconButton(
                          icon = Icons.Outlined.AddAPhoto,
                          contentDescription = "Add Image",
                          onClick = { imagePickerLauncher.launch("image/*") },
                          enabled = userSignedIn.value,
                          testTag = "AddImageIconButton",
                          modifier = Modifier.padding(start = 8.dp).height(32.dp).fillMaxWidth())
                      // There are images to display
                    } else {
                      LazyRow(
                          modifier = Modifier.fillMaxWidth().testTag("ParkingImagesRow"),
                          horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(imagesUrls) { url ->
                              Image(
                                  painter = rememberAsyncImagePainter(url),
                                  contentDescription =
                                      stringResource(
                                          R.string.view_profile_screen_profile_picture), // FIXME
                                  modifier = Modifier.size(100.dp),
                              )
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
                          Toast.makeText(
                                  context,
                                  "A feature to show the parking on the map will be added later",
                                  Toast.LENGTH_LONG)
                              .show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colorLevel = ColorLevel.PRIMARY,
                        testTag = "ShowInMapButton")

                    if (userViewModel.currentUser.value != null) {
                      Button(
                          text = stringResource(R.string.card_screen_report),
                          onClick = {
                            parkingViewModel.addReport(
                                ParkingReport(
                                    "TEST1",
                                    ParkingReportReason.INEXISTANT,
                                    userViewModel.currentUser.value?.public?.userId ?: "TESTUSER",
                                    selectedParking.uid),
                                userViewModel.currentUser.value!!)
                            Toast.makeText(context, "Report added!", Toast.LENGTH_LONG).show()
                          },
                          modifier = Modifier.fillMaxWidth(),
                          colorLevel = ColorLevel.ERROR,
                          testTag = "ReportButton")
                    }
                  }
            }
      }
}
