package com.github.se.cyrcle.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController

object Route {
  const val AUTH = "Auth"
  const val LIST = "List"
  const val MAP = "Map"
  const val ADD_SPOTS = "Add Spots"
  const val VIEW_PROFILE = "Profile"
  const val REVIEW = "Review"
  const val GAMBLING = "Gambling"
  const val ZONE = "Zone"
  const val TUTORIAL = "Tutorial"
}

object Screen {
  const val LIST = "List Screen"
  const val MAP = "Map Screen"
  const val PARKING_DETAILS = "Parking Details Screen"
  const val ADD_REVIEW = "Add Review Screen"
  const val ALL_REVIEWS = "All Reviews"
  const val LOCATION_PICKER = "Location Picker Screen"
  const val ATTRIBUTES_PICKER = "Attributes Picker Screen"
  const val RACK_INFO = "Rack Info Screen"
  const val AUTH = "Auth Screen"
  const val VIEW_PROFILE = "Profile Screen"
  const val CREATE_PROFILE = "Create Profile"
  const val PARKING_REPORT = "Parking Report Screen"
  const val REVIEW_REPORT = "Review Report Screen"
  const val IMAGE_REPORT = "Image Report Screen"
  const val GAMBLING = "Gambling Screen"
  const val ADMIN = "Admin Screen"
  const val VIEW_REPORTS = "View Reports Screen"
  const val ZONE_SELECTION = "Zone Selection Screen"
  const val ZONE_MANAGER = "Zone Manager Screen"
  const val TUTORIAL = "Tutorial Screen"
}

/**
 * Data class representing a top level destination in the app.
 *
 * @param route The route of the destination
 * @param icon The icon to display for the destination
 * @param textId The string resource ID for the text to display for the destination
 */
data class TopLevelDestination(val route: String, val icon: ImageVector, val textId: String)

/** Object containing the top level destinations in the app. */
object TopLevelDestinations {
  val AUTH =
      TopLevelDestination(route = Route.AUTH, icon = Icons.Outlined.Menu, textId = Route.AUTH)
  val LIST =
      TopLevelDestination(route = Route.LIST, icon = Icons.Outlined.Menu, textId = Route.LIST)
  val MAP =
      TopLevelDestination(route = Route.MAP, icon = Icons.Outlined.LocationOn, textId = Route.MAP)
  val PROFILE =
      TopLevelDestination(
          route = Route.VIEW_PROFILE, icon = Icons.Outlined.Person, textId = Route.VIEW_PROFILE)
}

/** List of top level destinations in the app. */
val LIST_TOP_LEVEL_DESTINATION =
    listOf(TopLevelDestinations.MAP, TopLevelDestinations.LIST, TopLevelDestinations.PROFILE)

/** Adapter class for navigating between screens in the app. */
open class NavigationActions(private val navController: NavHostController) {

  /**
   * Navigate to the specified [TopLevelDestination]
   *
   * @param destination The top level destination to navigate to.
   *
   * Clear the back stack when navigating to a new destination.
   */
  open fun navigateTo(destination: TopLevelDestination) {
    navController.navigate(destination.route) {
      // Pop up the whole back stack to the start destination
      popUpTo(0) { inclusive = true }
      // Avoid multiple copies of the same destination when reselecting the same item
      launchSingleTop = true
      // Restore state when reselecting a previously selected item
      restoreState = true
    }
  }

  /**
   * Navigate to the specified screen.
   *
   * @param screen The screen to navigate to
   */
  open fun navigateTo(screen: String) {
    navController.navigate(screen)
  }

  /** Navigate back to the previous screen. */
  open fun goBack() {
    navController.popBackStack()
  }

  /**
   * Get the current route of the navigation controller.
   *
   * @return The current route
   */
  open fun currentRoute(): String {
    return navController.currentDestination?.route ?: ""
  }
}
