package com.github.se.cyrcle.ui.parkingDetails

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.MockAuthenticationRepository
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockOfflineParkingRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockReportedObjectRepository
import com.github.se.cyrcle.di.mocks.MockUserRepository
import com.github.se.cyrcle.model.image.ImageRepository
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.offline.OfflineParkingRepository
import com.github.se.cyrcle.model.parking.online.ParkingRepository
import com.github.se.cyrcle.model.report.ReportedObjectRepository
import com.github.se.cyrcle.model.review.ReviewRepository
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.user.UserRepository
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class ParkingDetailsScreenAlertDialogTest {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var parkingRepository: ParkingRepository
  private lateinit var offlineParkingRepository: OfflineParkingRepository
  private lateinit var imageRepository: ImageRepository
  private lateinit var userRepository: UserRepository
  private lateinit var reviewRepository: ReviewRepository
  private lateinit var mockReportedObjectRepository: ReportedObjectRepository
  private lateinit var authenticator: MockAuthenticationRepository

  private lateinit var userViewModel: UserViewModel
  private lateinit var parkingViewModel: ParkingViewModel
  private lateinit var reviewViewModel: ReviewViewModel

  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    navigationActions = mock(NavigationActions::class.java)
    userRepository = MockUserRepository()
    parkingRepository = MockParkingRepository()
    offlineParkingRepository = MockOfflineParkingRepository()
    imageRepository = MockImageRepository()
    authenticator = MockAuthenticationRepository()
    mockReportedObjectRepository = MockReportedObjectRepository()
  }

  @Test
  fun testAssertAcceptAndCancelCloseDialog() {
    var dismissedCalled = false
    var acceptCalled = false
    composeTestRule.setContent {
      ParkingDetailsAlertDialogConfirmUpload(
          onDismiss = { dismissedCalled = true },
          onAccept = { acceptCalled = true },
          newParkingImageLocalPath = "imagePath")
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("parkingDetailsAlertDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("acceptButton").performClick()
    composeTestRule.waitForIdle()
    assert(acceptCalled)
    assert(!dismissedCalled)
    composeTestRule.onNodeWithTag("cancelButton").performClick()
    assert(dismissedCalled)
  }

  @Test
  fun testAssertDismissCloseImageDialog() {
    val userViewModel =
        UserViewModel(
            MockUserRepository(),
            MockParkingRepository(),
            MockImageRepository(),
            MockAuthenticationRepository())
    val parkingViewModel =
        ParkingViewModel(
            imageRepository,
            userViewModel,
            parkingRepository,
            offlineParkingRepository,
            mockReportedObjectRepository)
    composeTestRule.setContent {
      ParkingDetailsAlertDialogShowImage(
          parkingViewModel,
          userViewModel,
          onDismiss = {},
          navigationActions = mock(NavigationActions::class.java),
          imageUrl = "https://picsum.photos/200/300")
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("ParkingDetailsAlertDialogShowImage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("parkingDetailsAlertDialogImage").assertExists()
  }
}
