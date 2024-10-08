package com.github.se.cyrcle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.navigation.Screen

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {}
  }

  @Composable
  fun CyrcleApp() {
    val navController = rememberNavController()
    val navigationActions = NavigationActions(navController)

    NavHost(navController = navController, startDestination = Route.AUTH) {
      navigation(
          startDestination = Screen.AUTH,
          route = Route.AUTH,
      ) {
        composable(Screen.AUTH) {
          /* TO BE COMPLETED */
        }
      }

      navigation(
          startDestination = Screen.LIST,
          route = Route.LIST,
      ) {
        composable(Screen.LIST) {
          /* TO BE COMPLETED */
        }
      }

      navigation(
          startDestination = Screen.MAP,
          route = Route.MAP,
      ) {
        composable(Screen.MAP) {
          /* TO BE COMPLETED */
        }
      }

      navigation(
          startDestination = Screen.CARD,
          route = Route.CARD,
      ) {
        composable(Screen.CARD) {
          /* TO BE COMPLETED */
        }
      }
    }
  }
}
