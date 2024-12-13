package com.github.se.cyrcle.ui.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.user.ACCOUNT_CREATION_REWARD
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.user.UserDetails
import com.github.se.cyrcle.model.user.UserPublic
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.model.user.Wallet
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.atoms.Button
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

@Composable
fun CreateProfileScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  val context = LocalContext.current
  val defaultUser = User(UserPublic("", ""), UserDetails())

  val validationToastText = stringResource(R.string.create_profile_validation_toast)
  val welcomeBonusText =
      stringResource(R.string.create_profile_welcome_bonus_toast, ACCOUNT_CREATION_REWARD)
  val combinedToastText = "$validationToastText\n$welcomeBonusText"

  val errorToastText = stringResource(R.string.create_profile_error_toast)
  val accountExistsToastText = stringResource(R.string.create_profile_account_already_exists)
  val incorrectFieldsToastText = stringResource(R.string.create_profile_incorrect_field_inputs)

  val onSignInClick = { createdUser: User ->
    userViewModel.authenticate(
        // On successful authentication
        { userId ->
          val userWithId = createdUser.copy(public = createdUser.public.copy(userId = userId))

          // Check if user already exists
          userViewModel.doesUserExist(userWithId) { userExists ->
            if (userExists) {
              Toast.makeText(context, accountExistsToastText, Toast.LENGTH_SHORT).show()
              navigationActions.goBack()
            } else {
              val userWithCoins =
                  userWithId.copy(
                      details =
                          userWithId.details?.copy(
                              wallet = Wallet() // Start with 100 coins
                              ))
              // Add the user then continue
              userViewModel.addUser(
                  userWithCoins,
                  {
                    // setCurrentUser needs to be called even though it is done in the update since
                    // there is first a check in the UserViewModel that does not pass if the user is
                    // not set before
                    userViewModel.setCurrentUser(userWithCoins)
                    userViewModel.updateUser(userWithCoins, context)
                    userViewModel.setIsOnlineMode(true)
                    Toast.makeText(context, combinedToastText, Toast.LENGTH_LONG).show()
                    navigationActions.navigateTo(Screen.TUTORIAL)
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
            saveButton = { userAttempt: User, validInputs: Boolean ->
              Text(
                  stringResource(R.string.create_profile_sign_in_explanation),
                  modifier = Modifier.padding(bottom = 5.dp),
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
              Button(
                  onClick = {
                    if (validInputs) onSignInClick(userAttempt)
                    else
                        Toast.makeText(context, incorrectFieldsToastText, Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier.testTag("AuthenticateButton"),
                  text = "Create")
            },
            cancelButton = {})
      }
}
