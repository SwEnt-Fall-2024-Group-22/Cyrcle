package com.github.se.cyrcle.ui.gambling

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class GamblingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun gambling_screen_shows_all_elements() {
        // When: The gambling screen is launched
        composeTestRule.setContent {
            GamblingScreen2()
        }

        // Then: All UI elements should be visible
        composeTestRule.onNodeWithTag("gambling_screen")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("wheel_view")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("spin_button")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun spin_button_has_correct_properties() {
        // When: The gambling screen is launched
        composeTestRule.setContent {
            GamblingScreen2()
        }

        // Then: The spin button should have correct properties
        composeTestRule.onNodeWithTag("spin_button")
            .assertHasClickAction()
            .assertTextEquals("SPIN")
    }


    @Test
    fun wheel_view_has_correct_size() {
        // When: The gambling screen is launched
        composeTestRule.setContent {
            GamblingScreen2()
        }

        // Then: The wheel view should have the correct size
        composeTestRule.onNodeWithTag("wheel_view")
            .assertHeightIsEqualTo(300.dp)
            .assertWidthIsEqualTo(300.dp)
    }

    @Test
    fun spin_button_has_correct_size() {
        // When: The gambling screen is launched
        composeTestRule.setContent {
            GamblingScreen2()
        }

        // Then: The spin button should have the correct size
        composeTestRule.onNodeWithTag("spin_button")
            .assertHeightIsEqualTo(90.dp)
            .assertWidthIsEqualTo(90.dp)
    }



}