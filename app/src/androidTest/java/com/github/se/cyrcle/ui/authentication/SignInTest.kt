package com.github.se.cyrcle.ui.authentication

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import com.github.se.cyrcle.MainActivity
import com.github.se.cyrcle.model.parking.ParkingRepository
import com.github.se.cyrcle.model.user.UserRepository
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@HiltAndroidTest
class LoginTest {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Mock lateinit var navigationHost: NavHostController
  @Mock lateinit var userRepository: UserRepository
  @Mock lateinit var parkingRepository: ParkingRepository

  private lateinit var navigationActions: NavigationActions
  private lateinit var userViewModel: UserViewModel

  // The IntentsTestRule is not reliable.

  @Before
  fun setUp() {
    Intents.init()
    MockitoAnnotations.openMocks(this)

    userViewModel = UserViewModel(userRepository, parkingRepository)
    navigationActions = NavigationActions(navigationHost)
  }

  // Release Intents after each test
  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  fun titleAndButtonsAreCorrectlyDisplayed() {
    composeTestRule.activity.setContent { SignInScreen(navigationActions, userViewModel) }
    composeTestRule
        .onNodeWithTag("LoginTitle")
        .assertIsDisplayed()
        .assertTextEquals("Welcome to Cyrcle")

    composeTestRule.onNodeWithTag("GoogleLoginButton").assertIsDisplayed().assertHasClickAction()

    composeTestRule.onNodeWithTag("AnonymousLoginButton").assertIsDisplayed().assertHasClickAction()
  }

  @Test
  fun googleSignInReturnsValidActivityResult() {
    composeTestRule.activity.setContent { SignInScreen(navigationActions, userViewModel) }

    composeTestRule.onNodeWithTag("GoogleLoginButton").performClick()
    composeTestRule.waitForIdle()
    // assert that an Intent resolving to Google Mobile Services has been sent (for sign-in)
    intended(toPackage("com.google.android.gms"))
  }
}
