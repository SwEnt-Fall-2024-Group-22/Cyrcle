package com.github.se.cyrcle.ui.authentication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Outbox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.atoms.IconButton
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.googleSignInButtonStyle

/**
 * Interface for a Composable repository It provides buttons with callbacks once authentication is
 * complete This does not interface with the UserRepository or ViewModel
 */
interface Authenticator {

  /**
   * Composable button that authenticates the user
   *
   * @param onAuthComplete callback for when authentication is successful
   * @param onAuthError callback for when authentication fails
   */
  @Composable
  fun AuthenticateButton(onAuthComplete: (User) -> Unit, onAuthError: (Exception) -> Unit)

  /**
   * Composable button that signs the user in anonymously
   *
   * @param onComplete callback for when sign in is complete
   */
  @Composable fun SignInAnonymouslyButton(onComplete: () -> Unit)

  /**
   * Composable button that signs the user out
   *
   * @param onComplete callback for when the button is clicked
   */
  @Composable fun SignOutButton(onComplete: () -> Unit)

  /** Static methods for default composable buttons */
  companion object {

    /**
     * Default composable button for authentication
     *
     * @param onClick callback for when the button is clicked
     */
    @Composable
    fun DefaultAuthenticateButton(onClick: () -> Unit) {
      Button(
          onClick = onClick,
          colors = ButtonDefaults.buttonColors(containerColor = Color.White),
          shape = RoundedCornerShape(50),
          border = BorderStroke(1.dp, Color.LightGray),
          modifier =
              Modifier.padding(16.dp)
                  .height(48.dp) // Adjust height as needed
                  .testTag("AuthenticateButton")) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()) {

                  // Google Logo
                  Image(
                      painter = painterResource(id = R.drawable.google_logo),
                      contentDescription = "Google Logo",
                      modifier = Modifier.size(30.dp).padding(end = 8.dp))

                  // Text on Sign-In button
                  Text(
                      text = stringResource(R.string.sign_in_google_button),
                      color = Color.Gray,
                      style = googleSignInButtonStyle)
                }
          }
    }

    /**
     * Default composable button for anonymous login
     *
     * @param onClick callback for when the button is clicked
     */
    @Composable
    fun DefaultAnonymousLoginButton(onClick: () -> Unit) {
      com.github.se.cyrcle.ui.theme.atoms.Button(
          text = stringResource(R.string.sign_in_guest_button),
          onClick = onClick,
          colorLevel = ColorLevel.SECONDARY,
          modifier =
              Modifier.padding(16.dp)
                  .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(50))
                  .height(48.dp),
          testTag = "AnonymousLoginButton")
    }

    @Composable
    fun DefaultSignOutButton(onComplete: () -> Unit) {
      IconButton(Icons.Filled.Outbox, "Sign Out", onComplete)
    }
  }
}
