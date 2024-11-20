package com.github.se.cyrcle.model.user

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.cyrcle.model.image.ImageRepository
import com.github.se.cyrcle.model.image.ImageRepositoryCloudStorage
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingRepository
import com.github.se.cyrcle.model.parking.ParkingRepositoryFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class UserViewModel(
    private val userRepository: UserRepository,
    private val parkingRepository: ParkingRepository,
    private val imageRepository: ImageRepository? = null
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
    Log.d("UserViewModel", "Setting the current user in viewmodel : $user")
    _currentUser.value = user
  }

  private val _favoriteParkings = MutableStateFlow<List<Parking>>(emptyList())
  val favoriteParkings: StateFlow<List<Parking>> = _favoriteParkings

  /** Signs out the current user by setting the state to null */
  fun signOut() {
    setCurrentUser(null)
  }

  /**
   * Set the current user by fetching the user from the Firestore database with the given ID.
   *
   * @param userId the ID of the user to get
   */
  fun setCurrentUserById(userId: String) {
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
   * Gets a user by ID from the Firestore database.. Fetches the profile picture url from the cloud
   * storage and updates the user object with before calling the callback.
   *
   * @param userId the ID of the user to get
   * @param onSuccess the callback to call with the user
   */
  fun getUserById(userId: String, onSuccess: (User) -> Unit) {
    Log.d("UserViewModel", "Getting the current user by ID: $userId")
    userRepository.getUserById(
        userId,
        onSuccess = {
          transformPathToUrl(
              it,
              onSuccess = onSuccess,
              onFailure = { Log.e("UserViewModel", "Failed to transform path to url") })
        },
        onFailure = { exception ->
          Log.e(
              "com.github.se.cyrcle.model.user.UserViewModel",
              "Failed to fetch user by ID: $userId",
              exception)
        })
  }

  /**
   * Sign in a User : If he doesn't have an account on the database, it will create a new user. If
   * he does we retrieve the user from the database and set it as the current user.
   *
   * @param user the user to add
   */
  fun signIn(user: User) {
    // Set the user in the viewmodel even if it's incomplete as we need the ID to match to call
    // updateUser()
    // setCurrentUser(user)
    userRepository.addUser(
        user,
        onSuccess = { getUserById(user.public.userId) { setCurrentUser(it) } },
        onFailure = { exception ->
          Log.e(
              "com.github.se.cyrcle.model.user.UserViewModel",
              "Failed to add user: ${user.public.userId}",
              exception)
        })
  }

  /**
   * Updates a user in the Firestore database. If a user has Uri for the profile picture, it will
   * upload the image to the cloud storage and then delete the Uri.
   *
   * @param user the user to update
   * @param context the context used to resolve the image uri (if null the profile picture will not
   *   upload)
   * @param onSuccess the callback to call with the updated user
   */
  fun updateUser(user: User, context: Context? = null, onSuccess: () -> Unit = {}) {
    if (user.public.userId != currentUser.value?.public?.userId) {
      Log.e("UserViewModel", "Trying to update a user that is not the current user")
      return
    }

    Log.d("UserViewModel", "Updating user: $user")
    // First update the user object in the view model.
    setCurrentUser(user)
    // Then update the user object in the database
    if (user.localSession?.profilePictureUri.isNullOrBlank() || context == null) {
      userRepository.updateUser(
          user,
          onSuccess = {
            Log.d("UserViewModel", "User updated in the database")
            onSuccess()
          },
          onFailure = { exception ->
            Log.e(
                "com.github.se.cyrcle.model.user.UserViewModel",
                "Failed to update user: ${user.public.userId}",
                exception)
          })
    } else {
      Log.d("UserViewModel", "User has a profile picture to upload")
      // Even if the upload fails, uploadProfilePicture will still call updateUser to back up the
      // updated User.
      uploadProfilePicture(context)
    }
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

  /**
   * Upload the picture to the cloud storage update the user object in the database with the cloud
   * path created. update the user object in the viewmodel with the url of the image and the cloud
   * path
   *
   * @param context the context used to resolve the image uri
   */
  private fun uploadProfilePicture(context: Context) {
    if (imageRepository == null) {
      Log.e("UserViewModel", "ImageRepository is null, should not upload image")
      return
    }
    if (currentUser.value == null) {
      Log.e("UserViewModel", "Current user is null, should not upload image")
      return
    }
    if (currentUser.value?.localSession?.profilePictureUri == null) {
      Log.e("UserViewModel", "Profile picture uri is null, should not upload image")
      return
    }

    val user = currentUser.value!!
    val fileUri = user.localSession!!.profilePictureUri!!
    val destinationPath = "profile_pictures/${user.public.userId}"
    imageRepository.uploadImage(
        context = context,
        fileUri = fileUri,
        destinationPath = destinationPath,
        onSuccess = { url ->
          Log.d("UserViewModel", "Image uploaded successfully to $url")
          val updatedUser =
              user.copy(
                  public = user.public.copy(profilePictureCloudPath = destinationPath),
                  localSession =
                      user.localSession.copy(profilePictureUri = null, profilePictureUrl = url))
          updateUser(updatedUser)
        },
        onFailure = {
          val updatedUser =
              user.copy(localSession = user.localSession.copy(profilePictureUri = null))
          updateUser(updatedUser)
          Log.e("UserViewModel", "Failed to upload image, still updating user without the picture")
        })
  }

  /**
   * Updates the user object with the profile picture url fetched from the repo.
   *
   * @param user the user object to update (doesn't have to be the current user)
   * @param onSuccess the callback to call with the updated user
   * @param onFailure the callback to call if the image fetching fails
   */
  private fun transformPathToUrl(user: User, onSuccess: (User) -> Unit, onFailure: () -> Unit) {
    if (imageRepository == null) {
      Log.e("UserViewModel", "ImageRepository is null")
      return
    }
    val cloudPath = user.public.profilePictureCloudPath
    if (cloudPath.isBlank()) {
      Log.d("UserViewModel", "No image to fetch for user")
      onSuccess(user) // No image to fetch
      return
    }

    Log.d("UserViewModel", "Getting the url for the image at $cloudPath")
    imageRepository.getUrl(
        cloudPath,
        onSuccess = { url ->
          Log.d("UserViewModel", "Got the url for the image: $url")
          val updatedUser =
              user.copy(
                  localSession =
                      user.localSession?.copy(profilePictureUrl = url)
                          ?: LocalSession(profilePictureUrl = url))
          onSuccess(updatedUser)
        },
        onFailure = onFailure)
  }

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserViewModel(
                UserRepositoryFirestore(
                    FirebaseFirestore.getInstance(), FirebaseAuth.getInstance()),
                ParkingRepositoryFirestore(FirebaseFirestore.getInstance()),
                ImageRepositoryCloudStorage(
                    FirebaseAuth.getInstance(), FirebaseStorage.getInstance()))
                as T
          }
        }
  }
}
