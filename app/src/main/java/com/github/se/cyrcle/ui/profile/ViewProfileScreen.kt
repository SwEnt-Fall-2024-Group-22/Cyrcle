package com.github.se.cyrcle.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.atoms.Button
import com.github.se.cyrcle.ui.theme.atoms.InputText
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.BottomNavigationBar

@Composable
fun ViewProfileScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
) {

    val userState by userViewModel.currentUser.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var firstName by remember { mutableStateOf(userState?.firstName ?: "") }
    var lastName by remember { mutableStateOf(userState?.lastName ?: "") }
    var username by remember { mutableStateOf(userState?.username ?: "") }
    var profilePictureUrl by remember { mutableStateOf(userState?.profilePictureUrl ?: "") }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { profilePictureUrl = it.toString() }
        }

    Scaffold(
        modifier = Modifier.testTag("ProfileScreen"),
        bottomBar = {
            BottomNavigationBar(
                navigationActions = navigationActions,
                tabList = LIST_TOP_LEVEL_DESTINATION,
                selectedItem = Route.PROFILE)
        }) { innerPadding ->
        Column(
            modifier =
            Modifier.fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .testTag("ProfileContent"),
            horizontalAlignment = Alignment.CenterHorizontally) {
            if (isEditing) {
                EditProfileContent(
                    firstName = firstName,
                    lastName = lastName,
                    username = username,
                    profilePictureUrl = profilePictureUrl,
                    onFirstNameChange = { firstName = it },
                    onLastNameChange = { lastName = it },
                    onUsernameChange = { username = it },
                    onImageClick = { imagePickerLauncher.launch("image/*") },
                    onSave = {
                        userViewModel.updateUser(
                            userState?.copy(
                                firstName = firstName,
                                lastName = lastName,
                                username = username,
                                profilePictureUrl = profilePictureUrl) ?: return@EditProfileContent)
                        isEditing = false
                    },
                    onCancel = {
                        firstName = userState?.firstName ?: ""
                        lastName = userState?.lastName ?: ""
                        username = userState?.username ?: ""
                        profilePictureUrl = userState?.profilePictureUrl ?: ""
                        isEditing = false
                    })
            } else {
                DisplayProfileContent(
                    firstName = firstName,
                    lastName = lastName,
                    username = username,
                    profilePictureUrl = profilePictureUrl,
                    onEditClick = { isEditing = true })

                Spacer(modifier = Modifier.height(24.dp))

                FavoriteParkingsSection(userViewModel)
            }
        }
    }
}

@Composable
private fun EditProfileContent(
    firstName: String,
    lastName: String,
    username: String,
    profilePictureUrl: String,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onImageClick: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    ProfileImage(
        url = profilePictureUrl,
        onClick = onImageClick,
        isEditable = true,
        modifier = Modifier.testTag("ProfileImage"))

    Spacer(modifier = Modifier.height(24.dp))

    InputText(
        value = firstName,
        onValueChange = onFirstNameChange,
        label = "First Name",
        testTag = "FirstNameField")

    Spacer(modifier = Modifier.height(8.dp))

    InputText(
        value = lastName,
        onValueChange = onLastNameChange,
        label = "Last Name",
        testTag = "LastNameField")

    Spacer(modifier = Modifier.height(8.dp))

    InputText(
        value = username,
        onValueChange = onUsernameChange,
        label = "Username",
        testTag = "UsernameField")

    Spacer(modifier = Modifier.height(16.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(text = "Save", onClick = onSave, colorLevel = ColorLevel.PRIMARY, testTag = "SaveButton")

        Button(
            text = "Cancel",
            onClick = onCancel,
            colorLevel = ColorLevel.SECONDARY,
            testTag = "CancelButton")
    }
}

@Composable
private fun DisplayProfileContent(
    firstName: String,
    lastName: String,
    username: String,
    profilePictureUrl: String,
    onEditClick: () -> Unit
) {
    Text(
        text = firstName,
        style = MaterialTheme.typography.headlineMedium,
        testTag = "DisplayFirstName")

    Text(
        text = lastName, style = MaterialTheme.typography.headlineMedium, testTag = "DisplayLastName")

    Spacer(modifier = Modifier.height(16.dp))

    ProfileImage(
        url = profilePictureUrl,
        onClick = {},
        isEditable = false,
        modifier = Modifier.testTag("ProfileImage"))

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "@$username", style = MaterialTheme.typography.bodyMedium, testTag = "DisplayUsername")

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        text = "Modify Profile",
        onClick = onEditClick,
        colorLevel = ColorLevel.TERTIARY,
        testTag = "EditButton")
}

@Composable
private fun ProfileImage(
    url: String,
    onClick: () -> Unit,
    isEditable: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier =
        modifier.then(if (isEditable) Modifier.clickable(onClick = onClick) else Modifier)) {
        if (url.isBlank()) {
            Box(
                modifier =
                Modifier.size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Default Profile Picture",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        } else {
            Image(
                painter =
                rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(url)
                        .apply { transformations(CircleCropTransformation()) }
                        .build()),
                contentDescription = "Profile Picture",
                modifier = Modifier.size(120.dp).clip(CircleShape),
                contentScale = ContentScale.Crop)
        }

        if (isEditable) {
            Box(
                modifier =
                Modifier.size(120.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit Profile Picture",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp))
            }
        }
    }
}

@Composable
private fun FavoriteParkingsSection(userViewModel: UserViewModel) {
    val favoriteParkings = userViewModel.favoriteParkings.collectAsState().value

    LaunchedEffect(Unit) { userViewModel.getSelectedUserFavoriteParking() }

    Text(
        text = "Favorite Parkings",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.testTag("FavoriteParkingsTitle"))

    Spacer(modifier = Modifier.height(16.dp))

    if (favoriteParkings.isEmpty()) {
        Text(
            text = "No favorite parkings yet",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("NoFavoritesMessage"))
    } else {
        LazyRow(
            modifier = Modifier.fillMaxWidth().testTag("FavoriteParkingList"),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(favoriteParkings) { index, parking ->
                FavoriteParkingCard(
                    parking = parking,
                    index = index,
                    onRemove = {
                        userViewModel.removeFavoriteParkingFromSelectedUser(parking.uid)
                        userViewModel
                            .getSelectedUserFavoriteParking() // gotta call this cuz the UserViewModel is
                        // poorly implemented and doesnt change the
                        // favoriteParkings itself
                    })
            }
        }
    }
}

@Composable
private fun FavoriteParkingCard(parking: Parking, index: Int, onRemove: () -> Unit) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    Card(modifier = Modifier.size(120.dp).padding(8.dp), shape = MaterialTheme.shapes.medium) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = parking.optName ?: "", // Display only the optName property
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Center).padding(8.dp).testTag("ParkingItem_$index"))

            IconButton(
                onClick = { showConfirmDialog = true },
                modifier =
                Modifier.align(Alignment.TopEnd).size(32.dp).testTag("FavoriteToggle_$index")) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Remove from Favorites",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(20.dp))
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Remove favorite") },
            text = { Text("Are you sure you want to remove ${parking.optName} from your favorites?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemove()
                        showConfirmDialog = false
                    }) {
                    Text("Remove")
                }
            },
            dismissButton = { TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") } })
    }
}
