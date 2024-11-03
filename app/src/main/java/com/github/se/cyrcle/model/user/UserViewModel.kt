package com.github.se.cyrcle.model.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingRepository
import com.github.se.cyrcle.model.parking.ParkingRepositoryFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel(
    private val userRepository: UserRepository,
    private val parkingRepository: ParkingRepository
) : ViewModel() {

  private val _currentUser = MutableStateFlow<User?>(null)
  val currentUser: StateFlow<User?> = _currentUser

  /**
   * Sets the current user.
   *
   * @param user the user to set as the current user
   */
  fun setCurrentUser(user: User) {
    _currentUser.value = user
  }

  private val _favoriteParkings = MutableStateFlow<List<Parking>>(emptyList())
  val favoriteParkings: StateFlow<List<Parking>> = _favoriteParkings

  /**
   * Gets a user by ID from the Firestore database and sets it as the current user.
   *
   * @param userId the ID of the user to get
   */
  fun getUserById(userId: String) {
    userRepository.getUserById(
        userId,
        onSuccess = { setCurrentUser(it) },
        onFailure = { exception ->
          Log.e(
              "com.github.se.cyrcle.model.user.UserViewModel",
              "Failed to fetch user by ID: $userId",
              exception)
        })
  }

  /**
   * Adds a user to the Firestore database.
   *
   * @param user the user to add
   */
  fun addUser(user: User) {
    userRepository.addUser(
        user,
        onSuccess = {},
        onFailure = { exception ->
          Log.e(
              "com.github.se.cyrcle.model.user.UserViewModel",
              "Failed to add user: ${user.userId}",
              exception)
        })
  }

  /**
   * Updates a user in the Firestore database.
   *
   * @param user the user to update
   */
  fun updateUser(user: User) {
    userRepository.updateUser(
        user,
        onSuccess = {},
        onFailure = { exception ->
          Log.e(
              "com.github.se.cyrcle.model.user.UserViewModel",
              "Failed to update user: ${user.userId}",
              exception)
        })
  }

  /**
   * Adds parking to the list of favorite parkings for the selected user.
   *
   * @param parkingId the ID of the parking to add to the list of favorite parkings
   */
  fun addFavoriteParkingToSelectedUser(parkingId: String) {
    currentUser.value?.let { user ->
      val updatedUser = user.copy(favoriteParkings = user.favoriteParkings + parkingId)
      updateUser(updatedUser)
      setCurrentUser(updatedUser)
    }
  }

  /**
   * Removes parking from the list of favorite parkings for the selected user.
   *
   * @param parkingId the ID of the parking to remove from the list of favorite parkings
   */
  fun removeFavoriteParkingFromSelectedUser(parkingId: String) {
    currentUser.value?.let { user ->
      val updatedUser = user.copy(favoriteParkings = user.favoriteParkings - parkingId)
      updateUser(updatedUser)
      setCurrentUser(updatedUser)
    }
  }

  /** Gets the favorite parkings of the current user and sets them in the favorite parkings state */
  fun getSelectedUserFavoriteParking() {
    parkingRepository.getParkingsByListOfIds(
        currentUser.value!!.favoriteParkings,
        onSuccess = { _favoriteParkings.value = it },
        onFailure = { exception ->
          Log.e("UserViewModel", "Failed to fetch favorite parkings of current user", exception)
        })
  }

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserViewModel(
                UserRepositoryFirestore(
                    FirebaseFirestore.getInstance(), FirebaseAuth.getInstance()),
                ParkingRepositoryFirestore(FirebaseFirestore.getInstance()))
                as T
          }
        }
  }
}
