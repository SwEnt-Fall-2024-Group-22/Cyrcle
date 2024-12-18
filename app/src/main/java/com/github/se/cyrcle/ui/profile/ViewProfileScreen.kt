package com.github.se.cyrcle.ui.profile

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Outbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.navigation.TopLevelDestinations
import com.github.se.cyrcle.ui.review.ReviewCard
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.atoms.Button
import com.github.se.cyrcle.ui.theme.atoms.IconButton
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.BottomNavigationBar
import com.github.se.cyrcle.ui.theme.molecules.DeleteConfirmationDialog
import kotlinx.coroutines.launch

@Composable
fun ViewProfileScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    parkingViewModel: ParkingViewModel,
    reviewViewModel: ReviewViewModel
) {

  val context = LocalContext.current
  val userState by userViewModel.currentUser.collectAsState()
  var isEditing by remember { mutableStateOf(false) }
  var signOut by remember { mutableStateOf(false) }
  val signOutToastText = stringResource(R.string.view_profile_on_sign_out_toast)

  LaunchedEffect(Unit, userState) { userViewModel.loadSelectedUserImages() }

  Scaffold(
      modifier = Modifier.testTag("ViewProfileScreen"),
      bottomBar = {
        BottomNavigationBar(
            navigationActions = navigationActions,
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.VIEW_PROFILE)
      }) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
          IconButton(
              modifier = Modifier.padding(10.dp).align(Alignment.TopEnd),
              icon = Icons.Filled.Outbox,
              contentDescription = "Sign Out",
              testTag = "SignOutButton",
              onClick = { signOut = true })

          if (userViewModel.currentUser.collectAsState().value?.details?.isAdmin == true) {
            IconButton(
                modifier = Modifier.padding(10.dp).align(Alignment.TopEnd).padding(80.dp),
                icon = Icons.Filled.AddModerator,
                contentDescription = "Admin",
                testTag = "AdminButton",
                onClick = { navigationActions.navigateTo(Screen.ADMIN) })
          }

          IconButton(
              modifier = Modifier.padding(10.dp).align(Alignment.TopStart),
              icon =
                  Icons.Filled.Diamond, // or Icons.Filled.Paid or Icons.Filled.AttachMoney etc...
              contentDescription = "Go to Gambling Screen",
              testTag = "GamblingButton",
              onClick = { navigationActions.navigateTo(Screen.GAMBLING) })

          if (signOut) {
            AlertDialog(
                modifier = Modifier.testTag("SignOutDialog"),
                onDismissRequest = {},
                title = {
                  Text(stringResource(R.string.view_profile_screen_sign_out_dialog_title))
                },
                text = {
                  Text(stringResource(R.string.view_profile_screen_sign_out_dialog_message))
                },
                confirmButton = {
                  TextButton(
                      modifier = Modifier.testTag("SignOutDialogConfirmButton"),
                      onClick = {
                        userViewModel.signOut {
                          (context as Activity).runOnUiThread {
                            Toast.makeText(context, signOutToastText, Toast.LENGTH_SHORT).show()
                          }
                          navigationActions.navigateTo(TopLevelDestinations.AUTH)
                        }
                      }) {
                        Text(
                            stringResource(
                                R.string.view_profile_screen_sign_out_dialog_action_button))
                      }
                },
                dismissButton = {
                  TextButton(
                      modifier = Modifier.testTag("SignOutDialogCancelButton"),
                      onClick = { signOut = false }) {
                        Text(
                            stringResource(
                                R.string.view_profile_screen_sign_out_dialog_cancel_button))
                      }
                })
          }

          if (isEditing) {
            EditProfileComponent(
                user = userState,
                saveButton = { editedUser: User, validInputs: Boolean ->
                  Button(
                      text = stringResource(R.string.view_profile_screen_save_button),
                      onClick = {
                        userViewModel.updateUser(editedUser, context)
                        isEditing = false
                      },
                      colorLevel = ColorLevel.PRIMARY,
                      enabled = validInputs,
                      testTag = "SaveButton")
                },
                cancelButton = {
                  Button(
                      text = stringResource(R.string.view_profile_screen_cancel_button),
                      onClick = { isEditing = false },
                      colorLevel = ColorLevel.SECONDARY,
                      testTag = "CancelButton")
                })
          } else {
            DisplayProfileComponent(userState) {
              // Edit Button directly in the Box
              Button(
                  text = stringResource(R.string.view_profile_screen_modify_profile_button),
                  onClick = { isEditing = true },
                  colorLevel = ColorLevel.TERTIARY,
                  testTag = "EditButton")

              val userReviews by reviewViewModel.userReviews.collectAsState()
              // Map to store the parking corresponding to each review. This is used to display the
              // parking
              // name in the review card and allow to navigate to the parking's details screen
              val reviewToParkingMap = remember { mutableStateMapOf<String, Parking>() }
              LaunchedEffect(userReviews) {
                userReviews.forEach { review ->
                  parkingViewModel.getParkingById(
                      review.parking, { parking -> reviewToParkingMap[review.uid] = parking }, {})
                }
              }

              // Display the TabLayout
              TabLayout(
                  userViewModel = userViewModel,
                  parkingViewModel = parkingViewModel,
                  reviewViewModel = reviewViewModel,
                  navigationActions = navigationActions,
                  reviewToParkingMap = reviewToParkingMap)
            }
          }
        }
      }
}

/**
 * Tab layout for the profile screen. This layout contains the following tabs:
 * - Favorite Parkings
 * - My Reviews
 *
 * This layout allows a tab to be selected by either clicking on the tab itself or by swiping
 * horizontally.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TabLayout(
    userViewModel: UserViewModel,
    parkingViewModel: ParkingViewModel,
    reviewViewModel: ReviewViewModel,
    navigationActions: NavigationActions,
    reviewToParkingMap: Map<String, Parking>
) {
  val tabs =
      listOf(
          stringResource(R.string.view_profile_screen_favorite_parkings),
          stringResource(R.string.view_profile_screen_my_reviews),
          stringResource(R.string.view_profile_screen_my_images))

  val pagerState = rememberPagerState(pageCount = { tabs.size })
  val coroutineScope = rememberCoroutineScope()

  Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
    TabRow(selectedTabIndex = pagerState.currentPage, modifier = Modifier.testTag("TabRow")) {
      tabs.forEachIndexed { index, title ->
        Tab(
            text = { Text(title) },
            selected = pagerState.currentPage == index,
            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
            modifier = Modifier.testTag("Tab${title.replace(" ", "")}"))
      }
    }

    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
      when (page) {
        0 -> FavoriteParkingsSection(userViewModel, parkingViewModel, navigationActions)
        1 ->
            UserReviewsSection(
                reviewViewModel,
                userViewModel,
                parkingViewModel,
                navigationActions,
                reviewToParkingMap)
        2 -> UserImagesSection(userViewModel, parkingViewModel, navigationActions)
      }
    }
  }
}

@Composable
private fun FavoriteParkingsSection(
    userViewModel: UserViewModel,
    parkingViewModel: ParkingViewModel,
    navigationActions: NavigationActions
) {
  val favoriteParkings = userViewModel.favoriteParkings.collectAsState().value

  Box(modifier = Modifier.fillMaxSize()) {
    if (favoriteParkings.isEmpty()) {
      Text(
          text = stringResource(R.string.view_profile_screen_no_favorite_parking),
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.testTag("NoFavoritesMessage").padding(16.dp))
    } else {
      LazyColumn(
          modifier = Modifier.fillMaxWidth().testTag("FavoriteParkingList"),
          contentPadding = PaddingValues(16.dp)) {
            itemsIndexed(favoriteParkings) { index, parking ->
              FavoriteParkingCard(
                  navigationActions, parkingViewModel, userViewModel, parking, index)
            }
          }
    }
  }
}

@Composable
fun FavoriteParkingCard(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel,
    userViewModel: UserViewModel,
    parking: Parking,
    index: Int
) {
  val userState by userViewModel.currentUser.collectAsState()
  var showConfirmDialog by remember { mutableStateOf(false) }

  Card(
      modifier =
          Modifier.fillMaxWidth()
              .padding(8.dp)
              .clickable(
                  onClick = {
                    parkingViewModel.selectParking(parking)
                    navigationActions.navigateTo(Screen.PARKING_DETAILS)
                  })
              .testTag("ParkingItem$index"),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp).testTag("ParkingContent$index"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
              Column(
                  modifier = Modifier.weight(1f),
                  verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    // Parking name
                    androidx.compose.material3.Text(
                        text = parking.optName ?: stringResource(R.string.default_parking_name),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("ParkingName$index"))

                    // Display the personal note if it exists. Otherwise, display a message
                    // indicating that there is no note.
                    val personalNote = userState?.details?.personalNotes?.get(parking.uid)
                    Text(
                        text =
                            if (personalNote.isNullOrBlank()) {
                              stringResource(R.string.view_profile_screen_no_note)
                            } else {
                              stringResource(R.string.view_profile_screen_note, personalNote)
                            },
                        style =
                            if (personalNote.isNullOrBlank())
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontStyle = FontStyle.Italic, fontSize = 12.sp)
                            else MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("ParkingNote$index"),
                        textAlign = TextAlign.Justify)
                  }

              Spacer(modifier = Modifier.weight(0.1f).width(8.dp))

              // Favorite icon
              androidx.compose.material3.IconButton(
                  onClick = { showConfirmDialog = true },
                  modifier = Modifier.size(32.dp).testTag("FavoriteToggle$index")) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite Parking",
                        tint = Color.Red)
                  }
            }
      }

  if (showConfirmDialog) {
    AlertDialog(
        onDismissRequest = { showConfirmDialog = false },
        title = {
          Text(
              stringResource(R.string.view_profile_screen_remove_favorite_dialog_title),
              style = MaterialTheme.typography.titleMedium)
        },
        text = {
          Text(
              stringResource(
                  R.string.view_profile_screen_remove_favorite_dialog_message,
                  parking.optName ?: stringResource(R.string.default_parking_name)),
              textAlign = TextAlign.Justify)
        },
        confirmButton = {
          TextButton(
              onClick = {
                userViewModel.removeFavoriteParkingFromSelectedUser(parking)
                showConfirmDialog = false
              }) {
                Text(
                    stringResource(
                        R.string.view_profile_screen_remove_favorite_dialog_action_button))
              }
        },
        dismissButton = {
          TextButton(onClick = { showConfirmDialog = false }) {
            Text(stringResource(R.string.view_profile_screen_remove_favorite_dialog_cancel_button))
          }
        })
  }
}

@Composable
private fun UserReviewsSection(
    reviewViewModel: ReviewViewModel,
    userViewModel: UserViewModel,
    parkingViewModel: ParkingViewModel,
    navigationActions: NavigationActions,
    reviewToParkingMap: Map<String, Parking>
) {
  val userState by userViewModel.currentUser.collectAsState()

  LaunchedEffect(userState?.public?.userId) {
    userState?.public?.userId?.let { userId ->
      Log.d("ViewProfileScreen", "Fetching reviews for user $userId")
      reviewViewModel.getReviewsByOwnerId(userId)
    }
  }

  val userReviews by reviewViewModel.userReviews.collectAsState()

  Box(modifier = Modifier.fillMaxSize()) {
    if (userReviews.isEmpty()) {
      Text(
          text = stringResource(R.string.view_profile_screen_no_reviews_message),
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.padding(16.dp).testTag("NoReviewsMessage"))
    } else {
      LazyColumn(
          modifier = Modifier.fillMaxWidth().testTag("UserReviewsList"),
          contentPadding = PaddingValues(16.dp)) {
            items(
                // Filter out reviews that do not have a corresponding parking (desynced data)
                items = userReviews.filter { reviewToParkingMap.containsKey(it.uid) },
                key = { it.uid }) { curReview ->
                  val index = userReviews.indexOf(curReview)
                  val isExpanded = true

                  // We can assert parking is not null thanks to the filter above
                  val parking = reviewToParkingMap[curReview.uid]!!

                  ReviewCard(
                      review = curReview,
                      title = parking.optName ?: stringResource(R.string.default_parking_name),
                      index = index,
                      isExpanded = isExpanded,
                      onCardClick = {
                        parkingViewModel.selectParking(parking)
                        navigationActions.navigateTo(Screen.PARKING_DETAILS)
                      },
                      options = mapOf(),
                      userViewModel = userViewModel,
                      reviewViewModel = reviewViewModel,
                      ownerReputationScore = null,
                      navigationActions)
                }
          }
    }
  }
}

/** Section where all the User Images are displayed */
@Composable
private fun UserImagesSection(
    userViewModel: UserViewModel,
    parkingViewModel: ParkingViewModel,
    navigationActions: NavigationActions
) {
  val context = LocalContext.current
  val imagesUrls = userViewModel.selectedUserImageUrls.collectAsState().value
  val imagesPaths = userViewModel.selectedUserAssociatedImages.collectAsState().value

  // States for managing dialogs
  val showFirstDialog = remember { mutableStateOf(false) } // First dialog visibility
  val showDeleteDialog = remember { mutableStateOf(false) } // Delete confirmation dialog visibility
  val selectedImagePath = remember { mutableStateOf<String?>(null) }
  val selectedParkingId = remember { mutableStateOf<String?>(null) }
  val strResToast = stringResource(R.string.view_profile_screen_image_deleted)

  Box(modifier = Modifier.fillMaxSize()) {
    if (imagesUrls.isEmpty()) {
      Text(
          text = stringResource(R.string.view_profile_screen_no_images_message),
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.padding(16.dp).testTag("NoImagesMessage"))
    } else {
      LazyColumn(
          modifier = Modifier.fillMaxWidth().testTag("UserImagesList"),
          contentPadding = PaddingValues(16.dp)) {
            itemsIndexed(imagesUrls) { index, url ->
              UserImageCard(
                  url = url,
                  onClick = {
                    // When an image is clicked, trigger the first AlertDialog
                    selectedImagePath.value = imagesPaths[index]
                    selectedParkingId.value =
                        parkingViewModel.getParkingFromImagePath(imagesPaths[index])
                    showFirstDialog.value = true
                  })
            }
          }
    }
  }

  // First AlertDialog
  if (showFirstDialog.value) {
    AlertDialog(
        onDismissRequest = { showFirstDialog.value = false },
        title = { Text(stringResource(R.string.view_profile_screen_image_dialog_title)) },
        text = {
          Column {
            Text(stringResource(R.string.view_profile_screen_image_dialog_description))
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = rememberAsyncImagePainter(model = selectedImagePath.value),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(200.dp))
          }
        },
        confirmButton = {
          IconButton(
              onClick = {
                parkingViewModel.getParkingById(
                    selectedParkingId.value!!,
                    { parkingForPath ->
                      parkingViewModel.selectParking(parkingForPath)
                      navigationActions.navigateTo(Screen.PARKING_DETAILS)
                      showFirstDialog.value = false
                    },
                    {})
              },
              icon = Icons.Filled.LocalParking,
              contentDescription = "Parking From Image",
              modifier = Modifier.testTag("ParkingFromImageButton"))
        },
        dismissButton = {
          // Delete Button opens the SECOND delete dialog
          IconButton(
              onClick = {
                showFirstDialog.value = false
                showDeleteDialog.value = true // Trigger the DeleteConfirmationDialog
              },
              modifier = Modifier.testTag("DeleteImageButton"),
              icon = Icons.Filled.Delete,
              contentDescription = "Delete Image")
        })
  }

  if (showDeleteDialog.value) {
    DeleteConfirmationDialog(
        showDialog = showDeleteDialog,
        onDismiss = { showDeleteDialog.value = false },
        onConfirm = {
          val imagePath = selectedImagePath.value
          val parkingId = selectedParkingId.value

          if (imagePath != null && parkingId != null) {
            userViewModel.removeImageFromUserImages(imagePath)
            parkingViewModel.getParkingById(
                parkingId,
                { parkingForPath ->
                  parkingViewModel.selectParking(parkingForPath)
                  parkingViewModel.deleteImageFromParking(parkingId, imagePath)
                  Toast.makeText(context, strResToast, Toast.LENGTH_SHORT).show()
                  navigationActions.navigateTo(Screen.VIEW_PROFILE)
                },
                {})
          }
          showDeleteDialog.value = false
        })
  }
}

/** Card representing a UserImage to display in the above column */
@Composable
private fun UserImageCard(url: String, onClick: (String) -> Unit) {
  Card(
      modifier =
          Modifier.fillMaxWidth()
              .padding(8.dp)
              .clickable { onClick(url) } // Attach onClick here
              .testTag("ImageCard"),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
          Spacer(modifier = Modifier.height(8.dp))
          Image(
              painter = rememberAsyncImagePainter(model = url),
              contentDescription = null,
              modifier = Modifier.fillMaxWidth().height(200.dp).testTag("ImageContent"))
        }
      }
}
