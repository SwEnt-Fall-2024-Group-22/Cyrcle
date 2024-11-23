package com.github.se.cyrcle.model.authentication

import com.github.se.cyrcle.model.user.User

/**
 * Interface for a Composable repository It provides buttons with callbacks once authentication is
 * complete This does not interface with the UserRepository or ViewModel
 */
interface AuthenticationRepository {

  /**
   * Provides a Function capable of authenticating the user
   *
   * @return A function taking two callbacks:
   * 1. A callback to be called with the newly signed in user when authentication is successful
   * 2. A callback to be called when authentication fails
   */
  fun getAuthenticationCallback(): ((User) -> Unit, (Exception) -> Unit) -> Unit

  /**
   * Provides a Function capable of authenticating the user anonymously
   *
   * @return A function taking a callback to be called once anonymous authentication is successful
   */
  fun getAnonymousAuthenticationCallback(): (() -> Unit) -> Unit

  /**
   * Provides a Function capable of signing out the user
   *
   * @return A function taking a callback to be called once the user is signed out
   */
  fun getSignOutCallback(): (() -> Unit) -> Unit
}
