package com.github.se.cyrcle.ui.addParking

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.MockAddressRepository
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockReportedObjectRepository
import com.github.se.cyrcle.CyrcleNavHost
import com.github.se.cyrcle.di.mocks.AuthenticationRepositoryMock
import com.github.se.cyrcle.di.mocks.MockPermissionHandler
import com.github.se.cyrcle.model.address.AddressViewModel
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.Location
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.ui.addParking.attributes.AttributesPicker
import com.github.se.cyrcle.ui.addParking.location.LocationPicker
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.navigation.TopLevelDestinations
import com.mapbox.geojson.Point
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify


@RunWith(AndroidJUnit4::class)
class AddScreensNavigationTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Mock lateinit var navigationActions: NavigationActions

  private lateinit var reportedObjectRepository: MockReportedObjectRepository
  private lateinit var mockedParkingRepository: MockParkingRepository
  private lateinit var mockedImageRepository: MockImageRepository
  private lateinit var addressRepository: MockAddressRepository

  private lateinit var parkingViewModel: ParkingViewModel
  private lateinit var mapViewModel: MapViewModel
  private lateinit var addressViewModel: AddressViewModel


  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    reportedObjectRepository = MockReportedObjectRepository()
    mockedParkingRepository = MockParkingRepository()
    mockedImageRepository = MockImageRepository()
    addressRepository = MockAddressRepository()

    parkingViewModel = ParkingViewModel(mockedImageRepository, mockedParkingRepository, reportedObjectRepository)
    mapViewModel = MapViewModel()
    addressViewModel = AddressViewModel(addressRepository)
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun testNavigationToAttribute() {
    composeTestRule.setContent {
      LocationPicker(navigationActions, mapViewModel)
    }
    composeTestRule.waitForIdle()
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("nextButton"))
    // Perform click on the add button
    composeTestRule.onNodeWithTag("nextButton").performClick()

    // check that we're still on the same screen
    composeTestRule.waitUntilAtLeastOneExists(hasTestTag("LocationPickerScreen"))
    composeTestRule.onNodeWithTag("LocationPickerScreen").assertExists().assertIsDisplayed()

    // Perform Click on the next button
    composeTestRule.onNodeWithTag("nextButton").performClick()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun testSubmit() {
    mapViewModel.updateLocation(Location(Point.fromLngLat(0.0, 0.0)))

    composeTestRule.setContent {
      AttributesPicker(navigationActions, parkingViewModel, mapViewModel, addressViewModel)
    }

    composeTestRule.waitUntilExactlyOneExists(hasTestTag("AttributesPickerTitle"))
    // Artificially set the title (otherwise the submit button is disabled)
    composeTestRule
        .onNodeWithTag("AttributesPickerTitle")
        .assertExists()
        .performTextInput("titleForParking")
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("submitButton"))
    // Perform click on the add button
    composeTestRule.onNodeWithTag("submitButton").performClick()

    verify(navigationActions).navigateTo(TopLevelDestinations.MAP)
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun testCancel() {
    mapViewModel.updateLocation(Location(Point.fromLngLat(0.0, 0.0)))

    composeTestRule.setContent {
      AttributesPicker(navigationActions, parkingViewModel, mapViewModel, addressViewModel)
    }

    composeTestRule.waitUntilExactlyOneExists(hasTestTag("cancelButton"))
    // Perform click on the add button
    composeTestRule.onNodeWithTag("cancelButton").performClick()

    verify(navigationActions).navigateTo(TopLevelDestinations.MAP)
  }

  @Test
  fun testCancel2() {
    composeTestRule.setContent {
      LocationPicker(navigationActions, mapViewModel)
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("cancelButton").performClick()

    verify(navigationActions).navigateTo(Screen.MAP)
  }
}
