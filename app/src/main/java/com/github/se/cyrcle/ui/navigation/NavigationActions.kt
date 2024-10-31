package com.github.se.cyrcle.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

object Route {
  const val AUTH = "Auth"
  const val LIST = "List"
  const val MAP = "Map"
  const val ADD_SPOTS = "Add Spots"
  const val PROFILE = "Profile" // Add this
}

object Screen {
  const val AUTH = "Auth Screen"
  const val LIST = "List Screen"
  const val MAP = "Map Screen"
  const val CARD = "Card Screen"
  const val REVIEW = "Review Screen"
  const val LOCATION_PICKER = "Location Picker"
  const val ATTRIBUTES_PICKER = "Attributes Picker"
  const val PROFILE = "Profile Screen" // Add this
}

data class TopLevelDestination(val route: String, val icon: ImageVector, val textId: String)

object TopLevelDestinations {
  val LIST =
      TopLevelDestination(route = Route.LIST, icon = Icons.Outlined.Menu, textId = Route.LIST)
  val MAP =
      TopLevelDestination(route = Route.MAP, icon = Icons.Outlined.LocationOn, textId = Route.MAP)
  val PROFILE =
      TopLevelDestination(
          route = Route.PROFILE, icon = Icons.Outlined.Person, textId = Route.PROFILE)
}

val LIST_TOP_LEVEL_DESTINATION =
    listOf(TopLevelDestinations.MAP, TopLevelDestinations.LIST, TopLevelDestinations.PROFILE)

open class NavigationActions(private val navController: NavHostController) {
  open fun navigateTo(destination: TopLevelDestination) {
    navController.navigate(destination.route) {
      popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
        inclusive = true
      }
      launchSingleTop = true
      restoreState = true
    }
  }

  open fun navigateTo(screen: String) {
    navController.navigate(screen)
  }

  open fun goBack() {
    navController.popBackStack()
  }

  open fun currentRoute(): String {
    return navController.currentDestination?.route ?: ""
  }
}
