package com.github.se.cyrcle.ui.authentication

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthenticatorDefaultTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testDefaultAuthenticationBtn() {
    var clicked = false
    composeTestRule.setContent { Authenticator.DefaultAuthenticateButton { clicked = true } }

    composeTestRule
        .onNodeWithTag("AuthenticateButton")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()
    assert(clicked)
  }

  @Test
  fun testDefaultAnonymousBtn() {
    var clicked = false
    composeTestRule.setContent { Authenticator.DefaultAnonymousLoginButton { clicked = true } }

    composeTestRule
        .onNodeWithTag("AnonymousLoginButton")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()
    assert(clicked)
  }
}
