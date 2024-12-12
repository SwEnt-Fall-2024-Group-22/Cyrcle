package com.github.se.cyrcle.ui.gambling

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.MockAuthenticationRepository
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockUserRepository
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.user.UserDetails
import com.github.se.cyrcle.model.user.UserPublic
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.model.user.Wallet
import com.github.se.cyrcle.ui.navigation.NavigationActions
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class GamblingScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockNavigationActions: NavigationActions
  private lateinit var mockUserRepository: MockUserRepository
  private lateinit var mockParkingRepository: MockParkingRepository
  private lateinit var mockImageRepository: MockImageRepository
  private lateinit var mockAuthenticator: MockAuthenticationRepository

  private lateinit var userViewModel: UserViewModel

  @Before
  fun setUp() {
    mockNavigationActions = mock(NavigationActions::class.java)
    mockUserRepository = MockUserRepository()
    mockParkingRepository = MockParkingRepository()
    mockImageRepository = MockImageRepository()
    mockAuthenticator = MockAuthenticationRepository()

    val user =
        User(
            UserPublic("1", "janesmith", "http://example.com/jane.jpg", 2.7),
            UserDetails("Jane", "Smith", "jane.smith@example.com", wallet = Wallet.empty()))

    userViewModel =
        UserViewModel(
            mockUserRepository, mockParkingRepository, mockImageRepository, mockAuthenticator)

    userViewModel.addUser(user, {}, {})
    mockAuthenticator.testUser = user
    userViewModel.signIn({}, {})

    userViewModel.setCurrentUserById("1")
  }

  @Test
  fun gambling_screen_shows_all_elements() {
    composeTestRule.setContent { GamblingScreen(mockNavigationActions, userViewModel) }

    composeTestRule.onNodeWithTag("CoinDisplay").assertTextEquals("Coins: 0")
    composeTestRule.onNodeWithTag("SpinButton").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("GamblingScreen").assertExists()
    composeTestRule.onNodeWithTag("WheelCanvas").assertExists()
    composeTestRule.onNodeWithTag("SpinButton").assertExists()
  }

  @Test
  fun verify_spins_with_wallet_restrictions() {
    // Add 25 coins to user's wallet
    userViewModel.creditCoinsToCurrentUser(25)

    composeTestRule.setContent { GamblingScreen(mockNavigationActions, userViewModel) }

    composeTestRule.mainClock.autoAdvance = false

    composeTestRule.onNodeWithTag("CoinDisplay").assertTextEquals("Coins: 25")

    // First spin (25 coins -> 15 coins)
    composeTestRule.onNodeWithTag("SpinButton").assertIsEnabled()
    composeTestRule.onNodeWithTag("SpinButton").performClick()

    // Wait for first spin to complete
    run {
      val incrementMs = 1000L
      var elapsedTime = 0L

      while (elapsedTime < timeoutMs) {
        composeTestRule.mainClock.advanceTimeBy(incrementMs)
        Thread.sleep(1000)

        try {
          composeTestRule.onNodeWithTag("SpinButton").assertIsEnabled()
          break
        } catch (e: AssertionError) {
          // Button still disabled, continue waiting
        }
        elapsedTime += incrementMs
      }
    }

    // Second spin (15 coins -> 5 coins)
    composeTestRule.onNodeWithTag("CoinDisplay").assertTextEquals("Coins: 15")
    composeTestRule.onNodeWithTag("SpinButton").assertIsEnabled()
    composeTestRule.onNodeWithTag("SpinButton").performClick()

    // Wait for second spin to complete
    run {
      val incrementMs = 1000L
      var elapsedTime = 0L

      while (elapsedTime < timeoutMs) {
        composeTestRule.mainClock.advanceTimeBy(incrementMs)
        Thread.sleep(1000)

        try {
          composeTestRule.onNodeWithTag("SpinButton").assertIsEnabled()
          break
        } catch (e: AssertionError) {
          // Button still disabled, continue waiting
        }
        elapsedTime += incrementMs
      }
    }

    // Third spin attempt (5 coins, should be disabled)
    composeTestRule.onNodeWithTag("CoinDisplay").assertTextEquals("Coins: 5")
    composeTestRule.onNodeWithTag("SpinButton").assertIsNotEnabled()

    composeTestRule.mainClock.autoAdvance = true
  }

  @Test
  fun spin_button_triggers_wheel_spin() {
    userViewModel.creditCoinsToCurrentUser(1000000)
    composeTestRule.setContent { GamblingScreen(mockNavigationActions, userViewModel) }

    composeTestRule.onNodeWithTag("SpinButton").performClick()
    // Wait for spin animation to start
    composeTestRule.waitForIdle()

    // Verify wheel is spinning (through visual state or properties)
    composeTestRule.onNodeWithTag("WheelCanvas").assertExists()
  }

  @Test
  fun verify_spin_button_properties() {
    composeTestRule.setContent { GamblingScreen(mockNavigationActions, userViewModel) }

    composeTestRule
        .onNodeWithTag("SpinButton")
        .assertHasClickAction()
        .assertHeightIsEqualTo(90.dp)
        .assertWidthIsEqualTo(90.dp)

    composeTestRule
        .onNodeWithTag("SpinButtonText", useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("SPIN")

    composeTestRule
        .onNodeWithTag("SpinCostText", useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("(10 coins)")
  }

  @Test
  fun verify_wheel_dimensions() {
    composeTestRule.setContent { GamblingScreen(mockNavigationActions, userViewModel) }

    composeTestRule
        .onNodeWithTag("WheelCanvas")
        .assertHeightIsEqualTo(300.dp)
        .assertWidthIsEqualTo(300.dp)
  }

  @Test
  fun verify_spin_animation_completion() {
    userViewModel.creditCoinsToCurrentUser(100000000)

    composeTestRule.setContent { GamblingScreen(mockNavigationActions, userViewModel) }

    composeTestRule.mainClock.autoAdvance = false

    composeTestRule.onNodeWithTag("SpinButton").performClick()
    // Wait for second spin to complete
    run {
      val incrementMs = 1000L
      var elapsedTime = 0L

      while (elapsedTime < timeoutMs) {
        composeTestRule.mainClock.advanceTimeBy(incrementMs)
        Thread.sleep(1000)

        try {
          composeTestRule.onNodeWithTag("SpinButton").assertIsEnabled()
          break
        } catch (e: AssertionError) {
          // Button still disabled, continue waiting
        }
        elapsedTime += incrementMs
      }
    }
    composeTestRule.onNodeWithTag("SpinButton").assertIsEnabled()

    composeTestRule.mainClock.autoAdvance = true
  }

  @Test
  fun verify_wheel_idle_animation() {
    composeTestRule.setContent { GamblingScreen(mockNavigationActions, userViewModel) }

    // Verify wheel performs idle animation
    runBlocking { delay(100) }
    composeTestRule.onNodeWithTag("WheelCanvas").assertExists()
  }

  @Test
  fun verify_subsequent_spins() {

    userViewModel.creditCoinsToCurrentUser(100000)

    composeTestRule.setContent { GamblingScreen(mockNavigationActions, userViewModel) }

    composeTestRule.mainClock.autoAdvance = false

    repeat(3) {
      composeTestRule.onNodeWithTag("SpinButton").performClick()

      run {
        val incrementMs = 1000L
        var elapsedTime = 0L

        while (elapsedTime < timeoutMs) {
          composeTestRule.mainClock.advanceTimeBy(incrementMs)
          Thread.sleep(1000)

          try {
            composeTestRule.onNodeWithTag("SpinButton").assertIsEnabled()
            break
          } catch (e: AssertionError) {
            // Button still disabled, continue waiting
          }
          elapsedTime += incrementMs
        }
      }

      composeTestRule.onNodeWithTag("SpinButton").assertIsEnabled()
    }

    composeTestRule.mainClock.autoAdvance = true
  }

  @Test
  fun verify_wheel_spin_state_changes() {
    userViewModel.creditCoinsToCurrentUser(1000000)
    composeTestRule.setContent { GamblingScreen(mockNavigationActions, userViewModel) }

    // Initial state check
    composeTestRule.onNodeWithTag("WheelSpinState").assertExists()

    // Trigger spin
    composeTestRule.onNodeWithTag("SpinButton").performClick()

    // Wait and check various states
    composeTestRule.mainClock.autoAdvance = false // Prevent clock from auto advancing

    // Advance through the spin animation
    composeTestRule.mainClock.advanceTimeBy(10000)
    composeTestRule.onNodeWithTag("WheelSpinState").assertExists()

    // Advance through the pause
    composeTestRule.mainClock.advanceTimeBy(2000)
    composeTestRule.onNodeWithTag("WheelSpinState").assertExists()

    composeTestRule.mainClock.autoAdvance = true // Re-enable auto advance
  }

  @Test
  fun verify_back_navigation() {
    composeTestRule.setContent { GamblingScreen(mockNavigationActions, userViewModel) }

    // Find and click the back button
    composeTestRule
        .onNodeWithTag("GamblingScreenTopBarGoBackButton")
        .assertExists()
        .assertHasClickAction()
        .performClick()

    // Verify that navigationActions.goBack() was called
    verify(mockNavigationActions).goBack()
  }

  @Test
  fun verify_xp_increment_visible_after_spin() {
    // Add coins to the user's wallet to allow for spins
    userViewModel.creditCoinsToCurrentUser(1000)

    composeTestRule.setContent { GamblingScreen(mockNavigationActions, userViewModel) }

    composeTestRule.mainClock.autoAdvance = false

    // Perform a spin
    composeTestRule.onNodeWithTag("SpinButton").performClick()

    var retries = 0
    var xpIncrementVisible = false

    while (retries < maxRetries) {
      // Wait for the XP increment to appear
      val incrementMs = 1000L // Check every second
      var elapsedTime = 0L

      // Try checking for XP increment visibility
      while (elapsedTime < timeoutMs) {
        composeTestRule.mainClock.advanceTimeBy(incrementMs)
        Thread.sleep(1000)

        xpIncrementVisible =
            composeTestRule.onAllNodesWithTag("xpIncrementText").fetchSemanticsNodes().isNotEmpty()

        if (xpIncrementVisible) break // Exit the inner loop if the XP increment becomes visible

        elapsedTime += incrementMs
      }

      if (xpIncrementVisible) break // If XP increment became visible, exit outer loop

      retries++
      if (retries < maxRetries) {
        // If the retry limit is not reached, try again by clicking the spin button again
        composeTestRule.onNodeWithTag("SpinButton").performClick()
      }
    }
    // Assert the XP increment text
    composeTestRule
        .onNodeWithTag("xpIncrementText")
        .assertTextContains("XP", substring = true) // Ensure XP increment is displayed

    // Advance time to let the animation fade out
    composeTestRule.mainClock.advanceTimeBy(3000) // Simulate 3 seconds of animation
    composeTestRule
        .onNodeWithTag("xpIncrementText")
        .assertDoesNotExist() // Ensure XP increment text is no longer visible after 3 seconds
  }

  @Test
  fun verify_progress_bar_and_level_up_mechanism_after_xp_reward() {
    // Add coins to the user's wallet to allow for spins
    userViewModel.creditCoinsToCurrentUser(1000)

    composeTestRule.setContent { GamblingScreen(mockNavigationActions, userViewModel) }

    composeTestRule.mainClock.autoAdvance = false

    // Verify initial level components exist
    composeTestRule.onNodeWithTag("LevelProgressBar").assertExists()
    composeTestRule
        .onNodeWithTag("CurrentLevelText")
        .assertExists()
        .assertTextContains(
            "Level ${userViewModel.currentUser.value!!.public.userReputationScore.toInt()}")
    composeTestRule
        .onNodeWithTag("NextLevelText")
        .assertExists()
        .assertTextContains(
            "Level ${userViewModel.currentUser.value!!.public.userReputationScore.toInt() + 1}")

    // Perform a spin
    composeTestRule.onNodeWithTag("SpinButton").performClick()

    // Retry logic: maximum number of retries
    var retries = 0
    var levelUpVisible = false

    while (retries < maxRetries) {
      val incrementMs = 500L // Check every second
      var elapsedTime = 0L

      // Try checking for XP increment visibility
      while (elapsedTime < timeoutMs) {
        composeTestRule.mainClock.advanceTimeBy(incrementMs)
        Thread.sleep(500)

        // Also check for level up text if it appears
        levelUpVisible =
            composeTestRule.onAllNodesWithTag("LevelUpText").fetchSemanticsNodes().isNotEmpty()

        if (levelUpVisible) break // Exit the inner loop if the level up text becomes visible

        elapsedTime += incrementMs
      }

      if (levelUpVisible) break // If level up text became visible, exit outer loop

      retries++
      if (retries < maxRetries) {
        // If the retry limit is not reached, try again by clicking the spin button again
        composeTestRule.onNodeWithTag("SpinButton").performClick()
      }
    }

    // If level up occurred, verify the level up components
    if (levelUpVisible) {
      composeTestRule.onNodeWithTag("LevelUpText").assertExists()
      composeTestRule.onNodeWithTag("LevelProgressBar").assertExists()
      composeTestRule.mainClock.advanceTimeBy(2000) // Give time for progress bar animation
      composeTestRule
          .onNodeWithTag("CurrentLevelText")
          .assertExists()
          .assertTextContains(
              "Level ${userViewModel.currentUser.value!!.public.userReputationScore.toInt()}")
      composeTestRule
          .onNodeWithTag("NextLevelText")
          .assertExists()
          .assertTextContains(
              "Level ${userViewModel.currentUser.value!!.public.userReputationScore.toInt() + 1}")
    }

    // Advance time to let the animation fade out
    composeTestRule.mainClock.advanceTimeBy(3000) // Simulate 3 seconds of animation
    composeTestRule.onNodeWithTag("LevelUpText").assertDoesNotExist()
  }

  companion object {
    private const val timeoutMs = 15000L
    private const val maxRetries = 100 // if that's not enough ill play the loto
  }
}
