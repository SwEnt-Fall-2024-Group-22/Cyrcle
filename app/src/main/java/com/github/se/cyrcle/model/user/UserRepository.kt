package com.github.se.cyrcle.model.user

import com.github.se.cyrcle.model.parking.Location
import com.github.se.cyrcle.model.parking.Parking

interface UserRepository {

    /**
     * Initialize the repository
     *
     * @param onSuccess a callback that is called when the repository is initialized
     */
    fun onSignIn(onSuccess: () -> Unit = {})

    /**
     * Get a user by their identifier
     *
     * @param userId the identifier of the user
     * @param onSuccess a callback that is called when the user is retrieved
     * @param onFailure a callback that is called when an error occurs
     */
    fun getUserById(
        userId: String,
        onSuccess: (User) -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    )

    /**
     * Get all users
     *
     * @param onSuccess a callback that is called when the users are retrieved
     * @param onFailure a callback that is called when an error occurs
     */
    fun getAllUsers(onSuccess: (List<User>) -> Unit = {}, onFailure: (Exception) -> Unit = {})

    /**
     * Add a user
     *
     * @param user the user to add
     * @param onSuccess a callback that is called when the user is added
     * @param onFailure a callback that is called when an error occurs
     */
    fun addUser(user: User, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {})

    /**
     * Update a user
     *
     * @param user the user to update
     * @param onSuccess a callback that is called when the user is updated
     * @param onFailure a callback that is called when an error occurs
     */
    fun updateUser(user: User, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {})

    /**
     * Delete a user by their identifier
     *
     * @param userId the identifier of the user to delete
     * @param onSuccess a callback that is called when the user is deleted
     * @param onFailure a callback that is called when an error occurs
     */
    fun deleteUserById(
        userId: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    )
    /* Refer to User.kt's comments for explanation of non-implementation
      /**
       * Update user location
       *
       * @param userId the identifier of the user
       * @param location the new location
       * @param onSuccess a callback that is called when the location is updated
       * @param onFailure a callback that is called when an error occurs
       */
      fun updateUserLocation(
          userId: String,
          location: Location,
          onSuccess: () -> Unit = {},
          onFailure: (Exception) -> Unit = {}
      )

      /**
       * Get user's location history
       *
       * @param userId the identifier of the user
       * @param onSuccess a callback that is called when the location history is retrieved
       * @param onFailure a callback that is called when an error occurs
       */
      fun getLastKnownLocation(
          userId: String,
          onSuccess: (Location?) -> Unit,
          onFailure: (Exception) -> Unit
      ) */

    /**
     * Add a preferred location for the user
     *
     * @param userId the identifier of the user
     * @param parking the location to add
     * @param onSuccess a callback that is called when the location is added
     * @param onFailure a callback that is called when an error occurs
     */
    fun addFavoriteParking(
        userId: String,
        parkingId: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    )

    /**
     * Remove a preferred location for the user
     *
     * @param userId the identifier of the user
     * @param parking the location to remove
     * @param onSuccess a callback that is called when the location is removed
     * @param onFailure a callback that is called when an error occurs
     */
    fun removeFavoriteParking(
        userId: String,
        parkingId: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    )

    /**
     * Get user's preferred locations
     *
     * @param userId the identifier of the user
     * @param onSuccess a callback that is called when the preferred locations are retrieved
     * @param onFailure a callback that is called when an error occurs
     */
    fun getFavoriteParkings(
        userId: String,
        onSuccess: (List<String>) -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    )

    /**
     * Get user-contributed parking spots
     *
     * @param userId the identifier of the user
     * @param onSuccess a callback that is called when the spots are retrieved
     * @param onFailure a callback that is called when an error occurs
     */
    fun getUserContributedSpots(
        userId: String,
        onSuccess: (List<String>) -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    )
}