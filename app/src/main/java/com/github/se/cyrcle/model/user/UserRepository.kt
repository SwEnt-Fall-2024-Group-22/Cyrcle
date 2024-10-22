package com.github.se.cyrcle.model.user

interface UserRepository {

  /**
   * Initialize the repository
   *
   * @param onSuccess a callback that is called when the repository is initialized
   */
  fun onSignIn(onSuccess: () -> Unit)

  /**
   * Get a new unique identifier for a parking
   *
   * @return a new unique identifier for a parking
   */
  fun getNewUid(): String

  /**
   * Get a user by their identifier
   *
   * @param userId the identifier of the user
   * @param onSuccess a callback that is called when the user is retrieved
   * @param onFailure a callback that is called when an error occurs
   */
  fun getUserById(userId: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Get all users
   *
   * @param onSuccess a callback that is called when the users are retrieved
   * @param onFailure a callback that is called when an error occurs
   */
  fun getAllUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit)

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

  /*

   /**
   * Get the contributed spots of a user
   *
   * @param userId the identifier of the user
   * @param onSuccess a callback that is called when the contributed spots are retrieved
   * @param onFailure a callback that is called when an error occurs
   */
  fun getUserContributedSpots(
      userId: String,
      onSuccess: (List<String>) -> Unit,
      onFailure: (Exception) -> Unit
  )

   */
}
