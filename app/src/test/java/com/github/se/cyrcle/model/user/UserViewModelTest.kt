package com.github.se.cyrcle.model.user

import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingRepository
import com.github.se.cyrcle.model.parking.TestInstancesParking
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserViewModelTest {

  @Mock private lateinit var userRepository: UserRepository
  @Mock private lateinit var parkingRepository: ParkingRepository
  private lateinit var userViewModel: UserViewModel

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    userViewModel = UserViewModel(userRepository, parkingRepository)
  }

  @Test
  fun userSignedIn() = runBlocking {
    assert(!userViewModel.isSignedIn.first())
    userViewModel.setCurrentUser(TestInstancesUser.user1)
    assert(userViewModel.isSignedIn.first())
  }

  @Test
  fun signInTest() {
    userViewModel.signIn(TestInstancesUser.user1)
    // Check if the user was added to the repository
    verify(userRepository).addUser(eq(TestInstancesUser.user1), any(), any())
  }

  @Test
  fun setCurrentUserTest() {
    userViewModel.setCurrentUser(TestInstancesUser.user1)

    // Check if the user returned is the correct one
    assert(userViewModel.currentUser.value == TestInstancesUser.user1)
  }

  @Test
  fun setCurrentUserByIdTest() {
    userViewModel.setCurrentUserById("user1")

    // Check if the user was fetched from the repository
    verify(userRepository).getUserById(eq("user1"), any(), any())
  }
  // Check that the user returned is the correct one
  // and that onSuccess is called.
  @Test
  fun getUserBydWithCallbackTest() {
    `when`(userRepository.getUserById(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[1] as (User) -> Unit
      onSuccess(TestInstancesUser.user1)
    }
    userViewModel.setCurrentUserById("user1") { assert(it == TestInstancesUser.user1) }
    verify(userRepository).getUserById(eq("user1"), any(), any())
  }

  @Test
  fun updateUserTest() {
    userViewModel.updateUser(TestInstancesUser.user1)

    // Check if the user was updated in the repository
    verify(userRepository).updateUser(eq(TestInstancesUser.user1), any(), any())
  }

  @Test
  fun addFavoriteParkingToSelectedUserTest() {
    // Set the current user and add a favorite parking to the user
    val user = TestInstancesUser.user1
    userViewModel.setCurrentUser(user)
    userViewModel.addFavoriteParkingToSelectedUser("Test_spot_3")

    // Create a copy of the user with the favorite parking added
    val updatedUser =
        user.copy(
            details =
                user.details?.copy(
                    favoriteParkings = user.details!!.favoriteParkings + "Test_spot_3"))

    // Check if the favorite parking was added to the selected user
    verify(userRepository).updateUser(eq(updatedUser), any(), any())

    // Check if the selected user is the updated user
    assert(userViewModel.currentUser.value == updatedUser)
  }

  @Test
  fun getUserFavoriteParkingsTest() {
    // Set the current user
    userViewModel.setCurrentUser(TestInstancesUser.user1)
    userViewModel.getSelectedUserFavoriteParking()
    // Check if the favorite parkings were fetched from the repository
    verify(parkingRepository)
        .getParkingsByListOfIds(
            eq(TestInstancesUser.user1.details?.favoriteParkings?.toList()) ?: emptyList(),
            any(),
            any())
  }

  @Test
  fun getFavoriteParkingsSetsState() {
    // Set the current user
    `when`(parkingRepository.getParkingsByListOfIds(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[1] as (List<Parking>) -> Unit
      onSuccess(listOf(TestInstancesParking.parking1, TestInstancesParking.parking2))
    }

    userViewModel.setCurrentUser(TestInstancesUser.user1)
    userViewModel.getSelectedUserFavoriteParking()
    userViewModel.favoriteParkings.value.let {
      assert(it.contains(TestInstancesParking.parking1))
      assert(it.contains(TestInstancesParking.parking2))
    }
  }

  @Test
  fun removeFavoriteParkingFromSelectedUserTest() {
    // Set the current user and remove a favorite parking from the user
    val user = TestInstancesUser.user1
    userViewModel.setCurrentUser(user)
    userViewModel.removeFavoriteParkingFromSelectedUser("Test_spot_1")

    // Create a copy of the user with the favorite parking removed
    val updatedUser =
        user.copy(
            details =
                user.details?.copy(
                    favoriteParkings = user.details!!.favoriteParkings - "Test_spot_1"))

    // Check if the favorite parking was removed from the selected user
    verify(userRepository).updateUser(eq(updatedUser), any(), any())

    // Check if the selected user is the updated user
    assert(userViewModel.currentUser.value == updatedUser)
  }
}
