package com.github.se.cyrcle.ui.authentication

import androidx.compose.runtime.Composable
import com.github.se.cyrcle.model.user.User

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
}
