package com.github.se.cyrcle.ui.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.user.UserDetails
import com.github.se.cyrcle.model.user.UserPublic
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.TopLevelDestinations
import com.github.se.cyrcle.ui.theme.atoms.GoogleSignInButton
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

@Composable
fun CreateProfileScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  val context = LocalContext.current
  val defaultUser = User(UserPublic("", ""), UserDetails())

  val validationToastText = stringResource(R.string.create_profile_validation_toast)
  val errorToastText = stringResource(R.string.create_profile_error_toast)
  val accountExistsToastText = stringResource(R.string.create_profile_account_already_exists)

  val onSignInClick = { createdUser: User ->
    userViewModel.authenticate(
        // On successful authentication
        { userId ->
          val user = createdUser.copy(public = createdUser.public.copy(userId = userId))

          // Check if user already exists
          userViewModel.doesUserExist(user) { userExists ->
            if (userExists) {
              Toast.makeText(context, accountExistsToastText, Toast.LENGTH_SHORT).show()
              navigationActions.goBack()
            } else {

              // Add the user then continue
              userViewModel.addUser(
                  user,
                  {
                    userViewModel.setCurrentUser(user)
                    Toast.makeText(context, validationToastText, Toast.LENGTH_SHORT).show()
                    navigationActions.navigateTo(TopLevelDestinations.MAP)
                  },
                  {
                    Log.e("CreateProfileScreen", "Error adding user")
                    Toast.makeText(context, errorToastText, Toast.LENGTH_SHORT).show()
                  })
            }
          }
        },
        // On failed authentication
        {
          Log.e("CreateProfileScreen", "Error authenticating")
          Toast.makeText(context, errorToastText, Toast.LENGTH_SHORT).show()
        })
  }

  Scaffold(
      topBar = {
        TopAppBar(navigationActions, stringResource(R.string.create_profile_screen_title))
      }) { padding ->
        EditProfileComponent(
            defaultUser,
            verticalButtonDisplay = true,
            modifier = Modifier.padding(padding),
            saveButton = { userAttempt: User ->
              Text(
                  stringResource(R.string.create_profile_sign_in_explanation),
                  modifier = Modifier.padding(bottom = 5.dp),
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
              GoogleSignInButton { onSignInClick(userAttempt) }
            },
            cancelButton = {})
      }
}
