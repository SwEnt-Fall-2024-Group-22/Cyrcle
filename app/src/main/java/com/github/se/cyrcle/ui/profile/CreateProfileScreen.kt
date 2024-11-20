package com.github.se.cyrcle.ui.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.user.UserDetails
import com.github.se.cyrcle.model.user.UserPublic
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.authentication.Authenticator
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.TopLevelDestinations
import com.github.se.cyrcle.ui.theme.atoms.Button
import com.github.se.cyrcle.ui.theme.atoms.Text

@Composable
fun CreateProfileScreen(
    navigationActions: NavigationActions,
    authenticator: Authenticator,
    userViewModel: UserViewModel
) {
  val context = LocalContext.current
  val defaultUser = User(UserPublic("", ""), UserDetails())

  // Uses the Auth UID as userID for our new user
  val authCompleteCallback = { userAttempt: User, userAuthenticated: User ->
    val user =
        userAttempt.copy(public = userAttempt.public.copy(userId = userAuthenticated.public.userId))
    userViewModel.addUser(user)
    Toast.makeText(context, "Profile created successfully!", Toast.LENGTH_SHORT).show()
    navigationActions.navigateTo(TopLevelDestinations.MAP)
  }

  val authErrorCallback = { e: Exception ->
    Log.d("CreateProfileScreen", "Error authenticating: $e")
    Toast.makeText(context, "Error during authentication...", Toast.LENGTH_SHORT).show()
  }

  EditProfileComponent(
      defaultUser,
      verticalButtonDisplay = true,
      saveButton = { userAttempt: User ->
        Text(
            "To complete account creation,\n please authenticate with Google to link your email.",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
        authenticator.AuthenticateButton(
            onAuthComplete = { authCompleteCallback(userAttempt, it) },
            onAuthError = authErrorCallback)
      },
      cancelButton = { Button("Cancel", onClick = { navigationActions.goBack() }) })
}
