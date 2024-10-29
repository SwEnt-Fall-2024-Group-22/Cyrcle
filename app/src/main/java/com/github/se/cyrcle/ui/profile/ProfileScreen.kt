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
import androidx.compose.ui.platform.testTag
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
  var isEditing by remember { mutableStateOf(false) }
  var firstName by remember { mutableStateOf(TextFieldValue(user.firstName)) }
  var lastName by remember { mutableStateOf(TextFieldValue(user.lastName)) }
  var username by remember { mutableStateOf(TextFieldValue(user.username)) }

  var favoriteParkings by remember { mutableStateOf(user.favoriteParkings.toMutableList()) }
  val parkingFavorites = remember {
    mutableStateListOf(*List(user.favoriteParkings.size) { true }.toTypedArray())
  }

  var profilePictureUrl by remember { mutableStateOf(user.profilePictureUrl) }
  val defaultImageUrl =
      "https://th.bing.com/th/id/R.aee6adef085a77dfa4708f3fd4a1ffb5?rik=nj%2buNqgLIJU0JQ&pid=ImgRaw&r=0"
  val filledStarImageUrl =
      "https://th.bing.com/th/id/R.f493b7a899f0c38194622d00ca70a7ff?rik=MvAc4%2fM5MeISxA&pid=ImgRaw&r=0"
  val emptyStarImageUrl =
      "https://cdn0.iconfinder.com/data/icons/thin-voting-awards/57/thin-232_star_favorite_add-1024.png"

  val context = LocalContext.current
  val imagePickerLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { profilePictureUrl = it.toString() }
      }

  DisposableEffect(Unit) {
    onDispose {
      favoriteParkings =
          favoriteParkings.filterIndexed { index, _ -> parkingFavorites[index] }.toMutableList()
    }
  }

  Scaffold { innerPadding ->
    Column(
        modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
          if (isEditing) {
            TextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                modifier = Modifier.testTag("FirstNameField"))
            TextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
                modifier = Modifier.testTag("LastNameField"))
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.testTag("UsernameField"))

            val painter =
                rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(
                            if (profilePictureUrl.isNotBlank()) profilePictureUrl
                            else defaultImageUrl)
                        .apply { transformations(CircleCropTransformation()) }
                        .build())

            Image(
                painter = painter,
                contentDescription = "Profile Picture",
                modifier =
                    Modifier.size(100.dp)
                        .clip(CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") }
                        .testTag("ProfilePicture"),
                contentScale = ContentScale.Crop)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { isEditing = false },
                colors = getButtonColors(ColorLevel.PRIMARY),
                modifier = Modifier.testTag("SaveButton")) {
                  Text("Save Changes")
                }
            Button(
                onClick = { isEditing = false },
                colors = getButtonColors(ColorLevel.SECONDARY),
                modifier = Modifier.testTag("CancelButton")) {
                  Text("Cancel")
                }
          } else {
            Text(
                text = "${firstName.text} ${lastName.text}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.testTag("UserNameDisplay"))
            val painter =
                rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(
                            if (user.profilePictureUrl.isNotBlank()) user.profilePictureUrl
                            else defaultImageUrl)
                        .apply { transformations(CircleCropTransformation()) }
                        .build())

            Image(
                painter = painter,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(100.dp).clip(CircleShape).testTag("ProfilePicture"),
                contentScale = ContentScale.Crop)
            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("UserHandleDisplay"))

            Button(
                onClick = { isEditing = true },
                colors = getButtonColors(ColorLevel.TERTIARY),
                modifier = Modifier.testTag("ModifyButton")) {
                  Text("Modify Profile")
                }
          }

          Spacer(modifier = Modifier.height(24.dp))

          Text(
              text = "Favorite Parkings",
              style = MaterialTheme.typography.titleMedium,
              modifier = Modifier.testTag("FavoriteParkingsLabel"))

          LazyRow(
              modifier = Modifier.fillMaxWidth().testTag("FavoriteParkingList"),
              horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(favoriteParkings) { index, parking ->
                  Card(
                      modifier = Modifier.size(120.dp).padding(8.dp),
                      shape = MaterialTheme.shapes.medium) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()) {
                              Row(
                                  verticalAlignment = Alignment.CenterVertically,
                                  horizontalArrangement = Arrangement.SpaceBetween,
                                  modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                    Text(
                                        text = parking,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.testTag("ParkingItem_$index"))
                                    val starPainter =
                                        rememberAsyncImagePainter(
                                            if (parkingFavorites[index]) filledStarImageUrl
                                            else emptyStarImageUrl)

                                    Image(
                                        painter = starPainter,
                                        contentDescription = "Toggle Favorite",
                                        modifier =
                                            Modifier.size(24.dp)
                                                .clickable {
                                                  parkingFavorites[index] = !parkingFavorites[index]
                                                }
                                                .testTag("FavoriteToggle_$index"))
                                  }
                            }
                      }
                }
              }
        }
  }
}
