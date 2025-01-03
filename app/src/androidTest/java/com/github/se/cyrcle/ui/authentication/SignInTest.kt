package com.github.se.cyrcle.ui.authentication

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.MockAuthenticationRepository
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockUserRepository
import com.github.se.cyrcle.model.authentication.AuthenticationRepository
import com.github.se.cyrcle.model.image.ImageRepository
import com.github.se.cyrcle.model.parking.online.ParkingRepository
import com.github.se.cyrcle.model.user.TestInstancesUser
import com.github.se.cyrcle.model.user.UserRepository
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.TopLevelDestinations
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class SignInTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var userRepository: UserRepository
  private lateinit var parkingRepository: ParkingRepository
  private lateinit var imageRepository: ImageRepository
  private lateinit var authenticator: AuthenticationRepository
  private lateinit var userViewModel: UserViewModel

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)

    userRepository = MockUserRepository()
    parkingRepository = MockParkingRepository()
    imageRepository = MockImageRepository()
    authenticator = MockAuthenticationRepository()
    userViewModel = UserViewModel(userRepository, parkingRepository, imageRepository, authenticator)

    val mockAuthenticator = MockAuthenticationRepository()

    userRepository.addUser(TestInstancesUser.user1, {}, {})
    userViewModel.setCurrentUser(TestInstancesUser.user1)

    mockAuthenticator.testUser = TestInstancesUser.user1

    composeTestRule.setContent { SignInScreen(navigationActions, userViewModel) }
  }

  @Test
  fun testScreenDisplays() {
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("AppLogo").assertIsDisplayed()

    composeTestRule.onNodeWithTag("LoginTitle").assertIsDisplayed().assertTextEquals("Welcome to")

    composeTestRule.onNodeWithTag("AuthenticateButton").assertIsDisplayed()

    composeTestRule.onNodeWithTag("AnonymousLoginButton").assertIsDisplayed()
  }

  @Test
  fun testComponentsAndFunctionality() = runTest {
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag("AuthenticateButton")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()

    assert(userViewModel.isSignedIn.first())
    assertEquals(TestInstancesUser.user1, userViewModel.currentUser.first())

    verify(navigationActions).navigateTo(TopLevelDestinations.MAP)
  }
}
