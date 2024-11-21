package com.github.se.cyrcle.ui.gambling

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GamblingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun gambling_screen_shows_all_elements() {
        // When: The gambling screen is launched
        composeTestRule.setContent {
            GamblingScreen { wheelView ->
                wheelView.stopAnimation()
            }
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
            GamblingScreen { wheelView ->
                wheelView.stopAnimation()
            }
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
            GamblingScreen { wheelView ->
                wheelView.stopAnimation()
            }
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
            GamblingScreen { wheelView ->
                wheelView.stopAnimation()
            }
        }

        // Then: The spin button should have the correct size
        composeTestRule.onNodeWithTag("spin_button")
            .assertHeightIsEqualTo(90.dp)
            .assertWidthIsEqualTo(90.dp)
    }
}