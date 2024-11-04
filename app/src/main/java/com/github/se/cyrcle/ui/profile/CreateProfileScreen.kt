package com.github.se.cyrcle.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

@Composable
fun CreateProfile(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  Scaffold(
      modifier = Modifier.fillMaxSize().testTag("CreateProfileScreen"),
      topBar = { TopAppBar(navigationActions, "Create Profile") }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.Center) {
              Text("TODOOOOOOOOOO")

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
