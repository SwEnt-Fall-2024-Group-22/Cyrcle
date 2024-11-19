package com.github.se.cyrcle.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.authentication.Authenticator
import com.github.se.cyrcle.ui.navigation.NavigationActions

@Composable
fun ProfileScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    authenticator: Authenticator
) {
  val isSignedIn by userViewModel.isSignedIn.collectAsState(false)

  if (isSignedIn) ViewProfileScreen(navigationActions, userViewModel, authenticator)
  else CreateProfileScreen(navigationActions)
}
