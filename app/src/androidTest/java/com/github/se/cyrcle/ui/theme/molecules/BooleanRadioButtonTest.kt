package com.github.se.cyrcle.ui.theme.molecules

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

class BooleanRadioButtonTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun booleanRadioButtonTest() {
    val tagD = "BooleanRadioButton"
    val tag1 = "BooleanRadioButton1"

    val a1 = mutableStateOf(false)
    val a2 = mutableStateOf(false)

    composeTestRule.setContent {
      BooleanRadioButton("Q1 ?", a1)
      BooleanRadioButton("Q2 ?", a2, tag1)
    }

    composeTestRule.onNodeWithTag(tagD, true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tagD}Question").assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tagD}YesRadioButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tagD}YesText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tagD}NoRadioButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tagD}NoText").assertIsDisplayed()
  }
}
