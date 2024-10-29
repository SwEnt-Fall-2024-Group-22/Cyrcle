package com.github.se.cyrcle.model.user

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserViewModelTest {

  @Mock private lateinit var userRepositoryFirestore: UserRepositoryFirestore
  private lateinit var userViewModel: UserViewModel

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    userViewModel = UserViewModel(userRepositoryFirestore)
  }

  @Test
  fun addUserTest() {
    userViewModel.addUser(UserTestInstances.user1)
    // Check if the user was added to the repository
    verify(userRepositoryFirestore).addUser(eq(UserTestInstances.user1), any(), any())
  }

  @Test
  fun setCurrentUserTest() {
    userViewModel.setCurrentUser(UserTestInstances.user1)

    // Check if the user returned is the correct one
    assert(userViewModel.currentUser.value == UserTestInstances.user1)
  }

  @Test
  fun getUserByIdTest() {
    userViewModel.getUserById("user1")

    // Check if the user was fetched from the repository
    verify(userRepositoryFirestore).getUserById(eq("user1"), any(), any())
  }

  @Test
  fun updateUserTest() {
    userViewModel.updateUser(UserTestInstances.user1)

    // Check if the user was updated in the repository
    verify(userRepositoryFirestore).updateUser(eq(UserTestInstances.user1), any(), any())
  }

  @Test
  fun addFavoriteParkingToSelectedUserTest() {
    // Set the current user and add a favorite parking to the user
    val user = UserTestInstances.user1
    userViewModel.setCurrentUser(user)
    userViewModel.addFavoriteParkingToSelectedUser("Test_spot_3")

    // Create a copy of the user with the favorite parking added
    val updatedUser = user.copy(favoriteParkings = user.favoriteParkings + "Test_spot_3")

    // Check if the favorite parking was added to the selected user
    verify(userRepositoryFirestore).updateUser(eq(updatedUser), any(), any())

    // Check if the selected user is the updated user
    assert(userViewModel.currentUser.value == updatedUser)
  }

  @Test
  fun removeFavoriteParkingFromSelectedUserTest() {
    // Set the current user and remove a favorite parking from the user
    val user = UserTestInstances.user1
    userViewModel.setCurrentUser(user)
    userViewModel.removeFavoriteParkingFromSelectedUser("Test_spot_1")

    // Create a copy of the user with the favorite parking removed
    val updatedUser = user.copy(favoriteParkings = user.favoriteParkings - "Test_spot_1")

    // Check if the favorite parking was removed from the selected user
    verify(userRepositoryFirestore).updateUser(eq(updatedUser), any(), any())

    // Check if the selected user is the updated user
    assert(userViewModel.currentUser.value == updatedUser)
  }
}