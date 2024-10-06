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

    NavHost(navController = navController, startDestination = Route.Auth) {
      navigation(
          startDestination = Screen.Auth,
          route = Route.Auth,
      ) {
        composable(Screen.Auth) {
          /* TO BE COMPLETED */
        }

        navigation(
            startDestination = Screen.List,
            route = Route.List,
        ) {
          composable(Screen.List) {
            /* TO BE COMPLETED */
          }
        }

        navigation(
            startDestination = Screen.Map,
            route = Route.Map,
        ) {
          composable(Screen.Map) {
            /* TO BE COMPLETED */
          }
        }

        navigation(
            startDestination = Screen.Card,
            route = Route.Card,
        ) {
          composable(Screen.Card) {
            /* TO BE COMPLETED */
          }
        }
      }
    }
  }
}
