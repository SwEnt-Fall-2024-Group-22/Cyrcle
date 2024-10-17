import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.ui.add.AttributesPicker
import com.github.se.cyrcle.ui.add.LocationPicker
import com.github.se.cyrcle.ui.authentication.SignInScreen
import com.github.se.cyrcle.ui.card.CardScreen
import com.github.se.cyrcle.ui.list.SpotListScreen
import com.github.se.cyrcle.ui.map.MapScreen
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.navigation.Screen

@Composable
fun CyrcleNavHost(
    navigationActions: NavigationActions,
    navController: NavHostController,
    parkingViewModel: ParkingViewModel
) {
  NavHost(navController = navController, startDestination = Route.AUTH) {
    navigation(
        startDestination = Screen.AUTH,
        route = Route.AUTH,
    ) {
      composable(Screen.AUTH) { SignInScreen(navigationActions) }
    }

    navigation(
        startDestination = Screen.LIST,
        route = Route.LIST,
    ) {
      composable(Screen.LIST) { SpotListScreen(navigationActions, parkingViewModel) }
      composable(Screen.CARD) { CardScreen(navigationActions, parkingViewModel) }
      composable(Screen.REVIEW) { CardScreen(navigationActions, parkingViewModel) }
    }

    navigation(
        startDestination = Screen.MAP,
        route = Route.MAP,
    ) {
      composable(Screen.MAP) { MapScreen(navigationActions, parkingViewModel) }
    }
    navigation(startDestination = Screen.LOCATION_PICKER, route = Route.ADD_SPOTS) {
      composable(Screen.LOCATION_PICKER) { LocationPicker(navigationActions, parkingViewModel) }
      composable(Screen.ATTRIBUTES_PICKER) { AttributesPicker(navigationActions, parkingViewModel) }
    }
  }
}
