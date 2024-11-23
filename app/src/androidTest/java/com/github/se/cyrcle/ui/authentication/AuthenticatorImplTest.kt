package com.github.se.cyrcle.ui.authentication

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
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
    authenticator = AuthenticatorImpl(FirebaseAuth.getInstance())
  }

  @Test
  fun testAuthenticateButton() {
    composeTestRule.setContent { authenticator.AuthenticateButton({}, {}) }

    composeTestRule
        .onNodeWithTag("AuthenticateButton")
        .assertExists()
        .assertHasClickAction()
        .performClick()
  }

  @Test
  fun testAnonymousLoginButton() {
    var callbackCalled = false
    composeTestRule.setContent {
      authenticator.SignInAnonymouslyButton(Modifier) { callbackCalled = true }
    }

    composeTestRule
        .onNodeWithTag("AnonymousLoginButton")
        .assertExists()
        .assertHasClickAction()
        .performClick()

    assert(callbackCalled)
  }
}
