package com.github.se.cyrcle.ui.theme.atoms

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.ui.theme.ColorLevel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ButtonTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun displayButton() {
    val tagD = "Button"
    val tag1 = "Button1"
    val enabled = mutableStateOf(true)
    var a = 1
    composeTestRule.setContent {
      Button("Button", { a++ })
      Button("Button", { a++ }, Modifier, ColorLevel.PRIMARY, enabled.value, tag1)
    }

    composeTestRule.onNodeWithTag(tagD).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tagD}Text", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tagD).performClick()
    assertEquals(2, a)
    enabled.value = false
    composeTestRule.onNodeWithTag(tagD).performClick() // Shouldn't trigger onClick
    assertEquals(2, a)

    enabled.value = true
    composeTestRule.onNodeWithTag(tag1).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tag1}Text", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tag1).performClick()
    assertEquals(3, a)
    enabled.value = false
    composeTestRule.onNodeWithTag(tag1).performClick() // Shouldn't trigger onClick
    assertEquals(3, a)
  }

  @Test
  fun displayToggleButton() {
    val tagD = "ToggleButton"
    val tag1 = "ToggleButton1"
    val value = mutableStateOf(true)
    composeTestRule.setContent {
      ToggleButton("Button", value)
      ToggleButton("Button", value, Modifier, ColorLevel.PRIMARY, tag1)
    }

    composeTestRule.onNodeWithTag(tagD).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tagD}Text", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tagD).performClick() // Turn to false
    assertEquals(false, value.value)
    composeTestRule.onNodeWithTag(tagD).performClick() // Turn back to true
    assertEquals(true, value.value)

    composeTestRule.onNodeWithTag(tag1).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tag1}Text", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tag1).performClick() // Turn to false
    assertEquals(false, value.value)
    composeTestRule.onNodeWithTag(tag1).performClick() // Turn back to true
    assertEquals(true, value.value)
  }

  @Test
  fun displayToggleButtonOverload() {
    val tagD = "ToggleButton"
    val tag1 = "ToggleButton1"
    val mutableState = mutableStateOf(true)
    val function = { mutableState.value = !mutableState.value }
    composeTestRule.setContent {
      ToggleButton("Button", mutableState.value, function)
      ToggleButton("Button", mutableState.value, function, Modifier, ColorLevel.PRIMARY, tag1)
    }

    composeTestRule.onNodeWithTag(tagD).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tagD}Text", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tagD).performClick() // Turn to false
    assertEquals(false, mutableState.value)
    composeTestRule.onNodeWithTag(tagD).performClick() // Turn back to true
    assertEquals(true, mutableState.value)

    composeTestRule.onNodeWithTag(tag1).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tag1}Text", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tag1).performClick() // Turn to false
    assertEquals(false, mutableState.value)
    composeTestRule.onNodeWithTag(tag1).performClick() // Turn back to true
    assertEquals(true, mutableState.value)
  }

  @Test
  fun displaySmallFloatingActionButton() {
    val tagD = "SmallFab"
    val tag1 = "SmallFab1"
    var a = 1
    composeTestRule.setContent {
      Row {
        SmallFloatingActionButton(Icons.Filled.Add, "", { a++ })
        SmallFloatingActionButton(Icons.Filled.Add, "", { a++ }, Modifier, ColorLevel.PRIMARY, tag1)
      }
    }

    composeTestRule.onNodeWithTag(tagD).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tagD}Icon", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tagD).performClick()
    assertEquals(a, 2)

    composeTestRule.onNodeWithTag(tag1).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tag1}Icon", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tag1).performClick()
    assertEquals(a, 3)
  }

  @Test
  fun displayFloatingActionButton() {
    val tagD = "Fab"
    val tag1 = "Fab1"
    var a = 1
    composeTestRule.setContent {
      Row {
        FloatingActionButton(Icons.Filled.Add, "", { a++ })
        FloatingActionButton(Icons.Filled.Add, "", { a++ }, Modifier, ColorLevel.PRIMARY, tag1)
      }
    }

    composeTestRule.onNodeWithTag(tagD).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tagD}Icon", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tagD).performClick()
    assertEquals(a, 2)

    composeTestRule.onNodeWithTag(tag1).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tag1}Icon", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tag1).performClick()
    assertEquals(a, 3)
  }

  @Test
  fun displayLargeFloatingActionButton() {
    val tagD = "LargeFab"
    val tag1 = "LargeFab1"
    var a = 1
    composeTestRule.setContent {
      Row {
        LargeFloatingActionButton(Icons.Filled.Add, "", { a++ })
        LargeFloatingActionButton(Icons.Filled.Add, "", { a++ }, Modifier, ColorLevel.PRIMARY, tag1)
      }
    }

    composeTestRule.onNodeWithTag(tagD).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tagD}Icon", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tagD).performClick()
    assertEquals(a, 2)

    composeTestRule.onNodeWithTag(tag1).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tag1}Icon", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tag1).performClick()
    assertEquals(a, 3)
  }

  @Test
  fun displayExtendedFloatingActionButton() {
    val tagD = "ExtendedFab"
    val tag1 = "ExtendedFab1"
    var a = 1
    composeTestRule.setContent {
      Row {
        ExtendedFloatingActionButton(Icons.Filled.Add, "", { a++ }, text = "Extended")
        ExtendedFloatingActionButton(
            Icons.Filled.Add, "", { a++ }, Modifier, "Extended", ColorLevel.PRIMARY, tag1)
      }
    }

    composeTestRule.onNodeWithTag(tagD).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tagD}Icon", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tagD).performClick()
    assertEquals(a, 2)

    composeTestRule.onNodeWithTag(tag1).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tag1}Icon", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tag1).performClick()
    assertEquals(a, 3)
  }

  @Test
  fun displayIconButton() {
    val tagD = "IconButton"
    val tag1 = "IconButton1"
    var a = 1
    composeTestRule.setContent {
      Row {
        IconButton(Icons.Filled.Add, "na", { a++ })
        IconButton(Icons.Filled.Add, "na", { a++ }, Modifier, false, ColorLevel.PRIMARY, tag1)
      }
    }

    composeTestRule.onNodeWithTag(tagD).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tagD}Icon", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tagD).performClick()
    assertEquals(a, 2)

    composeTestRule.onNodeWithTag(tag1).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tag1}Icon", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tag1).performClick()
    assertEquals(a, 2)
  }

  @Test
  fun displayRadioButton() {
    val tagD = "RadioButton"
    val tag1 = "RadioButton1"
    var a = false
    var b = false
    composeTestRule.setContent {
      Row {
        RadioButton(a, { a = true })
        RadioButton(b, { b = true }, Modifier, ColorLevel.PRIMARY, tag1)
      }
    }

    composeTestRule.onNodeWithTag(tagD, true).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tagD, true).performClick()
    assertEquals(a, true)

    composeTestRule.onNodeWithTag(tag1).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tag1).performClick()
    assertEquals(b, true)
  }

  @Test
  fun displayScoreStars_fullStars() {
    val tag = "StarButton"

    composeTestRule.setContent {
      ScoreStars(score = 3.0, maxStars = 5, text = "3.0", testTag = tag)
    }

    // Check for 3 full stars and 2 empty stars
    composeTestRule.onAllNodesWithContentDescription("Full Star").assertCountEquals(3)
    composeTestRule.onAllNodesWithContentDescription("Empty Star").assertCountEquals(2)
  }

  @Test
  fun displayScoreStars_halfStar() {
    val tag = "StarButton"

    composeTestRule.setContent {
      ScoreStars(score = 3.5, maxStars = 5, text = "3.5", testTag = tag)
    }

    // Check for 3 full stars, 1 half star, and 1 empty star
    composeTestRule.onAllNodesWithContentDescription("Full Star").assertCountEquals(3)
    composeTestRule.onAllNodesWithContentDescription("Half Star").assertCountEquals(1)
    composeTestRule.onAllNodesWithContentDescription("Empty Star").assertCountEquals(1)
  }

  @Test
  fun displayScoreStars_emptyStars() {
    val tag = "StarButton"

    composeTestRule.setContent {
      ScoreStars(score = 0.0, maxStars = 5, text = "0.0", testTag = tag)
    }

    // Check for 5 empty stars
    composeTestRule.onAllNodesWithContentDescription("Empty Star").assertCountEquals(5)
  }

  @Test
  fun displayScoreStars_maxStars() {
    val tag = "StarButton"

    composeTestRule.setContent {
      ScoreStars(score = 5.0, maxStars = 5, text = "5.0", testTag = tag)
    }

    // Check for 5 full stars
    composeTestRule.onAllNodesWithContentDescription("Full Star").assertCountEquals(5)
  }

  @Test
  fun displayScoreStars_withText() {
    val tag = "StarButton"

    composeTestRule.setContent {
      ScoreStars(score = 4.0, maxStars = 5, text = "4.0", testTag = tag)
    }

    // Check for 4 full stars, 1 empty star, and the text label "4.0"
    composeTestRule.onAllNodesWithContentDescription("Full Star").assertCountEquals(4)
    composeTestRule.onAllNodesWithContentDescription("Empty Star").assertCountEquals(1)

    // Verify the text label is displayed
    composeTestRule.onNodeWithText("4.0").assertExists()
  }

  @Test
  fun displayScoreStars_scaledStars() {
    val tag = "StarButton"

    composeTestRule.setContent {
      ScoreStars(score = 3.0, maxStars = 5, scale = 1.5f, text = "3.0", testTag = tag)
    }

    // Check for 3 full stars and confirm the presence of the component
    composeTestRule.onAllNodesWithContentDescription("Full Star").assertCountEquals(3)
  }
}
