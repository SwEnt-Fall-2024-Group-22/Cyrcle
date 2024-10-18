package com.github.se.cyrcle.ui.theme.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ButtonTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun displayPrimaryButton() {
    val defaultTag = "PrimaryButton"
    var a = 1
    composeTestRule.setContent { Button("Button", { a++ }) }

    composeTestRule.onNodeWithTag("PrimaryButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("${defaultTag}Text").assertIsDisplayed()
    composeTestRule.onNodeWithTag(defaultTag).performClick()
    assertEquals(a, 2)
  }

  @Test
  fun displaySmallFloatingActionButton() {
    val defaultTag = "SmallFab"
    var a = 1
    composeTestRule.setContent {
      Scaffold(
          floatingActionButton = { SmallFloatingActionButton(Icons.Filled.Add, "", { a++ }) }) {
              innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {}
          }
    }

    composeTestRule.onNodeWithTag(defaultTag).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${defaultTag}Icon").assertIsDisplayed()

    composeTestRule.onNodeWithTag(defaultTag).performClick()
    assertEquals(a, 2)
  }

  @Test
  fun displayFloatingActionButton() {
    val defaultTag = "Fab"
    var a = 1
    composeTestRule.setContent {
      Scaffold(floatingActionButton = { FloatingActionButton(Icons.Filled.Add, "", { a++ }) }) {
          innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {}
      }
    }

    composeTestRule.onNodeWithTag(defaultTag).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${defaultTag}Icon").assertIsDisplayed()

    composeTestRule.onNodeWithTag(defaultTag).performClick()
    assertEquals(a, 2)
  }

  @Test
  fun displayLargeFloatingActionButton() {
    val defaultTag = "LargeFab"
    var a = 1
    composeTestRule.setContent {
      Scaffold(
          floatingActionButton = { LargeFloatingActionButton(Icons.Filled.Add, "", { a++ }) }) {
              innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {}
          }
    }

    composeTestRule.onNodeWithTag(defaultTag).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${defaultTag}Icon").assertIsDisplayed()

    composeTestRule.onNodeWithTag(defaultTag).performClick()
    assertEquals(a, 2)
  }

  @Test
  fun displayExtendedFloatingActionButton() {
    val defaultTag = "ExtendedFab"
    var a = 1
    composeTestRule.setContent {
      Scaffold(
          floatingActionButton = {
            ExtendedFloatingActionButton(Icons.Filled.Add, "A", { a++ }, text = "Extended")
          }) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {}
          }
    }

    composeTestRule.onNodeWithTag(defaultTag).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${defaultTag}Icon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("${defaultTag}Text").assertIsDisplayed()

    composeTestRule.onNodeWithTag(defaultTag).performClick()
    assertEquals(a, 2)
  }
}
