package com.github.se.cyrcle.ui.authentication

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class LoginTest : TestCase() {

  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    Intents.init()
    navigationActions = mock(NavigationActions::class.java)
  }

  // Release Intents after each test
  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  fun titleAndButtonAreCorrectlyDisplayed() {
    composeTestRule.setContent { SignInScreen(navigationActions) }

    composeTestRule.onNodeWithTag("loginTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginTitle").assertTextEquals("Welcome to Cyrcle")

    composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").assertHasClickAction()
  }

  @Test
  fun googleSignInReturnsValidActivityResult() {
    composeTestRule.setContent { SignInScreen(navigationActions) }

    composeTestRule.onNodeWithTag("loginButton").performClick()
    composeTestRule.waitForIdle()
    // assert that an Intent resolving to Google Mobile Services has been sent (for sign-in)
    intended(toPackage("com.google.android.gms"))
  }
}
