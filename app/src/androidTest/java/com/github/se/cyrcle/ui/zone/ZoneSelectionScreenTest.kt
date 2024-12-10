package com.github.se.cyrcle.ui.zone

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.MockAddressRepository
import com.github.se.cyrcle.di.mocks.MockAuthenticationRepository
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockOfflineParkingRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockReportedObjectRepository
import com.github.se.cyrcle.di.mocks.MockUserRepository
import com.github.se.cyrcle.model.address.AddressViewModel
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class ZoneSelectionScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions

  private lateinit var mapViewModel: MapViewModel
  private lateinit var parkingViewModel: ParkingViewModel
  private lateinit var addressViewModel: AddressViewModel

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    mapViewModel = MapViewModel()
    parkingViewModel =
        ParkingViewModel(
            MockImageRepository(),
            userViewModel =
                UserViewModel(
                    MockUserRepository(),
                    MockParkingRepository(),
                    MockImageRepository(),
                    MockAuthenticationRepository()),
            MockParkingRepository(),
            MockOfflineParkingRepository(),
            MockReportedObjectRepository())
    addressViewModel = AddressViewModel(MockAddressRepository())
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun checkAllUIElementsAreDisplayed() {
    composeTestRule.setContent {
      ZoneSelectionScreen(navigationActions, mapViewModel, parkingViewModel)
    }

    composeTestRule.waitUntilExactlyOneExists(hasTestTag("TopAppBar"))
    composeTestRule.onNodeWithTag("TopAppBar").assertIsDisplayed()
  }

  @Test
  fun verifyAlertDialog() {
    composeTestRule.setContent { AlertDialogPickZoneName() }
    composeTestRule.onNodeWithTag("AlertDialogTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ConditionCheckingInputText").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("AlertDialogButtonAccept")
        .assertIsDisplayed()
        .assertHasClickAction()
    composeTestRule
        .onNodeWithTag("AlertDialogButtonCancel")
        .assertIsDisplayed()
        .assertHasClickAction()
  }
}
