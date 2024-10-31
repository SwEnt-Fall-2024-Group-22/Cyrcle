package com.github.se.cyrcle.ui.theme.atoms

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TextTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun displayText() {
    val defaultTag = "Text"
    composeTestRule.setContent { Text("Test") }

    composeTestRule.onNodeWithTag(defaultTag).assertIsDisplayed()
  }
}
