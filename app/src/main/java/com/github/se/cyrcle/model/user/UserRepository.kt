package com.github.se.cyrcle.model.user

interface UserRepository {

  /**
   * Initialize the repository
   *
   * @param onSuccess a callback that is called when the repository is initialized
   */
  fun onSignIn(onSuccess: () -> Unit)

  /**
   * Get a a unique identifier for a user
   *
   * @return A UID linked to the user's signed in account
   */
  fun getUid(): String

  /**
   * Get a user by their identifier
   *
   * @param userId the identifier of the user
   * @param onSuccess a callback that is called when the user is retrieved
   * @param onFailure a callback that is called when an error occurs
   */
  fun getUserById(userId: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Add a user
   *
   * @param user the user to add
   * @param onSuccess a callback that is called when the user is added
   * @param onFailure a callback that is called when an error occurs
   */
  fun addUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Update a user
   *
   * @param user the user to update
   * @param onSuccess a callback that is called when the user is updated
   * @param onFailure a callback that is called when an error occurs
   */
  fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Delete a user by their identifier
   *
   * @param userId the identifier of the user to delete
   * @param onSuccess a callback that is called when the user is deleted
   * @param onFailure a callback that is called when an error occurs
   */
  fun deleteUserById(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
