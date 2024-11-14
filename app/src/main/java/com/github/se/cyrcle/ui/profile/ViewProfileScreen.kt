package com.github.se.cyrcle.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.authentication.Authenticator
import com.github.se.cyrcle.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.navigation.TopLevelDestinations
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.Red
import com.github.se.cyrcle.ui.theme.atoms.Button
import com.github.se.cyrcle.ui.theme.atoms.ConditionCheckingInputText
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
    authenticator: Authenticator
) {
  val userState by userViewModel.currentUser.collectAsState()
  var isEditing by remember { mutableStateOf(false) }
  var firstName by remember { mutableStateOf(userState?.details?.firstName ?: "") }
  var lastName by remember { mutableStateOf(userState?.details?.lastName ?: "") }
  var username by remember { mutableStateOf(userState?.public?.username ?: "") }
  var profilePictureUrl by remember { mutableStateOf(userState?.public?.profilePictureUrl ?: "") }

  val imagePickerLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { profilePictureUrl = it.toString() }
      }

  Scaffold(
      modifier = Modifier.testTag("ViewProfileScreen"),
      bottomBar = {
        BottomNavigationBar(
            navigationActions = navigationActions,
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.PROFILE)
      }) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
          authenticator.SignOutButton(Modifier.padding(10.dp).align(Alignment.TopEnd)) {
            userViewModel.setCurrentUser(null)
            navigationActions.navigateTo(TopLevelDestinations.AUTH)
          }

          Column(
              modifier = Modifier.fillMaxSize().padding(16.dp).testTag("ProfileContent"),
              horizontalAlignment = Alignment.CenterHorizontally) {
                if (isEditing) {
                  EditProfileContent(
                      firstName = firstName,
                      lastName = lastName,
                      username = username,
                      profilePictureUrl = profilePictureUrl,
                      onFirstNameChange = { firstName = it },
                      onLastNameChange = { lastName = it },
                      onUsernameChange = { username = it },
                      onImageClick = { imagePickerLauncher.launch("image/*") },
                      onSave = {
                        userViewModel.updateUser(
                            userState?.copy(
                                public =
                                    userState
                                        ?.public!!
                                        .copy(
                                            username = username,
                                            profilePictureUrl = profilePictureUrl),
                                details =
                                    userState
                                        ?.details
                                        ?.copy(firstName = firstName, lastName = lastName))
                                ?: return@EditProfileContent)
                        userViewModel.getUserById(userState?.public!!.userId)
                        isEditing = false
                      },
                      onCancel = {
                        firstName = userState?.details?.firstName ?: ""
                        lastName = userState?.details?.lastName ?: ""
                        username = userState?.public!!.username
                        profilePictureUrl = userState?.public!!.profilePictureUrl
                        isEditing = false
                      })
                } else {
                  DisplayProfileContent(
                      firstName = firstName,
                      lastName = lastName,
                      username = username,
                      profilePictureUrl = profilePictureUrl,
                      onEditClick = { isEditing = true })

                  Spacer(modifier = Modifier.height(24.dp))

                  FavoriteParkingsSection(userViewModel)
                }
              }
        }
      }
}

@Composable
private fun EditProfileContent(
    firstName: String,
    lastName: String,
    username: String,
    profilePictureUrl: String,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onImageClick: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
  ProfileImage(
      url = profilePictureUrl,
      onClick = onImageClick,
      isEditable = true,
      modifier = Modifier.testTag("ProfileImage"))

  Spacer(modifier = Modifier.height(24.dp))

  ConditionCheckingInputText(
      value = firstName,
      onValueChange = onFirstNameChange,
      label = stringResource(R.string.view_profile_screen_first_name_label),
      minCharacters = FIRST_NAME_MIN_LENGTH,
      maxCharacters = FIRST_NAME_MAX_LENGTH,
      testTag = "FirstNameField")

  Spacer(modifier = Modifier.height(8.dp))

  ConditionCheckingInputText(
      value = lastName,
      onValueChange = onLastNameChange,
      label = stringResource(R.string.view_profile_screen_last_name_label),
      minCharacters = LAST_NAME_MIN_LENGTH,
      maxCharacters = LAST_NAME_MAX_LENGTH,
      testTag = "LastNameField")

  Spacer(modifier = Modifier.height(8.dp))

  ConditionCheckingInputText(
      value = username,
      onValueChange = onUsernameChange,
      label = stringResource(R.string.view_profile_screen_username_label),
      minCharacters = USERNAME_MIN_LENGTH,
      maxCharacters = USERNAME_MAX_LENGTH,
      testTag = "UsernameField")

  Spacer(modifier = Modifier.height(16.dp))

  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    Button(
        text = stringResource(R.string.view_profile_screen_save_button),
        onClick = onSave,
        colorLevel = ColorLevel.PRIMARY,
        enabled = areInputsValid(firstName, lastName, username),
        testTag = "SaveButton")

    Button(
        text = stringResource(R.string.view_profile_screen_cancel_button),
        onClick = onCancel,
        colorLevel = ColorLevel.SECONDARY,
        testTag = "CancelButton")
  }
}

fun areInputsValid(firstName: String, lastName: String, username: String): Boolean {
  return firstName.length in FIRST_NAME_MIN_LENGTH..FIRST_NAME_MAX_LENGTH &&
      lastName.length in LAST_NAME_MIN_LENGTH..LAST_NAME_MAX_LENGTH &&
      username.length in USERNAME_MIN_LENGTH..USERNAME_MAX_LENGTH
}

@Composable
private fun DisplayProfileContent(
    firstName: String,
    lastName: String,
    username: String,
    profilePictureUrl: String,
    onEditClick: () -> Unit
) {
  Text(
      text = firstName,
      style = MaterialTheme.typography.headlineMedium,
      testTag = "DisplayFirstName")

  Text(
      text = lastName, style = MaterialTheme.typography.headlineMedium, testTag = "DisplayLastName")

  Spacer(modifier = Modifier.height(16.dp))

  ProfileImage(
      url = profilePictureUrl,
      onClick = {},
      isEditable = false,
      modifier = Modifier.testTag("ProfileImage"))

  Spacer(modifier = Modifier.height(8.dp))

  Text(
      text = stringResource(R.string.view_profile_screen_display_username, username),
      style = MaterialTheme.typography.bodyMedium,
      testTag = "DisplayUsername")

  Spacer(modifier = Modifier.height(16.dp))

  Button(
      text = stringResource(R.string.view_profile_screen_modify_profile_button),
      onClick = onEditClick,
      colorLevel = ColorLevel.TERTIARY,
      testTag = "EditButton")
}

@Composable
private fun ProfileImage(
    url: String,
    onClick: () -> Unit,
    isEditable: Boolean,
    modifier: Modifier = Modifier
) {
  Box(
      modifier =
          modifier.then(if (isEditable) Modifier.clickable(onClick = onClick) else Modifier)) {
        if (url.isBlank()) {
          Box(
              modifier =
                  Modifier.size(120.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription =
                        stringResource(R.string.view_profile_screen_default_profile_picture),
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer)
              }
        } else {
          Image(
              painter =
                  rememberAsyncImagePainter(
                      ImageRequest.Builder(LocalContext.current)
                          .data(url)
                          .apply { transformations(CircleCropTransformation()) }
                          .build()),
              contentDescription = stringResource(R.string.view_profile_screen_profile_picture),
              modifier = Modifier.size(120.dp).clip(CircleShape),
              contentScale = ContentScale.Crop)
        }

        if (isEditable) {
          Box(
              modifier =
                  Modifier.size(120.dp)
                      .clip(CircleShape)
                      .background(Color.Black.copy(alpha = 0.3f)),
              contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription =
                        stringResource(R.string.view_profile_screen_edit_profile_picture),
                    tint = Color.White,
                    modifier = Modifier.size(40.dp))
              }
        }
      }
}

@Composable
private fun FavoriteParkingsSection(userViewModel: UserViewModel) {
  val favoriteParkings = userViewModel.favoriteParkings.collectAsState().value

  LaunchedEffect(Unit) { userViewModel.getSelectedUserFavoriteParking() }

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
                onRemove = {
                  userViewModel.removeFavoriteParkingFromSelectedUser(parking.uid)
                  userViewModel.getSelectedUserFavoriteParking()
                })
          }
        }
  }
}

@Composable
private fun FavoriteParkingCard(parking: Parking, index: Int, onRemove: () -> Unit) {
  var showConfirmDialog by remember { mutableStateOf(false) }

  Card(modifier = Modifier.size(120.dp).padding(8.dp), shape = MaterialTheme.shapes.medium) {
    Box(modifier = Modifier.fillMaxSize()) {
      Text(
          text = parking.optName ?: "",
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.align(Alignment.Center).padding(8.dp).testTag("ParkingItem_$index"))

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
