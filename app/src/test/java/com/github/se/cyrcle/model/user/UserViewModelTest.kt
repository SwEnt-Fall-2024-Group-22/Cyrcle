package com.github.se.cyrcle.model.user

import com.github.se.cyrcle.model.authentication.AuthenticationRepository
import com.github.se.cyrcle.model.image.ImageRepository
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingRepository
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.google.firebase.firestore.util.Assert.fail
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
  @Mock private lateinit var imageRepository: ImageRepository
  @Mock private lateinit var authenticator: AuthenticationRepository
  private lateinit var userViewModel: UserViewModel

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    userViewModel = UserViewModel(userRepository, parkingRepository, imageRepository, authenticator)
  }

  @Test
  fun userSignedIn() = runBlocking {
    assert(!userViewModel.isSignedIn.first())
    userViewModel.setCurrentUser(TestInstancesUser.user1)
    assert(userViewModel.isSignedIn.first())
  }

  @Test
  fun signInTest() {
    userViewModel.signIn({}, {})
    // Check if the user was added to the repository
    verify(authenticator).authenticate(any(), any())
  }

  @Test
  fun setCurrentUserTest() {
    userViewModel.setCurrentUser(TestInstancesUser.user1)

    // Check if the user returned is the correct one
    assert(userViewModel.currentUser.value == TestInstancesUser.user1)
  }

  @Test
  fun testUserExists() {
    userViewModel.doesUserExist(TestInstancesUser.user1) {}

    // Check if the repository was called
    verify(userRepository).userExists(eq(TestInstancesUser.user1), any(), any())
  }

  @Test
  @Suppress("UNCHECKED_CAST")
  fun testAddUserSuccessful() {
    `when`(userRepository.addUser(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[1] as () -> Unit
      onSuccess()
    }

    var calledOnSuccess = false
    userViewModel.addUser(
        TestInstancesUser.user1,
        { calledOnSuccess = true },
        { fail("Call to onFailure was not expected") })

    // Check if the repository was called
    verify(userRepository).addUser(eq(TestInstancesUser.user1), any(), any())
    assert(calledOnSuccess)
  }

  @Test
  @Suppress("UNCHECKED_CAST")
  fun testAddUserError() {
    `when`(userRepository.addUser(any(), any(), any())).thenAnswer { invocation ->
      val onFailure = invocation.arguments[2] as (Exception) -> Unit
      onFailure(Exception())
    }

    var calledOnFailure = false
    userViewModel.addUser(
        TestInstancesUser.user1,
        { fail("Call to onSuccess was not expected") },
        { calledOnFailure = true })

    // Check if the repository was called
    verify(userRepository).addUser(eq(TestInstancesUser.user1), any(), any())
    assert(calledOnFailure)
  }

  @Test
  @Suppress("UNCHECKED_CAST")
  fun testAuthenticateSuccess() {
    `when`(authenticator.authenticate(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as (String) -> Unit
      onSuccess(TestInstancesUser.user1.public.userId)
    }

    var calledOnSuccess = false
    userViewModel.authenticate(
        {
          assert(it == TestInstancesUser.user1.public.userId)
          calledOnSuccess = true
        },
        { fail("Call to onFailure was not expected") })

    // Check if the repository was called
    verify(authenticator).authenticate(any(), any())
    assert(calledOnSuccess)
  }

  @Test
  @Suppress("UNCHECKED_CAST")
  fun testAuthenticateFailure() {
    `when`(authenticator.authenticate(any(), any())).thenAnswer { invocation ->
      val onFailure = invocation.arguments[1] as (Exception) -> Unit
      onFailure(Exception())
    }

    var calledOnFailure = false
    userViewModel.authenticate(
        { fail("Call to onSuccess was not expected") }, { calledOnFailure = true })

    // Check if the repository was called
    verify(authenticator).authenticate(any(), any())
    assert(calledOnFailure)
  }

  @Test
  @Suppress("UNCHECKED_CAST")
  fun testSignInSuccess() {
    val testUser = TestInstancesUser.user1
    `when`(authenticator.authenticate(any(), any())).thenAnswer { invocation ->
      val onComplete = invocation.arguments[0] as (String) -> Unit
      onComplete(testUser.public.userId)
    }
    `when`(userRepository.getUserById(eq(testUser.public.userId), any(), any())).thenAnswer {
        invocation ->
      val onSuccess = invocation.arguments[1] as (User) -> Unit
      onSuccess(testUser)
    }

    var calledOnComplete = false
    userViewModel.signIn(
        {
          calledOnComplete = true
          assert(it == testUser)
        },
        { fail("Call to onFailure was not expected") })

    // Check if the repository was called
    verify(authenticator).authenticate(any(), any())
    assert(calledOnComplete)
    assert(userViewModel.currentUser.value == testUser)
    assert(
        userViewModel.favoriteParkings.value.all { it.uid in testUser.details!!.favoriteParkings })
  }

  @Test
  @Suppress("UNCHECKED_CAST")
  fun testSignInAccountNotFound() {
    val testUser = TestInstancesUser.user1
    `when`(authenticator.authenticate(any(), any())).thenAnswer { invocation ->
      val onComplete = invocation.arguments[0] as (String) -> Unit
      onComplete(testUser.public.userId)
    }
    `when`(userRepository.getUserById(eq(testUser.public.userId), any(), any())).thenAnswer {
        invocation ->
      val onFailure = invocation.arguments[2] as (Exception) -> Unit
      onFailure(Exception())
    }

    var calledOnFailure = false
    userViewModel.signIn(
        { fail("Call to onFailure was not expected") },
        {
          calledOnFailure = true
          assert(it == UserViewModel.SignInFailureReason.ACCOUNT_NOT_FOUND)
        })

    // Check if the repository was called
    verify(authenticator).authenticate(any(), any())
    assert(calledOnFailure)
  }

  @Test
  @Suppress("UNCHECKED_CAST")
  fun testSignInAuthenticationFailed() {
    `when`(authenticator.authenticate(any(), any())).thenAnswer { invocation ->
      val onFailure = invocation.arguments[1] as (Exception) -> Unit
      onFailure(Exception())
    }

    var calledOnFailure = false
    userViewModel.signIn(
        { fail("Call to onFailure was not expected") },
        {
          calledOnFailure = true
          assert(it == UserViewModel.SignInFailureReason.ERROR)
        })

    // Check if the repository was called
    verify(authenticator).authenticate(any(), any())
    assert(calledOnFailure)
  }

  @Test
  @Suppress("UNCHECKED_CAST")
  fun testSignInAnonymously() {
    `when`(authenticator.authenticateAnonymously(any())).thenAnswer { invocation ->
      val onComplete = invocation.arguments[0] as () -> Unit
      onComplete()
    }

    var calledOnComplete = false
    userViewModel.signInAnonymously { calledOnComplete = true }

    // Check if the repository was called
    verify(authenticator).authenticateAnonymously(any())
    assert(calledOnComplete)
  }

  @Test
  @Suppress("UNCHECKED_CAST")
  fun testSignOut() {
    `when`(authenticator.signOut(any())).thenAnswer { invocation ->
      val onComplete = invocation.arguments[0] as () -> Unit
      onComplete()
    }

    var calledOnComplete = false
    userViewModel.signOut { calledOnComplete = true }

    // Check if the repository was called
    verify(authenticator).signOut(any())
    assert(calledOnComplete)
  }

  @Test
  fun setCurrentUserByIdTest() {
    userViewModel.setCurrentUserById("user1")

    // Check if the user was fetched from the repository
    verify(userRepository).getUserById(eq("user1"), any(), any())
  }

  @Suppress("UNCHECKED_CAST")
  @Test
  fun getUserBydWithCallbackTest() {
    `when`(userRepository.getUserById(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[1] as (User) -> Unit
      onSuccess(TestInstancesUser.user1)
    }
    // Check that the user returned is the correct one and that onSuccess is called.
    userViewModel.getUserById("user1", { assert(it == TestInstancesUser.user1) })
    verify(userRepository).getUserById(eq("user1"), any(), any())
  }

  @Test
  fun updateUserTest() {
    userViewModel.setCurrentUser(TestInstancesUser.user1)
    userViewModel.updateUser(TestInstancesUser.user1)

    // Check if the user was updated in the repository
    verify(userRepository).updateUser(eq(TestInstancesUser.user1), any(), any())
  }

  @Test
  fun addFavoriteParkingToSelectedUserTest() {
    // Set the current user and add a favorite parking to the user
    val user = TestInstancesUser.user1
    userViewModel.setCurrentUser(user)
    userViewModel.addFavoriteParkingToSelectedUser(TestInstancesParking.parking3)

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

  @Suppress("UNCHECKED_CAST")
  @Test
  fun getUserFavoriteParkingsTest() {
    // Define the behavior of the repository needed to sign in the user
    `when`(parkingRepository.getParkingsByListOfIds(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[1] as (List<Parking>) -> Unit
      onSuccess(listOf(TestInstancesParking.parking1, TestInstancesParking.parking2))
    }

    `when`(userRepository.addUser(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[1] as () -> Unit
      onSuccess()
    }

    `when`(userRepository.getUserById(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[1] as (User) -> Unit
      onSuccess(TestInstancesUser.user1)
    }

    `when`(authenticator.authenticate(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as (String) -> Unit
      onSuccess(TestInstancesUser.user1.public.userId)
    }
    // Set the current user
    userViewModel.signIn({}, {})
    // Check if the favorite parkings were fetched from the repository
    verify(parkingRepository)
        .getParkingsByListOfIds(
            eq(TestInstancesUser.user1.details?.favoriteParkings?.toList()) ?: emptyList(),
            any(),
            any())
  }

  @Suppress("UNCHECKED_CAST")
  @Test
  fun getFavoriteParkingsSetsState() {
    // Define the behavior of the repository needed to sign in the user
    `when`(parkingRepository.getParkingsByListOfIds(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[1] as (List<Parking>) -> Unit
      onSuccess(listOf(TestInstancesParking.parking1, TestInstancesParking.parking2))
    }

    `when`(userRepository.addUser(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[1] as () -> Unit
      onSuccess()
    }

    `when`(userRepository.getUserById(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[1] as (User) -> Unit
      onSuccess(TestInstancesUser.user1)
    }
    `when`(authenticator.authenticate(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as (String) -> Unit
      onSuccess(TestInstancesUser.user1.public.userId)
    }
    // Set the current user
    userViewModel.signIn({}, {})

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
    userViewModel.removeFavoriteParkingFromSelectedUser(TestInstancesParking.parking1)

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
