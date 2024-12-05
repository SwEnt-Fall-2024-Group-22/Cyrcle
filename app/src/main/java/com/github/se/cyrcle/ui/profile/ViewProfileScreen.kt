package com.github.se.cyrcle.ui.profile

import android.app.Activity
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Outbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.review.Review
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.navigation.TopLevelDestinations
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.Red
import com.github.se.cyrcle.ui.theme.atoms.Button
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.BottomNavigationBar

/** Maximum length for the truncated text in review cards. */
const val MAX_TRUNCATED_TEXT_LENGTH = 65

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

  Scaffold(
      modifier = Modifier.testTag("ViewProfileScreen"),
      bottomBar = {
        BottomNavigationBar(
            navigationActions = navigationActions,
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.VIEW_PROFILE)
      },
      floatingActionButton = {
        if (userState?.public?.userId != null) {
          if (userState!!.details?.isAdmin == true) {
            FloatingActionButton(
                onClick = { navigationActions.navigateTo(Screen.ADMIN) },
                modifier = Modifier.testTag("AdminFAB"),
                containerColor = MaterialTheme.colorScheme.primary) {
                  Text(text = "Admin", color = MaterialTheme.colorScheme.onPrimary)
                }
          }
        }
      }) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
          com.github.se.cyrcle.ui.theme.atoms.IconButton(
              modifier = Modifier.padding(10.dp).align(Alignment.TopEnd),
              icon = Icons.Filled.Outbox,
              contentDescription = "Sign Out",
              testTag = "SignOutButton",
              onClick = { signOut = true })

          com.github.se.cyrcle.ui.theme.atoms.IconButton(
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

              // Scrollable Column instead of LazyColumn
              Column(
                  modifier =
                      Modifier.fillMaxWidth()
                          .verticalScroll(rememberScrollState())
                          .padding(top = 56.dp) // Adjust based on button height
                          .testTag("ProfileContentSections")) {
                    FavoriteParkingsSection(userViewModel, parkingViewModel, navigationActions)

                    Spacer(modifier = Modifier.height(16.dp))

                    UserReviewsSection(reviewViewModel, userViewModel, parkingViewModel)
                  }
            }
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

  Text(
      text = stringResource(R.string.view_profile_screen_favorite_parking_title),
      style = MaterialTheme.typography.titleLarge,
      modifier = Modifier.testTag("FavoriteParkingsTitle"))

  Spacer(modifier = Modifier.height(12.dp))

  if (favoriteParkings.isEmpty()) {
    Text(
        text = stringResource(R.string.view_profile_screen_no_favorite_parking),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.testTag("NoFavoritesMessage"))
  } else {
    LazyRow(
        modifier = Modifier.fillMaxWidth().testTag("FavoriteParkingList"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          itemsIndexed(favoriteParkings) { index, parking ->
            FavoriteParkingCard(
                parking = parking,
                index = index,
                onRemove = { userViewModel.removeFavoriteParkingFromSelectedUser(parking) },
                parkingViewModel,
                navigationActions)
          }
        }
  }
}

@Composable
private fun FavoriteParkingCard(
    parking: Parking,
    index: Int,
    onRemove: () -> Unit,
    parkingViewModel: ParkingViewModel,
    navigationActions: NavigationActions
) {
  var showConfirmDialog by remember { mutableStateOf(false) }

  Card(
      modifier =
          Modifier.size(120.dp)
              .padding(8.dp)
              .clickable(
                  onClick = {
                    parkingViewModel.selectParking(parking)
                    navigationActions.navigateTo(Screen.PARKING_DETAILS)
                  }),
      shape = MaterialTheme.shapes.medium) {
        Box(modifier = Modifier.fillMaxSize()) {
          Text(
              text = parking.optName ?: "",
              style = MaterialTheme.typography.bodySmall,
              modifier =
                  Modifier.align(Alignment.Center).padding(8.dp).testTag("ParkingItem_$index"))

          IconButton(
              onClick = { showConfirmDialog = true },
              modifier =
                  Modifier.align(Alignment.TopEnd).size(32.dp).testTag("FavoriteToggle_$index")) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription =
                        stringResource(R.string.view_profile_screen_remove_from_favorite),
                    tint = Red,
                    modifier = Modifier.size(20.dp))
              }
        }
      }

  if (showConfirmDialog) {
    AlertDialog(
        onDismissRequest = { showConfirmDialog = false },
        title = { Text(stringResource(R.string.view_profile_screen_remove_favorite_dialog_title)) },
        text = {
          Text(
              stringResource(
                  R.string.view_profile_screen_remove_favorite_dialog_message,
                  parking.optName ?: stringResource(R.string.default_parking_name)))
        },
        confirmButton = {
          TextButton(
              onClick = {
                onRemove()
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
    parkingViewModel: ParkingViewModel
) {
  val userState = userViewModel.currentUser.collectAsState().value

  if (userState != null) {
    LaunchedEffect(userState.public.userId) {
      reviewViewModel.getReviewsByOwnerId(userState.public.userId)
    }
  }
  val userReviews = reviewViewModel.userReviews.collectAsState().value

  Text(
      text = stringResource(R.string.view_profile_screen_my_reviews_title),
      style = MaterialTheme.typography.titleLarge,
      modifier = Modifier.testTag("UserReviewsTitle"))

  Spacer(modifier = Modifier.height(12.dp))

  if (userReviews.isEmpty()) {
    Text(
        text = stringResource(R.string.view_profile_screen_no_reviews_message),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.testTag("NoReviewsMessage"))
  } else {
    LazyRow(
        modifier = Modifier.fillMaxWidth().testTag("UserReviewsList"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          itemsIndexed(userReviews) { _, review ->
            review?.let { ReviewCard(review = it, parkingViewModel = parkingViewModel) }
          }
        }
  }
}

/**
 * A composable that displays a card containing the user's review information.
 *
 * @param review The review to display
 * @param parkingViewModel The view model used to fetch the parking's name
 */
@Composable
private fun ReviewCard(review: Review, parkingViewModel: ParkingViewModel) {
  val defaultParkingName = stringResource(R.string.default_parking_name)
  var parkingName by remember { mutableStateOf(defaultParkingName) }

  // Fetch parking name when the card is created
  LaunchedEffect(review.parking) {
    parkingViewModel.getParkingById(
        review.parking,
        onSuccess = { parking -> parkingName = parking.optName ?: defaultParkingName },
        onFailure = {})
  }

  Card(
      modifier = Modifier.padding(8.dp).width(260.dp).height(200.dp),
      shape = MaterialTheme.shapes.medium) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween) {
              // Display parking name with unique test tag
              Text(
                  text = parkingName,
                  style = MaterialTheme.typography.titleMedium,
                  modifier = Modifier.fillMaxWidth().testTag("ParkingName_${review.uid}"))

              Spacer(modifier = Modifier.height(8.dp))

              // Display rating with unique test tag
              Text(
                  text =
                      stringResource(
                          R.string.view_profile_screen_you_rated_parking, review.rating.toString()),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.testTag("RatingText_${review.uid}"))

              Spacer(modifier = Modifier.height(8.dp))

              // Display review text section
              Text(
                  text = stringResource(R.string.view_profile_screen_you_said),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.testTag("YouSaidText_${review.uid}"))

              androidx.compose.material3.Text(
                  text = "\"${truncateText(review.text)}\"",
                  style = MaterialTheme.typography.bodyMedium,
                  maxLines = 2,
                  softWrap = false,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.fillMaxWidth().testTag("ReviewText_${review.uid}"))

              Spacer(modifier = Modifier.height(8.dp))

              // Display likes and dislikes counts
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End,
                  verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                          // Likes count
                          Row(
                              horizontalArrangement = Arrangement.spacedBy(4.dp),
                              verticalAlignment = Alignment.CenterVertically,
                              modifier = Modifier.testTag("LikesCount_${review.uid}")) {
                                Text(
                                    text =
                                        stringResource(
                                            R.string.view_profile_screen_likes_count,
                                            review.likedBy.size),
                                    style = MaterialTheme.typography.bodyMedium)
                              }
                          // Dislikes count
                          Row(
                              horizontalArrangement = Arrangement.spacedBy(4.dp),
                              verticalAlignment = Alignment.CenterVertically,
                              modifier = Modifier.testTag("DislikesCount_${review.uid}")) {
                                Text(
                                    text =
                                        stringResource(
                                            R.string.view_profile_screen_dislikes_count,
                                            review.dislikedBy.size),
                                    style = MaterialTheme.typography.bodyMedium)
                              }
                        }
                  }
            }
      }
}

/**
 * Truncates the given text if it exceeds the maximum length.
 *
 * @param text The text to truncate
 * @return The truncated text with "..." appended if it exceeds [MAX_TRUNCATED_TEXT_LENGTH],
 *   otherwise returns the original text
 */
private fun truncateText(text: String): String {
  // Reserve 4 characters: 1 for opening quote, 3 for "..." and 1 for closing quote
  val maxLength = MAX_TRUNCATED_TEXT_LENGTH - 5
  return if (text.length > maxLength) {
    text.take(maxLength) + "..."
  } else {
    text
  }
}
