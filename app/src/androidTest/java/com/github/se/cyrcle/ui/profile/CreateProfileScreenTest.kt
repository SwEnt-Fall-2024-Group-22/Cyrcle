package com.github.se.cyrcle.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.AuthenticationRepositoryMock
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockUserRepository
import com.github.se.cyrcle.model.user.TestInstancesUser
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.TopLevelDestinations
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

@RunWith(AndroidJUnit4::class)
class CreateProfileScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockNavigationActions: NavigationActions
  private lateinit var mockAuthenticator: AuthenticationRepositoryMock
  private lateinit var userViewModel: UserViewModel

  private lateinit var mockUserRepository: MockUserRepository
  private lateinit var mockParkingRepository: MockParkingRepository
  private lateinit var mockImageRepository: MockImageRepository

  @Before
  fun setUp() {
    mockNavigationActions = mock(NavigationActions::class.java)
    mockAuthenticator = AuthenticationRepositoryMock()

    mockUserRepository = MockUserRepository()
    mockParkingRepository = MockParkingRepository()
    mockImageRepository = MockImageRepository()
    userViewModel = UserViewModel(mockUserRepository, mockParkingRepository, mockImageRepository)

    composeTestRule.setContent {
      CreateProfileScreen(mockNavigationActions, mockAuthenticator, userViewModel)
    }
  }

  @Test
  fun testCreateProfileScreenSuccess() {
    val testUser = TestInstancesUser.user1
    composeTestRule
        .onNodeWithTag("FirstNameField")
        .assertIsDisplayed()
        .performTextInput(testUser.details!!.firstName)

    composeTestRule
        .onNodeWithTag("LastNameField")
        .assertIsDisplayed()
        .performTextInput(testUser.details!!.lastName)

    composeTestRule
        .onNodeWithTag("UsernameField")
        .assertIsDisplayed()
        .performTextInput(testUser.public.username)

    composeTestRule.onNodeWithTag("AuthenticateButton", useUnmergedTree = true).performClick()

    verify(mockNavigationActions).navigateTo(TopLevelDestinations.MAP)

    assert(
        mockUserRepository.users.any {
          it.public.userId == testUser.public.userId &&
              it.details!!.firstName == testUser.details!!.firstName &&
              it.details!!.lastName == testUser.details!!.lastName &&
              it.public.username == testUser.public.username
        })
  }

  @Test
  fun testCreateProfileUserAuthFails() {
    val testUser = TestInstancesUser.newUser
    mockAuthenticator.testUser = null

    composeTestRule
        .onNodeWithTag("FirstNameField")
        .assertIsDisplayed()
        .performTextInput(testUser.details!!.firstName)

    composeTestRule
        .onNodeWithTag("LastNameField")
        .assertIsDisplayed()
        .performTextInput(testUser.details!!.lastName)

    composeTestRule
        .onNodeWithTag("UsernameField")
        .assertIsDisplayed()
        .performTextInput(testUser.public.username)

    composeTestRule.onNodeWithTag("AuthenticateButton", useUnmergedTree = true).performClick()

    verifyNoInteractions(mockNavigationActions)
    assert(
        mockUserRepository.users.none {
          it.public.userId == testUser.public.userId &&
              it.details!!.firstName == testUser.details!!.firstName &&
              it.details!!.lastName == testUser.details!!.lastName &&
              it.public.username == testUser.public.username
        })
  }
}
