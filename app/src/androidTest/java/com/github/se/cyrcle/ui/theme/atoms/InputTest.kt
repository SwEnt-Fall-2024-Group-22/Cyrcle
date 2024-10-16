package com.github.se.cyrcle.ui.theme.atoms

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
        val tag = "InputText"

        val expected = "A"

        var text = "" // by remember { mutableStateOf("A") }
        composeTestRule.setContent { InputText(
            "Test",
            onValueChange = { newText -> text = newText },
            testTag = tag
            ) }

        composeTestRule.onNodeWithTag(tag)
            .assertIsDisplayed()
            .performTextInput(expected) // return Unit, can't chain further
        assertEquals(expected, text)
    }

    @Test
    fun inputTextDefaultArg() {
        composeTestRule.setContent { InputText(
            "Test",
            onValueChange = {},
        ) }

    }
}
