package com.github.se.cyrcle.ui.profile

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Outbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
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
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingViewModel
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

const val FIRST_NAME_MIN_LENGTH = 0
const val FIRST_NAME_MAX_LENGTH = 32
const val LAST_NAME_MIN_LENGTH = 0
const val LAST_NAME_MAX_LENGTH = 32
const val USERNAME_MIN_LENGTH = 4
const val USERNAME_MAX_LENGTH = 32

@Composable
fun ViewProfileScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    parkingViewModel: ParkingViewModel,
) {
  fun areInputsValid(firstName: String, lastName: String, username: String): Boolean {
    return firstName.length in FIRST_NAME_MIN_LENGTH..FIRST_NAME_MAX_LENGTH &&
        lastName.length in LAST_NAME_MIN_LENGTH..LAST_NAME_MAX_LENGTH &&
        username.length in USERNAME_MIN_LENGTH..USERNAME_MAX_LENGTH
  }

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
                saveButton = {
                  Button(
                      text = stringResource(R.string.view_profile_screen_save_button),
                      onClick = {
                        userViewModel.updateUser(it, context)
                        isEditing = false
                      },
                      colorLevel = ColorLevel.PRIMARY,
                      enabled =
                          areInputsValid(
                              it.details!!.lastName, it.details.firstName, it.public.username),
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
              Button(
                  text = stringResource(R.string.view_profile_screen_modify_profile_button),
                  onClick = { isEditing = true },
                  colorLevel = ColorLevel.TERTIARY,
                  testTag = "EditButton")

              Spacer(modifier = Modifier.height(24.dp))

              FavoriteParkingsSection(userViewModel, parkingViewModel, navigationActions)
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

  Spacer(modifier = Modifier.height(16.dp))

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
