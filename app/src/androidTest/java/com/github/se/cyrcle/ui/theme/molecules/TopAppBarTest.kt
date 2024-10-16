package com.github.se.cyrcle.ui.theme.molecules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import com.github.se.cyrcle.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class TopAppBarTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navigationDestination: NavDestination
    private lateinit var navHostController: NavHostController
    private lateinit var navigationActions: NavigationActions

    @Before
    fun setUp() {
        navigationDestination = mock(NavDestination::class.java)
        navHostController = mock(NavHostController::class.java)
        navigationActions = NavigationActions(navHostController)
    }

    @Test
    fun displayTopAppBar() {
        val testTag = "TopAppBar"
        val title = "Title"
        composeTestRule.setContent {
            Scaffold(
                topBar = {
                    TopAppBar(navigationActions, testTag = testTag, title = title)
                }) { innerPadding ->
                Column(modifier = Modifier.padding(innerPadding)) {}
            }
        }

        // Top App Bar
        composeTestRule.onNodeWithTag(testTag)
            .assertIsDisplayed()

        // Title
        composeTestRule.onNodeWithTag("${testTag}Title")
            .assertIsDisplayed()
            .assertTextEquals(title)

        // Go Back Button
        composeTestRule.onNodeWithTag("${testTag}GoBackButton")
            .assertIsDisplayed()
            .assertHasClickAction()
    }
}
