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
import com.github.se.cyrcle.ui.authentication.Authenticator
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.TopLevelDestinations
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

@Composable
fun CreateProfileScreen(
    navigationActions: NavigationActions,
    authenticator: Authenticator,
    userViewModel: UserViewModel
) {
  val context = LocalContext.current
  val defaultUser = User(UserPublic("", ""), UserDetails())

  val validationToastText = stringResource(R.string.create_profile_validation_toast)
  val errorToastText = stringResource(R.string.create_profile_error_toast)

  // Uses the Auth UID as userID for our new user
  val authCompleteCallback = { userAttempt: User, userAuthenticated: User ->
    val user =
        userAttempt.copy(public = userAttempt.public.copy(userId = userAuthenticated.public.userId))

    userViewModel.signIn(user)

    Toast.makeText(context, validationToastText, Toast.LENGTH_SHORT).show()
    navigationActions.navigateTo(TopLevelDestinations.MAP)
  }

  val authErrorCallback = { e: Exception ->
    Log.d("CreateProfileScreen", "Error authenticating: $e")
    Toast.makeText(context, errorToastText, Toast.LENGTH_SHORT).show()
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
              authenticator.AuthenticateButton(
                  onAuthComplete = { authCompleteCallback(userAttempt, it) },
                  onAuthError = authErrorCallback)
            },
            cancelButton = {})
      }
}
