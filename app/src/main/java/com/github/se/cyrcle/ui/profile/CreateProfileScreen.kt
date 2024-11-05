package com.github.se.cyrcle.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

@Composable
fun CreateProfileScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  val screenTitle = stringResource(R.string.profile_screen_title)

  Scaffold(
      modifier = Modifier.fillMaxSize().testTag("CreateProfileScreen"),
      topBar = { TopAppBar(navigationActions, screenTitle) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.Center) {
              Text("TODO")

              // TODO Toast text need to take inspiration from this
              // val failSignInMsg = stringResource(R.string.sign_in_failed_toast)
              // val successSignInMsg = stringResource(R.string.sign_in_successful_toast)
              //
              // GoogleSignInButton(
              //    onAuthComplete = { result: AuthResult ->
              //      val user =
              //          User(
              //              userId = result.user?.uid ?: "",
              //              username = result.user?.displayName ?: "",
              //              email = result.user?.email ?: "")
              //      Log.d("Cyrcle", "User account created: ${result.user?.displayName}")
              //      Toast.makeText(context, "Welcome ${user.username}!", Toast.LENGTH_LONG).show()

              //      userViewModel.addUser(user)
              //      userViewModel.setCurrentUser(user)
              //      navigationActions.navigateTo(TopLevelDestinations.MAP)
              //    },
              //    onAuthError = { e: ApiException ->
              //      Log.e("Cyrcle", "Failed to sign in: ${e.statusCode}")
              //      Toast.makeText(context, "Account creation failed...",
              // Toast.LENGTH_LONG).show()
              //    })
            }
      }
}
