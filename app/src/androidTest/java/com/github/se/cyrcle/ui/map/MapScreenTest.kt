package com.github.se.cyrcle.ui.map

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.model.parking.ImageRepositoryCloudStorage
import com.github.se.cyrcle.model.parking.ParkingRepositoryFirestore
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class MapScreenTest : TestCase() {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockNavigation: NavigationActions
  private lateinit var parkingViewModel: ParkingViewModel

  @Before
  fun setUp() {

    mockNavigation = mock(NavigationActions::class.java)
    val imageRepository = ImageRepositoryCloudStorage(FirebaseAuth.getInstance())
    val parkingRepository = ParkingRepositoryFirestore(FirebaseFirestore.getInstance())
    parkingViewModel = ParkingViewModel(imageRepository, parkingRepository)

    `when`(mockNavigation.currentRoute()).thenReturn(Screen.MAP)
  }

  /**
   * Test to verify that the map screen and its components are displayed correctly.
   *
   * This test sets the content to the `MapScreen` and checks if the map screen, bottom navigation
   * bar, zoom controls, and add button are displayed.
   */
  @Test
  fun testMapIsDisplayed() {
    composeTestRule.setContent { MapScreen(mockNavigation, parkingViewModel = parkingViewModel) }

    composeTestRule.onNodeWithTag("MapScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("BottomNavigationBar").assertIsDisplayed()

    // Assert that the zoom controls are displayed
    composeTestRule.onNodeWithTag("ZoomControlsIn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ZoomControlsOut").assertIsDisplayed()

    // Assert that the add button is displayed
    composeTestRule.onNodeWithTag("addButton").assertIsDisplayed()
  }
}
