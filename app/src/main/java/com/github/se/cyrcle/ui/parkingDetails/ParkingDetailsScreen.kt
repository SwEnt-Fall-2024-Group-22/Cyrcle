package com.github.se.cyrcle.ui.parkingDetails

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.user.MAX_NOTE_LENGTH
import com.github.se.cyrcle.model.user.UserLevelDisplay
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.profile.UserTagText
import com.github.se.cyrcle.ui.theme.Black
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.Red
import com.github.se.cyrcle.ui.theme.atoms.Button
import com.github.se.cyrcle.ui.theme.atoms.ConditionCheckingInputText
import com.github.se.cyrcle.ui.theme.atoms.IconButton
import com.github.se.cyrcle.ui.theme.atoms.ScoreStars
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

@Composable
fun ParkingDetailsScreen(
    mapViewModel: MapViewModel,
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel,
    userViewModel: UserViewModel
) {
  val selectedParking =
      parkingViewModel.selectedParking.collectAsState().value
          ?: return Text(stringResource(R.string.no_selected_parking_error))
  val userSignedIn by userViewModel.isSignedIn.collectAsState(false)
  val context = LocalContext.current

  // === States for the images ===
  var newParkingImageLocalPath by remember { mutableStateOf("") }
  val showDialog = remember { mutableStateOf(false) }
  val imagesUrls by parkingViewModel.selectedParkingImagesUrls.collectAsState()
  val imagesPaths by parkingViewModel.selectedParkingAssociatedPaths.collectAsState()
  val showDialogImage = remember { mutableStateOf<String?>(null) }
  val showDialogImageDestinationPath = remember { mutableStateOf<String?>("") }

  val displayOnlineElement by userViewModel.displayOnlineElementFlow.collectAsState(false)
  val defaultUsername = stringResource(R.string.undefined_username)
  var ownerReputationScore by remember { mutableDoubleStateOf(0.0) }
  var ownerUsername by remember { mutableStateOf(defaultUsername) }
  if (selectedParking.owner != "Unknown Owner" && selectedParking.owner != null) {
    userViewModel.selectSelectedParkingUser(
        parkingViewModel.selectedParking.collectAsState().value?.owner!!)
    userViewModel.getUserById(
        selectedParking.owner,
        onSuccess = {
          ownerReputationScore = it.public.userReputationScore
          ownerUsername = it.public.username
        })
  }
  val range = UserLevelDisplay.getLevelRange(ownerReputationScore)
  val level = ownerReputationScore.toInt()

  // === === === === === === ===

  LaunchedEffect(Unit, selectedParking) {
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
    ParkingDetailsAlertDialogConfirmUpload(
        onDismiss = {
          showDialog.value = false
          newParkingImageLocalPath = ""
        },
        newParkingImageLocalPath = newParkingImageLocalPath,
        onAccept = {
          showDialog.value = false
          parkingViewModel.uploadImage(newParkingImageLocalPath, context) {}
        })
  }

  if (showDialogImage.value != null) {
    parkingViewModel.selectImage(showDialogImageDestinationPath.value!!)
    ParkingDetailsAlertDialogShowImage(
        parkingViewModel,
        userViewModel,
        onDismiss = {
          showDialogImage.value = null
          showDialogImageDestinationPath.value = ""
        },
        imageUrl = showDialogImage.value!!,
        navigationActions)
  }

  Scaffold(
      topBar = {
        TopAppBar(
            navigationActions,
            stringResource(R.string.card_screen_description)
                .format(selectedParking.optName ?: stringResource(R.string.default_parking_name)))
      },
      modifier = Modifier.testTag("ParkingDetailsScreen")) { padding ->
        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
                    .padding(padding)
                    .padding(32.dp)
                    .testTag("ParkingDetailsColumn")) {
              item {
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("TopInteractionRow"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                      val parkingNote =
                          userViewModel.currentUser
                              .collectAsState()
                              .value
                              ?.details
                              ?.personalNotes
                              ?.get(selectedParking.uid)

                      val editingNote = remember { mutableStateOf(false) }
                      val editingNoteText = remember { mutableStateOf(parkingNote ?: "") }

                      // Column to handle text wrapping
                      Column(
                          modifier =
                              Modifier.weight(1f) // Allow this column to take available space
                                  .padding(end = 16.dp) // Space for icons
                          ) {
                            if (editingNote.value) {
                              ConditionCheckingInputText(
                                  value = editingNoteText.value,
                                  onValueChange = { editingNoteText.value = it },
                                  label = stringResource(R.string.list_screen_edit_note),
                                  minCharacters = 0,
                                  maxCharacters = MAX_NOTE_LENGTH,
                                  maxLines = 2,
                                  testTag = "NoteInputText")
                            } else {
                              if (userSignedIn) {
                                Text(
                                    text =
                                        parkingNote ?: stringResource(R.string.list_screen_no_note),
                                    style =
                                        MaterialTheme.typography.bodyLarge.copy(
                                            fontStyle =
                                                if (parkingNote.isNullOrBlank()) FontStyle.Italic
                                                else FontStyle.Normal),
                                    textAlign = TextAlign.Left,
                                    modifier = Modifier.testTag("NoteText"))
                              }
                            }
                          }

                      // Icons Row
                      Row(
                          modifier = Modifier.testTag("IconsRow"),
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Save or Add/Edit Note Icon
                            if (editingNote.value) {
                              Icon(
                                  imageVector = Icons.Default.Check,
                                  contentDescription = "Save Note",
                                  tint = Black,
                                  modifier =
                                      Modifier.clickable {
                                            if (editingNoteText.value.length <= MAX_NOTE_LENGTH) {
                                              userViewModel.editCurrentUserPersonalNoteForParking(
                                                  selectedParking, editingNoteText.value)
                                              editingNote.value = false
                                            }
                                          }
                                          .testTag("SaveNoteIcon"))
                            } else {
                              // Add icon if no note, Edit icon if note exists
                              if (parkingNote == null) {
                                val toastMsgNote = stringResource(R.string.sign_in_to_add_note)
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Note",
                                    tint = Black,
                                    modifier =
                                        Modifier.clickable {
                                              if (userSignedIn) editingNote.value = true
                                              else
                                                  Toast.makeText(
                                                          context, toastMsgNote, Toast.LENGTH_SHORT)
                                                      .show()
                                            }
                                            .testTag("AddNoteIcon"))
                              } else {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Note",
                                    tint = Black,
                                    modifier =
                                        Modifier.clickable { editingNote.value = true }
                                            .testTag("EditNoteIcon"))
                              }
                            }

                            // Pin Icon
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
                                    Modifier.clickable {
                                          parkingViewModel.togglePinStatus(selectedParking)
                                        }
                                        .rotate(45f)
                                        .testTag("PinIcon"))

                            // Favorite Icon
                            val isFavorite =
                                userSignedIn &&
                                    userViewModel.favoriteParkings
                                        .collectAsState()
                                        .value
                                        .contains(selectedParking)
                            val toastMsgFavorite = stringResource(R.string.sign_in_to_add_favorites)
                            Icon(
                                imageVector =
                                    if (isFavorite) Icons.Default.Favorite
                                    else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Red else Black,
                                modifier =
                                    Modifier.clickable {
                                          if (userSignedIn) {
                                            if (isFavorite) {
                                              userViewModel.removeFavoriteParkingFromSelectedUser(
                                                  selectedParking)
                                            } else {
                                              userViewModel.addFavoriteParkingToSelectedUser(
                                                  selectedParking)
                                            }
                                          } else {
                                            Toast.makeText(
                                                    context, toastMsgFavorite, Toast.LENGTH_SHORT)
                                                .show()
                                          }
                                        }
                                        .testTag(
                                            if (isFavorite) "RedFilledFavoriteIcon"
                                            else "BlackOutlinedFavoriteIcon"))
                          }
                    }
                // Reviews
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("AverageRatingRow"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                      if (selectedParking.nbReviews > 0) {
                        Row {
                          ScoreStars(
                              selectedParking.avgScore,
                              scale = 0.8f,
                              text =
                                  pluralStringResource(
                                          R.plurals.reviews_count,
                                          count = selectedParking.nbReviews)
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
                              Modifier.clickable {
                                if (displayOnlineElement)
                                    navigationActions.navigateTo(Screen.ALL_REVIEWS)
                                else
                                    Toast.makeText(
                                            context,
                                            R.string.card_screen_offline_info,
                                            Toast.LENGTH_SHORT)
                                        .show()
                              })
                    }

                Spacer(modifier = Modifier.height(16.dp))

                // Images
                Row(
                    modifier = Modifier.height(150.dp).fillMaxWidth().testTag("ImagesRow"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                      // No images
                      if (selectedParking.images.isEmpty()) {
                        Text(
                            text = stringResource(R.string.card_screen_no_image),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(3f),
                            textAlign = TextAlign.Left,
                            testTag = "NoImageText")
                        // There are images to display
                      } else {
                        if (imagesUrls.isEmpty()) {
                          Text(stringResource(R.string.card_screen_no_image))
                        } else {
                          LazyRow(
                              modifier =
                                  Modifier.weight(2f).fillMaxHeight().testTag("ParkingImagesRow"),
                              horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                itemsIndexed(
                                    items = imagesUrls,
                                    key = { index, _ ->
                                      imagesPaths[index]
                                    } // Use associated paths as stable keys
                                    ) { index, url ->
                                      Image(
                                          painter = rememberAsyncImagePainter(url),
                                          contentDescription = "Parking Image",
                                          contentScale = ContentScale.Crop,
                                          modifier =
                                              Modifier.fillMaxHeight().width(150.dp).clickable {
                                                // Set both imageUrl and destinationPath in the
                                                // state
                                                showDialogImage.value = url
                                                showDialogImageDestinationPath.value =
                                                    showDialogImageDestinationPath.value.plus(
                                                        imagesPaths[index])
                                              })
                                    }
                              }
                        }
                      }
                      IconButton(
                          icon = Icons.Outlined.AddAPhoto,
                          contentDescription = "Add Image",
                          onClick = { imagePickerLauncher.launch("image/*") },
                          enabled = userSignedIn,
                          testTag = "AddImageIconButton",
                          modifier =
                              Modifier.padding(start = 8.dp)
                                  .height(32.dp)
                                  .weight(1f)
                                  .testTag("addPhotoButton"))
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
                              Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.card_screen_rack_type),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground)
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "Info",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier =
                                        Modifier.padding(horizontal = 4.dp)
                                            .size(16.dp)
                                            .testTag("RackInfoIcon")
                                            .clickable {
                                              navigationActions.navigateTo(Screen.RACK_INFO)
                                            })
                              }
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
                            Column(modifier = Modifier.weight(1f).testTag("UserColumn")) {
                              Text(
                                  text = stringResource(R.string.card_screen_user),
                                  style = MaterialTheme.typography.bodyMedium,
                                  color = MaterialTheme.colorScheme.onBackground)
                              UserTagText(
                                  ownerUsername = ownerUsername,
                                  symbol = range.symbol,
                                  color = range.color,
                                  level = level,
                                  modifier =
                                      if (range.color ==
                                          stringResource(R.string.rainbow_text_color)) {
                                        Modifier.testTag("RainbowUserTag")
                                      } else {
                                        Modifier.testTag("UserTag")
                                      },
                                  style = MaterialTheme.typography.bodyMedium)
                            }
                          }
                    }

                Column(
                    modifier = Modifier.fillMaxWidth().testTag("ButtonsColumn"),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                      Button(
                          text = stringResource(R.string.card_screen_show_map),
                          onClick = {
                            parkingViewModel.selectParking(selectedParking)
                            mapViewModel.updateTrackingMode(false)
                            mapViewModel.updateMapRecentering(true)
                            mapViewModel.zoomOnLocation(selectedParking.location)
                            navigationActions.navigateTo(Screen.MAP)
                          },
                          modifier = Modifier.fillMaxWidth(),
                          colorLevel = ColorLevel.PRIMARY,
                          testTag = "ShowInMapButton")

                      if (userViewModel.currentUser.collectAsState().value != null) {
                        Button(
                            text = stringResource(R.string.card_screen_report),
                            onClick = { navigationActions.navigateTo(Screen.PARKING_REPORT) },
                            modifier = Modifier.fillMaxWidth(),
                            colorLevel = ColorLevel.ERROR,
                            testTag = "ReportButton")
                      }
                    }
              }
            }
      }
}
