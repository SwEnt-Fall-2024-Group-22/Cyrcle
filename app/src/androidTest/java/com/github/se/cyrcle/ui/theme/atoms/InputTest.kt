package com.github.se.cyrcle.ui.theme.atoms

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InputTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun displayInputText() {
    val tagD = "InputText"
    val tag1 = "InputText1"
    val expected = "Some text"
    var text = ""

    composeTestRule.setContent {
      InputText("Test", onValueChange = { newText -> text = newText })
      InputText(
          "Test",
          Modifier,
          { newText -> text = newText },
          text,
          false,
          2,
          1,
          hasClearIcon = false,
          isError = false,
          testTag = tag1)
    }

    composeTestRule.onNodeWithTag(tagD).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tagD}Text", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tagD).performTextInput(expected)
    assertEquals(expected, text)

    text = ""
    composeTestRule.onNodeWithTag(tag1).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tag1}Text", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tag1).performTextInput(expected)
    assertEquals(expected, text)
  }

  @Test
  fun displayConditionCheckingInputText() {
    val tag = "ConditionCheckingInputText"
    val tag1 = "ConditionCheckingInputText1"
    val expected = "Some text"
    val text = mutableStateOf("")

    composeTestRule.setContent {
      ConditionCheckingInputText("Test", onValueChange = { newText -> text.value = newText })
      ConditionCheckingInputText(
          label = "Test",
          onValueChange = { newText -> text.value = newText },
          value = text.value,
          minCharacters = 2,
          maxCharacters = 10,
          hasClearIcon = true,
          testTag = tag1)
    }

    composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tag}Text", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tag).performTextInput(expected)
    assertEquals(expected, text.value)

    text.value = ""
    composeTestRule.onNodeWithTag(tag1).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tag1}Text", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tag1).performTextInput(expected)
    composeTestRule.onNodeWithTag("${tag1}ErrorRow").assertIsNotDisplayed()
    assertEquals(expected, text.value)

    composeTestRule.onNodeWithTag("${tag1}Clear").assertIsDisplayed().performClick()
    assertEquals("", text.value)
    // Check that the Error row is displayed and that the message is correct
    composeTestRule
        .onNodeWithTag("${tag1}Error")
        .assertIsDisplayed()
        .assertTextEquals("Minimum 2 characters required (current: 0)")
  }
}
