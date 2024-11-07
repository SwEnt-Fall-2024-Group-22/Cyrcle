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

class ColorTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Composable
  fun ColoredSquare(
      color: Color,
      colorScheme: ColorScheme = LightColorScheme,
      testTag: String = "TestTag",
  ) {
    Box(modifier = Modifier.size(2.dp).background(color).testTag(testTag)) {}
  }

  @Test
  fun getColorPrimaryLightTest() {
    composeTestRule.setContent { ColoredSquare(getColor(ColorLevel.PRIMARY), LightColorScheme) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getColorSecondaryLightTest() {
    composeTestRule.setContent { ColoredSquare(getColor(ColorLevel.SECONDARY), LightColorScheme) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getColorTertiaryLightTest() {
    composeTestRule.setContent { ColoredSquare(getColor(ColorLevel.TERTIARY), LightColorScheme) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getColorErrorLightTest() {
    composeTestRule.setContent { ColoredSquare(getColor(ColorLevel.ERROR), LightColorScheme) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getColorPrimaryDarkTest() {
    composeTestRule.setContent { ColoredSquare(getColor(ColorLevel.PRIMARY), DarkColorScheme) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getColorSecondaryDarkTest() {
    composeTestRule.setContent { ColoredSquare(getColor(ColorLevel.SECONDARY), DarkColorScheme) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getColorTertiaryDarkTest() {
    composeTestRule.setContent { ColoredSquare(getColor(ColorLevel.TERTIARY), DarkColorScheme) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getColorErrorDarkTest() {
    composeTestRule.setContent { ColoredSquare(getColor(ColorLevel.ERROR), DarkColorScheme) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnColorPrimaryLightTest() {
    composeTestRule.setContent { ColoredSquare(getOnColor(ColorLevel.PRIMARY), LightColorScheme) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnColorSecondaryLightTest() {
    composeTestRule.setContent { ColoredSquare(getOnColor(ColorLevel.SECONDARY), LightColorScheme) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnColorTertiaryLightTest() {
    composeTestRule.setContent { ColoredSquare(getOnColor(ColorLevel.TERTIARY), LightColorScheme) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnColorErrorLightTest() {
    composeTestRule.setContent { ColoredSquare(getOnColor(ColorLevel.ERROR), LightColorScheme) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnColorPrimaryDarkTest() {
    composeTestRule.setContent { ColoredSquare(getOnColor(ColorLevel.PRIMARY), DarkColorScheme) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnColorSecondaryDarkTest() {
    composeTestRule.setContent { ColoredSquare(getOnColor(ColorLevel.SECONDARY), DarkColorScheme) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnColorTertiaryDarkTest() {
    composeTestRule.setContent { ColoredSquare(getOnColor(ColorLevel.TERTIARY), DarkColorScheme) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnColorErrorDarkTest() {
    composeTestRule.setContent { ColoredSquare(getOnColor(ColorLevel.ERROR), DarkColorScheme) }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getContainerColorPrimaryLightTest() {
    composeTestRule.setContent {
      ColoredSquare(getContainerColor(ColorLevel.PRIMARY), LightColorScheme)
    }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getContainerColorSecondaryLightTest() {
    composeTestRule.setContent {
      ColoredSquare(getContainerColor(ColorLevel.SECONDARY), LightColorScheme)
    }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getContainerColorTertiaryLightTest() {
    composeTestRule.setContent {
      ColoredSquare(getContainerColor(ColorLevel.TERTIARY), LightColorScheme)
    }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getContainerColorErrorLightTest() {
    composeTestRule.setContent {
      ColoredSquare(getContainerColor(ColorLevel.ERROR), LightColorScheme)
    }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getContainerColorPrimaryDarkTest() {
    composeTestRule.setContent {
      ColoredSquare(getContainerColor(ColorLevel.PRIMARY), DarkColorScheme)
    }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getContainerColorSecondDarkTest() {
    composeTestRule.setContent {
      ColoredSquare(getContainerColor(ColorLevel.SECONDARY), DarkColorScheme)
    }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getContainerColorTertiaryDarkTest() {
    composeTestRule.setContent {
      ColoredSquare(getContainerColor(ColorLevel.TERTIARY), DarkColorScheme)
    }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color

  }

  @Test
  fun getContainerColorErrorDarkTest() {
    composeTestRule.setContent {
      ColoredSquare(getContainerColor(ColorLevel.ERROR), DarkColorScheme)
    }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnContainerColorPrimaryLightTest() {
    composeTestRule.setContent {
      ColoredSquare(getOnContainerColor(ColorLevel.PRIMARY), LightColorScheme)
    }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnContainerColorSecondaryLightTest() {
    composeTestRule.setContent {
      ColoredSquare(getOnContainerColor(ColorLevel.SECONDARY), LightColorScheme)
    }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnContainerColorTertiaryLightTest() {
    composeTestRule.setContent {
      ColoredSquare(getOnContainerColor(ColorLevel.TERTIARY), LightColorScheme)
    }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnContainerColorErrorLightTest() {
    composeTestRule.setContent {
      ColoredSquare(getOnContainerColor(ColorLevel.ERROR), LightColorScheme)
    }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnContainerColorPrimaryDarkTest() {
    composeTestRule.setContent {
      ColoredSquare(getOnContainerColor(ColorLevel.PRIMARY), DarkColorScheme)
    }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnContainerColorSecondaryDarkTest() {
    composeTestRule.setContent {
      ColoredSquare(getOnContainerColor(ColorLevel.SECONDARY), DarkColorScheme)
    }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnContainerColorTertiaryDarkTest() {
    composeTestRule.setContent {
      ColoredSquare(getOnContainerColor(ColorLevel.TERTIARY), DarkColorScheme)
    }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }

  @Test
  fun getOnContainerColorErrorDarkTest() {
    composeTestRule.setContent {
      ColoredSquare(getOnContainerColor(ColorLevel.ERROR), DarkColorScheme)
    }
    composeTestRule.onNodeWithTag("TestTag").assertIsDisplayed()
    // No test for checking the color
  }
}
