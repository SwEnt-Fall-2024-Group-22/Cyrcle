import androidx.compose.ui.semantics.text
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
import com.github.se.cyrcle.di.mocks.MockAuthenticationRepository
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockUserRepository
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.model.report.ReportedObjectRepository
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.user.UserDetails
import com.github.se.cyrcle.model.user.UserPublic
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.model.user.Wallet
import com.github.se.cyrcle.ui.gambling.GamblingScreen
import com.github.se.cyrcle.ui.navigation.NavigationActions
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

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

    val wallet = Wallet.empty()
    val user =
      User(
        UserPublic("1", "janesmith", "http://example.com/jane.jpg"),
        UserDetails("Jane", "Smith", "jane.smith@example.com", wallet = wallet)
      )

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

    composeTestRule.onNodeWithTag("coin_display").assertTextEquals("Coins: 0")
    composeTestRule.onNodeWithTag("gambling_screen").assertExists()
    composeTestRule.onNodeWithTag("wheel_canvas").assertExists()
    composeTestRule.onNodeWithTag("spin_button").assertExists()
  }

  @Test
  fun spin_button_triggers_wheel_spin() {
    composeTestRule.setContent { GamblingScreen(mockNavigationActions, userViewModel) }

    composeTestRule.onNodeWithTag("spin_button").performClick()
    // Wait for spin animation to start
    composeTestRule.waitForIdle()

    // Verify wheel is spinning (through visual state or properties)
    composeTestRule.onNodeWithTag("wheel_canvas").assertExists()
  }

  @Test
  fun verify_spin_button_properties() {
    composeTestRule.setContent { GamblingScreen(mockNavigationActions, userViewModel) }

    composeTestRule
        .onNodeWithTag("spin_button")
        .assertHasClickAction()
        .assertTextEquals("SPIN")
        .assertHeightIsEqualTo(90.dp)
        .assertWidthIsEqualTo(90.dp)
  }

  @Test
  fun verify_wheel_dimensions() {
    composeTestRule.setContent { GamblingScreen(mockNavigationActions, userViewModel) }

    composeTestRule
        .onNodeWithTag("wheel_canvas")
        .assertHeightIsEqualTo(300.dp)
        .assertWidthIsEqualTo(300.dp)
  }

  @Test
  fun verify_spin_animation_completion() {
    composeTestRule.setContent { GamblingScreen(mockNavigationActions, userViewModel) }

    composeTestRule.mainClock.autoAdvance = false

    composeTestRule.onNodeWithTag("spin_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(10000)
    composeTestRule.onNodeWithTag("spin_button").assertIsEnabled()

    composeTestRule.mainClock.autoAdvance = true
  }

  @Test
  fun verify_wheel_idle_animation() {
    composeTestRule.setContent { GamblingScreen(mockNavigationActions, userViewModel) }

    // Verify wheel performs idle animation
    runBlocking { delay(100) }
    composeTestRule.onNodeWithTag("wheel_canvas").assertExists()
  }

  @Test
  fun verify_subsequent_spins() {
    composeTestRule.setContent { GamblingScreen(mockNavigationActions, userViewModel) }

    composeTestRule.mainClock.autoAdvance = false

    repeat(3) {
      composeTestRule.onNodeWithTag("spin_button").performClick()
      composeTestRule.mainClock.advanceTimeBy(10000)
      composeTestRule.onNodeWithTag("spin_button").assertIsEnabled()
    }

    composeTestRule.mainClock.autoAdvance = true
  }

  @Test
  fun verify_wheel_spin_state_changes() {
    composeTestRule.setContent { GamblingScreen(mockNavigationActions, userViewModel) }

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
