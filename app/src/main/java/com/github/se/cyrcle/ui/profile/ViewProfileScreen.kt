package com.github.se.cyrcle.ui.profile

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Outbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.navigation.TopLevelDestinations
import com.github.se.cyrcle.ui.review.ReviewCard
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.atoms.Button
import com.github.se.cyrcle.ui.theme.atoms.IconButton
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.BottomNavigationBar
import kotlinx.coroutines.launch

@Composable
fun ViewProfileScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    parkingViewModel: ParkingViewModel,
    reviewViewModel: ReviewViewModel
) {

  val context = LocalContext.current
  val userState by userViewModel.currentUser.collectAsState()
  var isEditing by remember { mutableStateOf(false) }
  var signOut by remember { mutableStateOf(false) }

  val signOutToastText = stringResource(R.string.view_profile_on_sign_out_toast)

  Scaffold(
      modifier = Modifier.testTag("ViewProfileScreen"),
      bottomBar = {
        BottomNavigationBar(
            navigationActions = navigationActions,
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.VIEW_PROFILE)
      },
      floatingActionButton = {
        if (userState?.public?.userId != null) {
          if (userState!!.details?.isAdmin == true) {
            FloatingActionButton(
                onClick = { navigationActions.navigateTo(Screen.ADMIN) },
                modifier = Modifier.testTag("AdminFAB"),
                containerColor = MaterialTheme.colorScheme.primary) {
                  Text(text = "Admin", color = MaterialTheme.colorScheme.onPrimary)
                }
          }
        }
      }) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
          IconButton(
              modifier = Modifier.padding(10.dp).align(Alignment.TopEnd),
              icon = Icons.Filled.Outbox,
              contentDescription = "Sign Out",
              testTag = "SignOutButton",
              onClick = { signOut = true })

          IconButton(
              modifier = Modifier.padding(10.dp).align(Alignment.TopStart),
              icon =
                  Icons.Filled.Diamond, // or Icons.Filled.Paid or Icons.Filled.AttachMoney etc...
              contentDescription = "Go to Gambling Screen",
              testTag = "GamblingButton",
              onClick = { navigationActions.navigateTo(Screen.GAMBLING) })

          if (signOut) {
            AlertDialog(
                modifier = Modifier.testTag("SignOutDialog"),
                onDismissRequest = {},
                title = {
                  Text(stringResource(R.string.view_profile_screen_sign_out_dialog_title))
                },
                text = {
                  Text(stringResource(R.string.view_profile_screen_sign_out_dialog_message))
                },
                confirmButton = {
                  TextButton(
                      modifier = Modifier.testTag("SignOutDialogConfirmButton"),
                      onClick = {
                        userViewModel.signOut {
                          (context as Activity).runOnUiThread {
                            Toast.makeText(context, signOutToastText, Toast.LENGTH_SHORT).show()
                          }
                          navigationActions.navigateTo(TopLevelDestinations.AUTH)
                        }
                      }) {
                        Text(
                            stringResource(
                                R.string.view_profile_screen_sign_out_dialog_action_button))
                      }
                },
                dismissButton = {
                  TextButton(
                      modifier = Modifier.testTag("SignOutDialogCancelButton"),
                      onClick = { signOut = false }) {
                        Text(
                            stringResource(
                                R.string.view_profile_screen_sign_out_dialog_cancel_button))
                      }
                })
          }

          if (isEditing) {
            EditProfileComponent(
                user = userState,
                saveButton = { editedUser: User, validInputs: Boolean ->
                  Button(
                      text = stringResource(R.string.view_profile_screen_save_button),
                      onClick = {
                        userViewModel.updateUser(editedUser, context)
                        isEditing = false
                      },
                      colorLevel = ColorLevel.PRIMARY,
                      enabled = validInputs,
                      testTag = "SaveButton")
                },
                cancelButton = {
                  Button(
                      text = stringResource(R.string.view_profile_screen_cancel_button),
                      onClick = { isEditing = false },
                      colorLevel = ColorLevel.SECONDARY,
                      testTag = "CancelButton")
                })
          } else {
            DisplayProfileComponent(userState) {
              // Edit Button directly in the Box
              Button(
                  text = stringResource(R.string.view_profile_screen_modify_profile_button),
                  onClick = { isEditing = true },
                  colorLevel = ColorLevel.TERTIARY,
                  testTag = "EditButton")

              // Display the TabLayout
              TabLayout(
                  userViewModel = userViewModel,
                  parkingViewModel = parkingViewModel,
                  reviewViewModel = reviewViewModel,
                  navigationActions = navigationActions)
            }
          }
        }
      }
}

/**
 * Tab layout for the profile screen. This layout contains the following tabs:
 * - Favorite Parkings
 * - My Reviews
 *
 * This layout allows a tab to be selected by either clicking on the tab itself or by swiping
 * horizontally.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TabLayout(
    userViewModel: UserViewModel,
    parkingViewModel: ParkingViewModel,
    reviewViewModel: ReviewViewModel,
    navigationActions: NavigationActions
) {
  val tabs =
      listOf(
          stringResource(R.string.view_profile_screen_favorite_parkings),
          stringResource(R.string.view_profile_screen_my_reviews))

  val pagerState = rememberPagerState(pageCount = { tabs.size })
  val coroutineScope = rememberCoroutineScope()

  Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
    TabRow(selectedTabIndex = pagerState.currentPage, modifier = Modifier.testTag("TabRow")) {
      tabs.forEachIndexed { index, title ->
        Tab(
            text = { Text(title) },
            selected = pagerState.currentPage == index,
            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
            modifier = Modifier.testTag("Tab${title.replace(" ", "")}"))
      }
    }

    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
      when (page) {
        0 -> FavoriteParkingsSection(userViewModel, parkingViewModel, navigationActions)
        1 -> UserReviewsSection(reviewViewModel, userViewModel, parkingViewModel, navigationActions)
      }
    }
  }
}

@Composable
private fun FavoriteParkingsSection(
    userViewModel: UserViewModel,
    parkingViewModel: ParkingViewModel,
    navigationActions: NavigationActions
) {
  val favoriteParkings = userViewModel.favoriteParkings.collectAsState().value

  Box(modifier = Modifier.fillMaxSize().testTag("FavoriteParkingList")) {
    if (favoriteParkings.isEmpty()) {
      Text(
          text = stringResource(R.string.view_profile_screen_no_favorite_parking),
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.testTag("NoFavoritesMessage").padding(16.dp))
    } else {
      LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        itemsIndexed(favoriteParkings) { index, parking ->
          FavoriteParkingCard(navigationActions, parkingViewModel, userViewModel, parking, index)
        }
      }
    }
  }
}

@Composable
fun FavoriteParkingCard(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel,
    userViewModel: UserViewModel,
    parking: Parking,
    index: Int
) {
  val userState by userViewModel.currentUser.collectAsState()
  var showConfirmDialog by remember { mutableStateOf(false) }

  Card(
      modifier =
          Modifier.fillMaxWidth()
              .padding(8.dp)
              .clickable(
                  onClick = {
                    parkingViewModel.selectParking(parking)
                    navigationActions.navigateTo(Screen.PARKING_DETAILS)
                  })
              .testTag("ParkingItem$index"),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp).testTag("ParkingContent$index"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
              Column(
                  modifier = Modifier.weight(1f),
                  verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    // Parking name
                    androidx.compose.material3.Text(
                        text = parking.optName ?: stringResource(R.string.default_parking_name),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("ParkingName$index"))

                    // Display the personal note if it exists. Otherwise, display a message
                    // indicating that there is no note.
                    val personalNote = userState?.details?.personalNotes?.get(parking.uid)
                    Text(
                        text =
                            if (personalNote.isNullOrBlank()) {
                              stringResource(R.string.view_profile_screen_no_note)
                            } else {
                              stringResource(R.string.view_profile_screen_note, personalNote)
                            },
                        style =
                            if (personalNote.isNullOrBlank())
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontStyle = FontStyle.Italic, fontSize = 12.sp)
                            else MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("ParkingNote$index"),
                        textAlign = TextAlign.Justify)
                  }

              Spacer(modifier = Modifier.weight(0.1f).width(8.dp))

              // Favorite icon
              androidx.compose.material3.IconButton(
                  onClick = { showConfirmDialog = true },
                  modifier = Modifier.size(32.dp).testTag("FavoriteToggle$index")) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite Parking",
                        tint = Color.Red)
                  }
            }
      }

  if (showConfirmDialog) {
    AlertDialog(
        onDismissRequest = { showConfirmDialog = false },
        title = {
          Text(
              stringResource(R.string.view_profile_screen_remove_favorite_dialog_title),
              style = MaterialTheme.typography.titleMedium)
        },
        text = {
          Text(
              stringResource(
                  R.string.view_profile_screen_remove_favorite_dialog_message,
                  parking.optName ?: stringResource(R.string.default_parking_name)),
              textAlign = TextAlign.Justify)
        },
        confirmButton = {
          TextButton(
              onClick = {
                userViewModel.removeFavoriteParkingFromSelectedUser(parking)
                showConfirmDialog = false
              }) {
                Text(
                    stringResource(
                        R.string.view_profile_screen_remove_favorite_dialog_action_button))
              }
        },
        dismissButton = {
          TextButton(onClick = { showConfirmDialog = false }) {
            Text(stringResource(R.string.view_profile_screen_remove_favorite_dialog_cancel_button))
          }
        })
  }
}

@Composable
private fun UserReviewsSection(
    reviewViewModel: ReviewViewModel,
    userViewModel: UserViewModel,
    parkingViewModel: ParkingViewModel,
    navigationActions: NavigationActions
) {
  val userState by userViewModel.currentUser.collectAsState()
  val userReviews by reviewViewModel.userReviews.collectAsState()

  LaunchedEffect(userState?.public?.userId) {
    userState?.public?.userId?.let { userId -> reviewViewModel.getReviewsByOwnerId(userId) }
  }

  Box(modifier = Modifier.fillMaxSize().testTag("UserReviewsList")) {
    if (userReviews.isEmpty()) {
      Text(
          text = stringResource(R.string.view_profile_screen_no_reviews_message),
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.padding(16.dp).testTag("NoReviewsMessage"))
    } else {
      LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        items(items = userReviews, key = { it.uid }) { curReview ->
          val index = userReviews.indexOf(curReview)
          val isExpanded = true

          // We need to get the parking associated with the review to allow clicking on the card
          var parking by remember { mutableStateOf<Parking?>(null) }
          parkingViewModel.getParkingById(curReview.parking, { parking = it }, {})

          // We only display the review if the parking exists in the database
          val defaultName = stringResource(R.string.default_parking_name)
          parking?.let {
            ReviewCard(
                review = curReview,
                title = it.optName ?: defaultName,
                index = index,
                isExpanded = isExpanded,
                onCardClick = {
                  parkingViewModel.selectParking(it)
                  navigationActions.navigateTo(Screen.PARKING_DETAILS)
                },
                options = mapOf(),
                userViewModel = userViewModel,
                reviewViewModel = reviewViewModel)
          }
        }
      }
    }
  }
}
