package com.github.se.cyrcle.model.user

import UserViewModel
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.github.se.cyrcle.model.user.UserTestInstances.newUser
import com.github.se.cyrcle.model.user.UserTestInstances.updatedUser
import com.github.se.cyrcle.model.user.UserTestInstances.user1
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.CountDownLatch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
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
    // Act: Fetch a user by ID (make sure the user exists in Firestore)
    userViewModel.fetchUserById("user1")

    // Assert: Check if the user is updated correctly in the StateFlow
    val fetchedUser = userViewModel.user.first()
    assertNotNull(fetchedUser)
    assertEquals("user1", fetchedUser?.userId)
    assertEquals("john_doe", fetchedUser?.username)
  }

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
    // Act: Delete a user by ID
    userViewModel.deleteUserById("user1")

    // Fetch the user to ensure it was deleted
    userViewModel.fetchUserById("user1")
    val fetchedUser = userViewModel.user.first()

    // Assert that the user was deleted successfully
    assertNull(fetchedUser)
  }
}
