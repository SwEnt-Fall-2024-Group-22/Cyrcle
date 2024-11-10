package com.github.se.cyrcle.ui.authentication

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthenticatorImplTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var authenticator: Authenticator

  @Before
  fun setUp() {
    Intents.init()
    authenticator = AuthenticatorImpl(FirebaseAuth.getInstance())
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  fun testAuthenticateButton() {
    composeTestRule.setContent { authenticator.AuthenticateButton({}, {}) }

    composeTestRule
        .onNodeWithTag("AuthenticateButton")
        .assertExists()
        .assertHasClickAction()
        .performClick()
    composeTestRule.waitForIdle()
    // assert that an Intent resolving to Google Mobile Services has been sent (for sign-in)
    intended(toPackage("com.google.android.gms"))
  }

  @Test
  fun testAnonymousLoginButton() {
    var callbackCalled = false
    composeTestRule.setContent { authenticator.SignInAnonymouslyButton { callbackCalled = true } }

    composeTestRule
        .onNodeWithTag("AnonymousLoginButton")
        .assertExists()
        .assertHasClickAction()
        .performClick()

    assert(callbackCalled)
  }
}
