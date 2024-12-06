package com.github.se.cyrcle.ui.profile

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Outbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.github.se.cyrcle.ui.theme.Red
import com.github.se.cyrcle.ui.theme.atoms.Button
import com.github.se.cyrcle.ui.theme.atoms.IconButton
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.BottomNavigationBar

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
          IconButton(
              modifier = Modifier.padding(10.dp).align(Alignment.TopEnd),
              icon = Icons.Filled.Outbox,
              contentDescription = "Sign Out",
              testTag = "SignOutButton",
              onClick = { signOut = true })

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

              // Display the TabLayout
              TabLayout(
                  userViewModel = userViewModel,
                  parkingViewModel = parkingViewModel,
                  reviewViewModel = reviewViewModel,
                  navigationActions = navigationActions)
            }
          }
        }
      }
}

/** Tab layout that displays different sections based on the selected tab. */
@Composable
private fun TabLayout(
    userViewModel: UserViewModel,
    parkingViewModel: ParkingViewModel,
    reviewViewModel: ReviewViewModel,
    navigationActions: NavigationActions
) {
  var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
  val tabs = listOf("Favorite Parkings", "My Reviews")

  Column(
      modifier = Modifier.fillMaxWidth().padding(top = 56.dp) // Adjust based on button height
      ) {
        TabRow(selectedTabIndex = selectedTabIndex, modifier = Modifier.testTag("TabRow")) {
          tabs.forEachIndexed { index, title ->
            Tab(
                text = { Text(title) },
                selected = selectedTabIndex == index,
                onClick = { selectedTabIndex = index },
                modifier = Modifier.testTag("Tab${title.replace(" ", "")}"))
          }
        }

        when (selectedTabIndex) {
          0 -> FavoriteParkingsSection(userViewModel, parkingViewModel, navigationActions)
          1 ->
              UserReviewsSection(
                  reviewViewModel, userViewModel, parkingViewModel, navigationActions)
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

  if (favoriteParkings.isEmpty()) {
    Text(
        text = stringResource(R.string.view_profile_screen_no_favorite_parking),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.testTag("NoFavoritesMessage").padding(16.dp))
  } else {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("FavoriteParkingList"),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

          androidx.compose.material3.IconButton(
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
    parkingViewModel: ParkingViewModel,
    navigationActions: NavigationActions
) {
  val userState by userViewModel.currentUser.collectAsState()
  val userReviews by reviewViewModel.userReviews.collectAsState()

  LaunchedEffect(userState?.public?.userId) {
    userState?.public?.userId?.let { userId -> reviewViewModel.getReviewsByOwnerId(userId) }
  }

  if (userReviews.isEmpty()) {
    Text(
        text = stringResource(R.string.view_profile_screen_no_reviews_message),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(16.dp).testTag("NoReviewsMessage"))
  } else {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("UserReviewsList"),
        contentPadding = PaddingValues(16.dp)) {
          items(items = userReviews, key = { it.uid }) { curReview ->
            val index = userReviews.indexOf(curReview)
            val isExpanded = true
            var parking by remember { mutableStateOf<Parking?>(null) }
            parkingViewModel.getParkingById(curReview.parking, { parking = it }, {})

            parking?.let {
              val defaultUsername = stringResource(R.string.undefined_username)
              ReviewCard(
                  review = curReview,
                  ownerUsername = userState?.public?.username ?: defaultUsername,
                  index = index,
                  isExpanded = isExpanded,
                  onCardClick = {
                    parkingViewModel.selectParking(it)
                    navigationActions.navigateTo(Screen.PARKING_DETAILS)
                  },
                  options = mapOf(),
                  userViewModel = userViewModel,
                  reviewViewModel = reviewViewModel)
            }
          }
        }
  }
}
