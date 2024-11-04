package com.github.se.cyrcle.ui.profile

import androidx.activity.compose.setContent
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.cyrcle.MainActivity
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@HiltAndroidTest
class CreateProfileScreenTest {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<MainActivity>()

  private lateinit var mockNavigationActions: NavigationActions
  private lateinit var userViewModel: UserViewModel

  @Before
  fun setUp() {
    hiltRule.inject()

    mockNavigationActions = mock(NavigationActions::class.java)

    userViewModel =
        UserViewModel(
            composeTestRule.activity.userRepository, composeTestRule.activity.parkingRepository)

    composeTestRule.activity.setContent {
      CreateProfileScreen(mockNavigationActions, userViewModel)
    }
  }

  @Test
  @OptIn(ExperimentalTestApi::class)
  fun testInitialDisplay() {
    composeTestRule.waitUntilAtLeastOneExists(hasTestTag("CreateProfileScreen"))

    // Verify initial display mode elements
    composeTestRule.onNodeWithTag("CreateProfileScreen").assertExists()
  }

  @Test
  @OptIn(ExperimentalTestApi::class)
  fun testTopAppBar() {
    composeTestRule.waitUntilAtLeastOneExists(hasTestTag("CreateProfileScreen"))

    // Verify initial display mode elements
    composeTestRule.onNodeWithTag("TopAppBar").assertExists()

    val createProfileScreenTitle = composeTestRule.activity.getString(R.string.profile_screen_title)
    composeTestRule.onNodeWithTag("TopAppBarTitle").assertTextEquals(createProfileScreenTitle)

    composeTestRule
        .onNodeWithTag("TopAppBarGoBackButton")
        .assertExists()
        .assertHasClickAction()
        .performClick()

    verify(mockNavigationActions).goBack()
  }
}
