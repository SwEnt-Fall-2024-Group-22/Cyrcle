import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.user.UserRepository
import com.github.se.cyrcle.model.user.UserRepositoryFirestore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel(val userRepository: UserRepository) : ViewModel() {

  private val _user = MutableStateFlow<User?>(null)
  val user: StateFlow<User?> = _user

  fun fetchUserById(userId: String) {
    userRepository.getUserById(
        userId,
        onSuccess = { fetchedUser -> _user.value = fetchedUser },
        onFailure = { exception ->
          Log.e("UserViewModel", "Failed to fetch user by ID: $userId", exception)
        })
  }

  fun addUser(user: User) {
    userRepository.addUser(
        user,
        onSuccess = {},
        onFailure = { exception ->
          Log.e("UserViewModel", "Failed to add user: ${user.userId}", exception)
        })
  }

  fun updateUser(user: User) {
    userRepository.updateUser(
        user,
        onSuccess = {
          // Handle success, e.g., log or simple notification
        },
        onFailure = { exception ->
          Log.e("UserViewModel", "Failed to update user: ${user.userId}", exception)
        })
  }

  fun deleteUserById(userId: String) {
    userRepository.deleteUserById(
        userId,
        onSuccess = {
          // Handle success, e.g., log or notify
        },
        onFailure = { exception ->
          Log.e("UserViewModel", "Failed to delete user by ID: $userId", exception)
        })
  }
  /*
  fun updateUserLocation(userId: String, location: Location) {
    viewModelScope.launch {
      userRepository.updateUserLocation(
          userId,
          location,
          onSuccess = {
            // Handle success, e.g., log or notify
          },
          onFailure = { exception ->
            Log.e("UserViewModel", "Failed to update location for user: $userId", exception)
          })
    }
  }

  fun getLastKnownLocation(userId: String, onResult: (Location?) -> Unit) {
    viewModelScope.launch {
      userRepository.getLastKnownLocation(
          userId,
          onSuccess = { location -> onResult(location) },
          onFailure = { exception ->
            Log.e("UserViewModel", "Failed to get last known location for user: $userId", exception)
            onResult(null)
          })
    }
  }

  fun addFavoriteParking(userId: String, parkingId: String) {
      userRepository.addFavoriteParking(
          userId,
          parkingId,
          onSuccess = {

          },
          onFailure = { exception ->
            Log.e("UserViewModel", "Failed to add favorite parking for user: $userId", exception)
          })

  }

  fun removeFavoriteParking(userId: String, parkingId: String) {
      userRepository.removeFavoriteParking(
          userId,
          parkingId,
          onSuccess = {
            // Handle success, e.g., log or notify
          },
          onFailure = { exception ->
            Log.e("UserViewModel", "Failed to remove favorite parking for user: $userId", exception)
          })
  }


  fun getFavoriteParkings(userId: String, onResult: (List<String>) -> Unit) {
      userRepository.getFavoriteParkings(
          userId,
          onSuccess = { parkingIds -> onResult(parkingIds) },
          onFailure = { exception ->
            Log.e("UserViewModel", "Failed to get favorite parkings for user: $userId", exception)
            onResult(emptyList())
          })
  }


   */

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserViewModel(UserRepositoryFirestore(FirebaseFirestore.getInstance())) as T
          }
        }
  }
}
