import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.user.UserRepositoryFirestore
import com.github.se.cyrcle.model.user.user1
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UserViewModelTest {

  private lateinit var userViewModel: UserViewModel
  private lateinit var userRepositoryFirestore: UserRepositoryFirestore

  @Before
  fun setUp() = runTest {
    // Initialize Firebase and Firestore
    val context = ApplicationProvider.getApplicationContext<Context>()
    FirebaseApp.initializeApp(context)

    val firestore = FirebaseFirestore.getInstance()

    // Initialize the UserRepositoryFirestore with Firestore
    userRepositoryFirestore = UserRepositoryFirestore(firestore)

    // Initialize the UserViewModel with the real repository
    userViewModel = UserViewModel(userRepositoryFirestore)
    userViewModel.addUser(user1)
  }

  @Test
  fun fetchUserById_updates_selectedUser_on_success() = runTest {
    val countDownLatch = CountDownLatch(1)

    // Act: Fetch a user by ID (make sure the user exists in Firestore)
    userViewModel.fetchUserById("user1")

    // Wait for the asynchronous call to complete (up to 5 seconds)
    countDownLatch.await(5, TimeUnit.SECONDS)

    // Assert: Check if the user is updated correctly in the StateFlow
    val fetchedUser = userViewModel.user.first()
    assertNotNull(fetchedUser)
    assertEquals("user1", fetchedUser?.userId)
    assertEquals("john_doe", fetchedUser?.username)
  }

  val newUser =
      User(
          userId = "usr",
          username = "new_user",
          firstName = "New",
          lastName = "User",
          email = "newuser@example.com",
          profilePictureUrl = "",
          homeAddress = "123 Main St",
          favoriteParkingSpots = emptyList(),
          // lastKnownLocation = Location(Point.fromLngLat(0.0, 0.0)),
          // lastLoginTime = Timestamp(0,0),
          // accountCreationDate = Timestamp(0,0)
      )

  @Test
  fun addUser_adds_new_user_to_firestore() = runBlocking {
    val countDownLatch = CountDownLatch(1)
    userViewModel.userRepository.addUser(newUser, {}, {})
    userViewModel.userRepository.getUserById(
        newUser.userId,
        { user ->
          assert(user == newUser)
          countDownLatch.countDown()
        },
        { fail("Failed to get User") })

    countDownLatch.await()
  }

  // Act: Update an existing user
  val updatedUser =
      User(
          userId = "usr",
          username = "updaed_user",
          firstName = "Updated",
          lastName = "User",
          email = "updateduser@example.com",
          profilePictureUrl = "https://example.com/profile.png",
          homeAddress = "456 Updated St",
          favoriteParkingSpots = emptyList(),
          // lastKnownLocation = Location(Point.fromLngLat(0.0, 0.0)),
          // lastLoginTime = Timestamp(0,0),
          // accountCreationDate = Timestamp(0,0)
      )

  @Test
  fun updateUser_updates_user_data_in_firestore(): Unit = runBlocking {
    val countDownLatch = CountDownLatch(1)

    userViewModel.updateUser(updatedUser)
    userViewModel.userRepository.getUserById(
        updatedUser.userId,
        { newUser ->
          assert("updaed_user" == newUser.username)
          countDownLatch.countDown()
        },
        { fail("Failed to retrieve updated review") })
    countDownLatch.await()
  }

  @Test
  fun deleteUserById_removes_user_from_firestore() = runTest {
    val countDownLatch = CountDownLatch(1)

    // Act: Delete a user by ID
    userViewModel.deleteUserById("user1")

    // Wait for the asynchronous call to complete (up to 5 seconds)
    countDownLatch.await(5, TimeUnit.SECONDS)

    // Fetch the user to ensure it was deleted
    userViewModel.fetchUserById("user1")
    val fetchedUser = userViewModel.user.first()

    // Assert that the user was deleted successfully
    assertNull(fetchedUser)
  }
}
