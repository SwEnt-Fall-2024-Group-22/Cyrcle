package com.github.se.cyrcle.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class ThemeTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Composable
  fun ColoredSquare(
      color: Color,
      testTag: String = "TestTag",
  ) {
    Box(modifier = Modifier.size(100.dp).background(color).testTag(testTag)) {}
  }

  @Test
  fun disabledColorTestLight() {
    composeTestRule.setContent { ColoredSquare(disabledColor()) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun disableColorTestDark() {
    composeTestRule.setContent { ColoredSquare(disabledColor()) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun defaultOnColorFromHighLuminance() {
    composeTestRule.setContent { ColoredSquare(defaultOnColorFromLuminance(White)) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color (should be Black)
  }

  @Test
  fun defaultOnColorFromLowLuminance() {
    composeTestRule.setContent { ColoredSquare(defaultOnColorFromLuminance(Black)) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color (should be White)
  }
}
