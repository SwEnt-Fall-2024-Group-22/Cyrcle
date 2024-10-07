package com.github.se.cyrcle.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController

object Route {
  const val Auth = "Auth"
  const val List = "List"
  const val Map = "Map"
  const val Card = "Card"
}

object Screen {
  const val Auth = "Auth Screen"
  const val List = "List Screen"
  const val Map = "Map Screen"
  const val Card = "Card Screen"
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
  val List = TopLevelDestination(route = Route.List, icon = Icons.Outlined.Menu, textId = "List")
  val Map = TopLevelDestination(route = Route.Map, icon = Icons.Outlined.LocationOn, textId = "Map")
}

/** List of top level destinations in the app. */
val LIST_TOP_LEVEL_DESTINATION = listOf(TopLevelDestinations.List, TopLevelDestinations.Map)

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
    navController.navigate(destination.route) { popUpTo(navController.graph.startDestinationId) }
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
