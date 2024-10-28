import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.getButtonColors

@Composable
fun ProfileScreen(user: User) {
    // State for editing mode and user details
    var isEditing by remember { mutableStateOf(false) }
    var firstName by remember { mutableStateOf(TextFieldValue(user.firstName)) }
    var lastName by remember { mutableStateOf(TextFieldValue(user.lastName)) }
    var username by remember { mutableStateOf(TextFieldValue(user.username)) }

    // State for favorite parkings
    var favoriteParkings by remember { mutableStateOf(user.favoriteParkings.toMutableList()) }
    val parkingFavorites = remember { mutableStateListOf(*List(user.favoriteParkings.size) { true }.toTypedArray()) }

    // URLs for profile picture and stars
    var profilePictureUrl by remember { mutableStateOf(user.profilePictureUrl) }
    val defaultImageUrl = "https://th.bing.com/th/id/R.aee6adef085a77dfa4708f3fd4a1ffb5?rik=nj%2buNqgLIJU0JQ&pid=ImgRaw&r=0"
    val filledStarImageUrl = "https://th.bing.com/th/id/R.f493b7a899f0c38194622d00ca70a7ff?rik=MvAc4%2fM5MeISxA&pid=ImgRaw&r=0"
    val emptyStarImageUrl = "https://cdn0.iconfinder.com/data/icons/thin-voting-awards/57/thin-232_star_favorite_add-1024.png"

    // Image picker launcher
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            profilePictureUrl = it.toString()
        }
    }

    // Filter non-favorites when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            favoriteParkings = favoriteParkings.filterIndexed { index, _ -> parkingFavorites[index] }.toMutableList()
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isEditing) {
                // Editable fields
                TextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") }
                )
                TextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") }
                )
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") }
                )

                // Profile picture
                val painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(if (profilePictureUrl.isNotBlank()) profilePictureUrl else defaultImageUrl)
                        .apply {
                            transformations(CircleCropTransformation())
                        }
                        .build()
                )

                Image(
                    painter = painter,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Save and Cancel buttons
                Button(
                    onClick = { isEditing = false },
                    colors = getButtonColors(ColorLevel.PRIMARY)
                ) {
                    Text("Save Changes")
                }
                Button(
                    onClick = { isEditing = false },
                    colors = getButtonColors(ColorLevel.SECONDARY)
                ) {
                    Text("Cancel")
                }
            } else {
                // Display user information
                Text(
                    text = "${user.firstName} ${user.lastName}",
                    style = MaterialTheme.typography.titleLarge
                )
                val painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(if (user.profilePictureUrl.isNotBlank()) user.profilePictureUrl else defaultImageUrl)
                        .apply {
                            transformations(CircleCropTransformation())
                        }
                        .build()
                )

                Image(
                    painter = painter,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Modify Profile button
                Button(
                    onClick = { isEditing = true },
                    colors = getButtonColors(ColorLevel.TERTIARY)
                ) {
                    Text("Modify Profile")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Favorite Parkings section
            Text(
                text = "Favorite Parkings",
                style = MaterialTheme.typography.titleMedium
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(favoriteParkings) { index, parking ->
                    Card(
                        modifier = Modifier
                            .size(120.dp)
                            .padding(8.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(8.dp)
                            ) {
                                Text(
                                    text = parking,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                val starPainter = rememberAsyncImagePainter(
                                    if (parkingFavorites[index]) filledStarImageUrl else emptyStarImageUrl
                                )
                                Image(
                                    painter = starPainter,
                                    contentDescription = "Toggle Favorite",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            parkingFavorites[index] = !parkingFavorites[index]
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}