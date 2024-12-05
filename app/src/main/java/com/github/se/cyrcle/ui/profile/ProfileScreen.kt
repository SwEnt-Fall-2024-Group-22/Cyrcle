package com.github.se.cyrcle.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions

@Composable
fun ProfileScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    parkingViewModel: ParkingViewModel,
    reviewViewModel: ReviewViewModel
) {
  val isSignedIn by userViewModel.isSignedIn.collectAsState(false)

  if (isSignedIn)
      ViewProfileScreen(navigationActions, userViewModel, parkingViewModel, reviewViewModel)
  else InviteToAuthScreen(navigationActions)
}
