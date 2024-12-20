package com.github.se.cyrcle.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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

class ColorTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Composable
  fun ColoredSquare(
      color: Color,
      testTag: String = "TestTag",
  ) {
    Box(modifier = Modifier.size(2.dp).background(color).testTag(testTag)) {}
  }

  @Test
  fun getColorPrimaryLightTest() {
    composeTestRule.setContent { ColoredSquare(getColor(ColorLevel.PRIMARY)) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getColorSecondaryLightTest() {
    composeTestRule.setContent { ColoredSquare(getColor(ColorLevel.SECONDARY)) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getColorTertiaryLightTest() {
    composeTestRule.setContent { ColoredSquare(getColor(ColorLevel.TERTIARY)) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getColorErrorLightTest() {
    composeTestRule.setContent { ColoredSquare(getColor(ColorLevel.ERROR)) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnColorPrimaryLightTest() {
    composeTestRule.setContent { ColoredSquare(getOnColor(ColorLevel.PRIMARY)) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnColorSecondaryLightTest() {
    composeTestRule.setContent { ColoredSquare(getOnColor(ColorLevel.SECONDARY)) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnColorTertiaryLightTest() {
    composeTestRule.setContent { ColoredSquare(getOnColor(ColorLevel.TERTIARY)) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnColorErrorLightTest() {
    composeTestRule.setContent { ColoredSquare(getOnColor(ColorLevel.ERROR)) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getContainerColorPrimaryLightTest() {
    composeTestRule.setContent { ColoredSquare(getContainerColor(ColorLevel.PRIMARY)) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getContainerColorSecondaryLightTest() {
    composeTestRule.setContent { ColoredSquare(getContainerColor(ColorLevel.SECONDARY)) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getContainerColorTertiaryLightTest() {
    composeTestRule.setContent { ColoredSquare(getContainerColor(ColorLevel.TERTIARY)) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getContainerColorErrorLightTest() {
    composeTestRule.setContent { ColoredSquare(getContainerColor(ColorLevel.ERROR)) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnContainerColorPrimaryLightTest() {
    composeTestRule.setContent { ColoredSquare(getOnContainerColor(ColorLevel.PRIMARY)) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnContainerColorSecondaryLightTest() {
    composeTestRule.setContent { ColoredSquare(getOnContainerColor(ColorLevel.SECONDARY)) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnContainerColorTertiaryLightTest() {
    composeTestRule.setContent { ColoredSquare(getOnContainerColor(ColorLevel.TERTIARY)) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnContainerColorErrorLightTest() {
    composeTestRule.setContent { ColoredSquare(getOnContainerColor(ColorLevel.ERROR)) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }
}
