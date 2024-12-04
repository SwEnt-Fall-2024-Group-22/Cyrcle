package com.github.se.cyrcle.model.authentication

/** Interface for a Authentication Repository */
interface AuthenticationRepository {

  /**
   * Signs in a User
   *
   * @param onSuccess a function to be called once the user is authenticated
   * @param onFailure a function to be called if the authentication fails
   */
  fun authenticate(onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Sign in a user anonymously
   *
   * @param onComplete a function to be called once the user is loged in anonymously
   */
  fun authenticateAnonymously(onComplete: () -> Unit = {}, onFailure: (Exception) -> Unit = {})

  /**
   * Sign out the user
   *
   * @param onComplete a function to be called once the user logs out
   */
  fun signOut(onComplete: () -> Unit)
}
