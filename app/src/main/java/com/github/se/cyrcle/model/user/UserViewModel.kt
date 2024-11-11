package com.github.se.cyrcle.model.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingRepository
import com.github.se.cyrcle.model.parking.ParkingRepositoryFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.maps.extension.style.expressions.dsl.generated.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class UserViewModel(
    private val userRepository: UserRepository,
    private val parkingRepository: ParkingRepository
) : ViewModel() {

  private val _currentUser = MutableStateFlow<User?>(null)
  val currentUser: StateFlow<User?> = _currentUser

  val isSignedIn: Flow<Boolean>
    get() = currentUser.map { it != null }

  /**
   * Sets the current user.
   *
   * @param user the user to set as the current user
   */
  fun setCurrentUser(user: User?) {
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
   * Gets a user by ID from the Firestore database and calls the onSuccess callback with the user.
   *
   * @param userId the ID of the user to get
   * @param onSuccess the callback to call with the user
   */
  fun getUserById(userId: String, onSuccess: (User) -> Unit) {
    Log.d("UserViewModel", "Getting user by ID: $userId")
    userRepository.getUserById(
        userId,
        onSuccess = { onSuccess(it) },
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
        onSuccess = {
          // set the current user by fetching the user from the database
          // can't use the user object passed in because it doesn't have the full details of the
          // user if the user is not a new user.
          userRepository.getUserById(
              user.public.userId, onSuccess = { setCurrentUser(it) }, onFailure = {})
        },
        onFailure = { exception ->
          Log.e(
              "com.github.se.cyrcle.model.user.UserViewModel",
              "Failed to add user: ${user.public.userId}",
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
        onSuccess = { Log.d("UserViewModel", "User updated") },
        onFailure = { exception ->
          Log.e(
              "com.github.se.cyrcle.model.user.UserViewModel",
              "Failed to update user: ${user.public.userId}",
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
      val updatedDetails =
          user.details?.copy(favoriteParkings = user.details.favoriteParkings + parkingId)
      val updatedUser = user.copy(details = updatedDetails)
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
      val updatedDetails =
          user.details?.copy(favoriteParkings = user.details.favoriteParkings - parkingId)
      val updatedUser = user.copy(details = updatedDetails)
      updateUser(updatedUser)
      setCurrentUser(updatedUser)
    }
  }

  /** Gets the favorite parkings of the current user and sets them in the favorite parkings state */
  fun getSelectedUserFavoriteParking() {
    currentUser.value?.details?.let { details ->
      parkingRepository.getParkingsByListOfIds(
          details.favoriteParkings,
          onSuccess = { _favoriteParkings.value = it },
          onFailure = { exception ->
            Log.e("UserViewModel", "Failed to fetch favorite parkings of current user", exception)
          })
    }
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
