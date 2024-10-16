package com.github.se.cyrcle

import android.util.Log
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.model.parking.ParkingRepositoryFirestore
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.ui.navigation.TopLevelDestinations
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Before
  fun setUp() = runBlocking {

    // Activate cache and disable network
    val db = Firebase.firestore
    db.disableNetwork().await()

    // Add some parking into the cache
    val parkingRepositoryFirestore = ParkingRepositoryFirestore(db)
    parkingRepositoryFirestore.addParking(TestInstancesParking.parking1, {}, {})
    parkingRepositoryFirestore.addParking(TestInstancesParking.parking2, {}, {})
    parkingRepositoryFirestore.addParking(TestInstancesParking.parking3, {}, {})
  }

  fun printParkings() {
    val parkingRepositoryFirestore = ParkingRepositoryFirestore(Firebase.firestore)
    parkingRepositoryFirestore.getParkings(
        { list -> list.forEach { Log.e("ENDTOEND", "$it") } }, { Log.e("ENDTOEND", "Failed") })
  }

  @Test
  fun reviewCardDisplaysWell() = runBlocking {
    with(AuthScreenRobot(composeTestRule)) {
      printParkings()
      assertAuthScreen()
      printParkings()
      performSignIn()
      printParkings()
    }

    with(MapScreenRobot(composeTestRule)) { toList() }
  }

  private class MapScreenRobot(val composeTestRule: ComposeTestRule) {
    @OptIn(ExperimentalTestApi::class)
    fun toList() {
      composeTestRule
          .onNodeWithTag(TopLevelDestinations.LIST.textId)
          .assertHasClickAction()
          .performClick()

      composeTestRule.waitUntilExactlyOneExists(hasTestTag("SpotListColumn"))
      Thread.sleep(100)
    }
  }

  private class AuthScreenRobot(val composeTestRule: ComposeTestRule) {

    fun assertAuthScreen() {
      composeTestRule.onNodeWithTag("loginScreen").assertIsDisplayed()
      composeTestRule
          .onNodeWithTag("loginTitle")
          .assertIsDisplayed()
          .assertTextEquals("Welcome to Cyrcle")
      composeTestRule
          .onNodeWithTag("loginButton")
          .assertIsDisplayed()
          .assertHasClickAction()
          .assertTextContains("Sign in with Google")
    }

    @OptIn(ExperimentalTestApi::class)
    suspend fun performSignIn() {
      val user = FirebaseAuth.getInstance().signInAnonymously().await()

      Log.wtf("ENDTOEND", "user $user")

      composeTestRule.onNodeWithTag("loginButton").performClick()

      composeTestRule.waitUntilExactlyOneExists(hasTestTag("MapScreen"))
    }
  }
}
