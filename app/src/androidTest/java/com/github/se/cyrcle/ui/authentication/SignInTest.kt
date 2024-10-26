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
import com.github.se.cyrcle.ui.navigation.NavigationActions
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

@HiltAndroidTest
class LoginTest {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<MainActivity>()

  private lateinit var navigationHost: NavHostController
  private lateinit var navigationActions: NavigationActions

  // The IntentsTestRule is not reliable.

  @Before
  fun setUp() {
    Intents.init()

    navigationHost = mock(NavHostController::class.java)
    navigationActions = NavigationActions(navigationHost)
  }

  // Release Intents after each test
  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  fun titleAndButtonsAreCorrectlyDisplayed() {
    composeTestRule.activity.setContent { SignInScreen(navigationActions) }
    composeTestRule
        .onNodeWithTag("LoginTitle")
        .assertIsDisplayed()
        .assertTextEquals("Welcome to Cyrcle")

    composeTestRule.onNodeWithTag("GoogleLoginButton").assertIsDisplayed().assertHasClickAction()

    composeTestRule.onNodeWithTag("AnonymousLoginButton").assertIsDisplayed().assertHasClickAction()
  }

  @Test
  fun googleSignInReturnsValidActivityResult() {
    composeTestRule.activity.setContent { SignInScreen(navigationActions) }

    composeTestRule.onNodeWithTag("GoogleLoginButton").performClick()
    composeTestRule.waitForIdle()
    // assert that an Intent resolving to Google Mobile Services has been sent (for sign-in)
    intended(toPackage("com.google.android.gms"))
  }
}
