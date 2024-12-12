package com.github.se.cyrcle

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.github.se.cyrcle.model.address.AddressViewModel
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.report.ReportedObjectViewModel
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.permission.PermissionHandler
import com.github.se.cyrcle.ui.addParking.attributes.AttributesPicker
import com.github.se.cyrcle.ui.addParking.attributes.RackTypeHelpScreen
import com.github.se.cyrcle.ui.addParking.location.LocationPicker
import com.github.se.cyrcle.ui.authentication.SignInScreen
import com.github.se.cyrcle.ui.gambling.GamblingScreen
import com.github.se.cyrcle.ui.list.SpotListScreen
import com.github.se.cyrcle.ui.map.MapScreen
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.parkingDetails.ParkingDetailsScreen
import com.github.se.cyrcle.ui.profile.CreateProfileScreen
import com.github.se.cyrcle.ui.profile.ProfileScreen
import com.github.se.cyrcle.ui.report.AdminScreen
import com.github.se.cyrcle.ui.report.ImageReportScreen
import com.github.se.cyrcle.ui.report.ParkingReportScreen
import com.github.se.cyrcle.ui.report.ReviewReportScreen
import com.github.se.cyrcle.ui.report.ViewReportsScreen
import com.github.se.cyrcle.ui.review.AllReviewsScreen
import com.github.se.cyrcle.ui.review.ReviewScreen
import com.github.se.cyrcle.ui.zone.ZoneManagerScreen
import com.github.se.cyrcle.ui.zone.ZoneSelectionScreen

@Composable
fun CyrcleNavHost(
    navigationActions: NavigationActions,
    navController: NavHostController,
    parkingViewModel: ParkingViewModel,
    reviewViewModel: ReviewViewModel,
    userViewModel: UserViewModel,
    mapViewModel: MapViewModel,
    addressViewModel: AddressViewModel,
    reportedObjectViewModel: ReportedObjectViewModel,
    permissionHandler: PermissionHandler
) {
  NavHost(navController = navController, startDestination = Route.AUTH) {
    navigation(
        startDestination = Screen.AUTH,
        route = Route.AUTH,
    ) {
      composable(Screen.AUTH) { SignInScreen(navigationActions, userViewModel) }
      composable(Screen.CREATE_PROFILE) { CreateProfileScreen(navigationActions, userViewModel) }
    }

    navigation(
        startDestination = Screen.LIST,
        route = Route.LIST,
    ) {
      composable(Screen.LIST) {
        SpotListScreen(
            navigationActions,
            parkingViewModel,
            mapViewModel,
            userViewModel,
            addressViewModel,
            permissionHandler)
      }
      composable(Screen.PARKING_DETAILS) {
        ParkingDetailsScreen(mapViewModel, navigationActions, parkingViewModel, userViewModel)
      }
      composable(Screen.PARKING_REPORT) {
        ParkingReportScreen(navigationActions, userViewModel, parkingViewModel)
      }
      composable(Screen.IMAGE_REPORT) {
        ImageReportScreen(navigationActions, userViewModel, parkingViewModel)
      }
    }
    navigation(
        startDestination = Screen.ALL_REVIEWS,
        route = Route.REVIEW,
    ) {
      composable(Screen.ADD_REVIEW) {
        ReviewScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
      }
      composable(Screen.ALL_REVIEWS) {
        AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
      }
      composable(Screen.REVIEW_REPORT) {
        ReviewReportScreen(navigationActions, userViewModel, reviewViewModel)
      }
    }
    navigation(
        startDestination = Screen.MAP,
        route = Route.MAP,
    ) {
      composable(Screen.MAP) {
        MapScreen(
            navigationActions,
            parkingViewModel,
            userViewModel,
            mapViewModel,
            permissionHandler,
            addressViewModel)
      }
    }

    navigation(startDestination = Screen.LOCATION_PICKER, route = Route.ADD_SPOTS) {
      composable(Screen.LOCATION_PICKER) {
        LocationPicker(navigationActions, mapViewModel, parkingViewModel)
      }
      composable(Screen.ATTRIBUTES_PICKER) {
        AttributesPicker(
            navigationActions, parkingViewModel, mapViewModel, addressViewModel, userViewModel)
      }
      composable(Screen.RACK_INFO) { RackTypeHelpScreen(navigationActions) }
    }

    navigation(
        startDestination = Screen.VIEW_PROFILE,
        route = Route.VIEW_PROFILE,
    ) {
      composable(Screen.VIEW_PROFILE) {
        ProfileScreen(navigationActions, userViewModel, parkingViewModel, reviewViewModel)
      }
      composable(Screen.ADMIN) {
        AdminScreen(navigationActions, reportedObjectViewModel, parkingViewModel, reviewViewModel)
      }
      composable(Screen.VIEW_REPORTS) {
        ViewReportsScreen(
            navigationActions,
            reportedObjectViewModel,
            parkingViewModel,
            reviewViewModel,
            userViewModel)
      }
    }

    navigation(
        startDestination = Screen.GAMBLING,
        route = Route.GAMBLING,
    ) {
      composable(Screen.GAMBLING) { GamblingScreen(navigationActions, userViewModel) }
    }
    navigation(
        startDestination = Screen.ZONE_MANAGER,
        route = Route.ZONE,
    ) {
      composable(Screen.ZONE_MANAGER) {
        ZoneManagerScreen(mapViewModel, parkingViewModel, navigationActions)
      }
      composable(Screen.ZONE_SELECTION) {
        ZoneSelectionScreen(navigationActions, mapViewModel, parkingViewModel, addressViewModel)
      }
    }
  }
}
