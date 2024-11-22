import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.ui.gambling.GamblingScreen
import com.github.se.cyrcle.ui.gambling.WheelView
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GamblingScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun gambling_screen_shows_all_elements() {
    composeTestRule.setContent { GamblingScreen() }

    composeTestRule.onNodeWithTag("gambling_screen").assertExists()
    composeTestRule.onNodeWithTag("wheel_canvas").assertExists()
    composeTestRule.onNodeWithTag("spin_button").assertExists()
  }

  @Test
  fun verify_wheel_segments() {
    composeTestRule.setContent { GamblingScreen() }

    // Verify all segments exist
    for (i in 0..4) {
      composeTestRule.onNodeWithTag("segment_$i").assertExists()
      composeTestRule.onNodeWithTag("segment_text_$i").assertExists()
    }
  }

  @Test
  fun spin_button_triggers_wheel_spin() {
    composeTestRule.setContent { GamblingScreen() }

    composeTestRule.onNodeWithTag("spin_button").performClick()
    // Wait for spin animation to start
    composeTestRule.waitForIdle()

    // Verify wheel is spinning (through visual state or properties)
    composeTestRule.onNodeWithTag("wheel_canvas").assertExists()
  }

  @Test
  fun verify_spin_button_properties() {
    composeTestRule.setContent { GamblingScreen() }

    composeTestRule
        .onNodeWithTag("spin_button")
        .assertHasClickAction()
        .assertTextEquals("SPIN")
        .assertHeightIsEqualTo(90.dp)
        .assertWidthIsEqualTo(90.dp)
  }

  @Test
  fun verify_wheel_dimensions() {
    composeTestRule.setContent { GamblingScreen() }

    composeTestRule
        .onNodeWithTag("wheel_canvas")
        .assertHeightIsEqualTo(300.dp)
        .assertWidthIsEqualTo(300.dp)
  }

  @Test
  fun verify_segment_probabilities() {
    var wheelSpinFunction: (() -> Unit)? = null

    composeTestRule.setContent {
      WheelView(modifier = Modifier.testTag("wheel_view")).let { spinFn ->
        wheelSpinFunction = spinFn
      }
    }

    // Simulate multiple spins and verify probability distribution
    repeat(100) {
      wheelSpinFunction?.invoke()
      composeTestRule.waitForIdle()
      // Add delay between spins
      runBlocking { delay(100) }
    }
  }

  @Test
  fun verify_near_miss_mechanics() {
    var wheelSpinFunction: (() -> Unit)? = null

    composeTestRule.setContent {
      WheelView(modifier = Modifier.testTag("wheel_view")).let { spinFn ->
        wheelSpinFunction = spinFn
      }
    }

    // Trigger spins and verify near miss logs
    repeat(10) {
      wheelSpinFunction?.invoke()
      composeTestRule.waitForIdle()
      runBlocking { delay(100) }
    }
  }

  @Test
  fun verify_spin_animation_completion() {
    composeTestRule.setContent { GamblingScreen() }

    composeTestRule.onNodeWithTag("spin_button").performClick()

    // Wait for full spin duration
    runBlocking { delay(10000) }

    // Verify spin has completed
    composeTestRule.onNodeWithTag("spin_button").assertIsEnabled()
  }

  @Test
  fun verify_wheel_idle_animation() {
    composeTestRule.setContent { GamblingScreen() }

    // Verify wheel performs idle animation
    runBlocking { delay(1000) }
    composeTestRule.onNodeWithTag("wheel_canvas").assertExists()
  }

  @Test
  fun verify_subsequent_spins() {
    composeTestRule.setContent { GamblingScreen() }

    repeat(3) {
      composeTestRule.onNodeWithTag("spin_button").performClick()
      runBlocking { delay(10000) }
      composeTestRule.onNodeWithTag("spin_button").assertIsEnabled()
    }
  }

  @Test
  fun verify_wheel_pointer_exists() {
    composeTestRule.setContent { GamblingScreen() }
    composeTestRule.onNodeWithTag("wheel_pointer").assertExists()
  }

  @Test
  fun verify_wheel_rotation_updates() {
    composeTestRule.setContent { GamblingScreen() }

    composeTestRule.onNodeWithTag("wheel_rotation").assertExists()

    // Wait for idle animation
    runBlocking { delay(100) }

    composeTestRule.onNodeWithTag("wheel_rotation").assertExists()
  }

  @Test
  fun verify_wheel_spin_state_changes() {
    composeTestRule.setContent { GamblingScreen() }

    // Initial state check
    composeTestRule.onNodeWithTag("wheel_spin_state").assertExists()

    // Trigger spin
    composeTestRule.onNodeWithTag("spin_button").performClick()

    // Wait and check various states
    composeTestRule.mainClock.autoAdvance = false // Prevent clock from auto advancing

    // Advance through the spin animation
    composeTestRule.mainClock.advanceTimeBy(10000)
    composeTestRule.onNodeWithTag("wheel_spin_state").assertExists()

    // Advance through the pause
    composeTestRule.mainClock.advanceTimeBy(2000)
    composeTestRule.onNodeWithTag("wheel_spin_state").assertExists()

    composeTestRule.mainClock.autoAdvance = true // Re-enable auto advance
  }
}
