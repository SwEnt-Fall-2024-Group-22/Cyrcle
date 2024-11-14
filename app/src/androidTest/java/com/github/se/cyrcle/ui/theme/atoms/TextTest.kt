package com.github.se.cyrcle.ui.theme.atoms

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.text.style.TextAlign
import com.github.se.cyrcle.ui.theme.White
import org.junit.Rule
import org.junit.Test

class TextTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun textTest() {
    val text = "Text"
    val tagD = "Text"
    val tag1 = "Text1"

    composeTestRule.setContent {
      Text(text)
      Text(text, Modifier, MaterialTheme.typography.displayMedium, White, TextAlign.Center, tag1)
    }

    composeTestRule.onNodeWithTag(tagD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tag1).assertIsDisplayed()
  }
}
