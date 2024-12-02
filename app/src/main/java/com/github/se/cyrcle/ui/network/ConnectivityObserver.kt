import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.network.NetworkReceiver
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.theme.atoms.Text
import kotlinx.coroutines.delay

const val CONNECTION_CHANGE_DEBOUNCE_TIME = 1000

@Composable
fun ConnectivityObserver(userViewModel: UserViewModel, navigationActions: NavigationActions) {
  val networkReceiver = NetworkReceiver(LocalContext.current)
  networkReceiver.register()
  val isConnected by networkReceiver.isConnected.collectAsState(initial = true)
  var connectionChangedAcknowledged by remember { mutableStateOf(true) }
  LaunchedEffect(isConnected) {
    delay(CONNECTION_CHANGE_DEBOUNCE_TIME.toLong())
    // this blocks runs when the connection status changes, to prompt the user if needed.
    val connectivityMode = userViewModel.isOnlineMode.value

    Log.d("ConnectivityObserver", "Connection changed, connected :  $isConnected")
    Log.d(
        "ConnectivityObserver",
        "Connection changed,  user is in mode : online :   $connectivityMode")
    // only prompt the user if the new connection is not the same as the one selected by the user
    // i.e does not tell a user he lost connection if he is already using offline mode.
    connectionChangedAcknowledged = (connectivityMode == isConnected)
  }

  if (!connectionChangedAcknowledged) {
    ConnectivityChangedAlertDialog(
        navigationActions,
        userViewModel,
        onAck = { connectionChangedAcknowledged = true },
        lostConnection = !isConnected)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectivityChangedAlertDialog(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    onAck: () -> Unit,
    lostConnection: Boolean = true
) {
  // updates the user's connectivity mode based on the user's response.
  fun onAccept() {
    userViewModel.setIsOnlineMode(!lostConnection)
    // calls signInAnonymously
    userViewModel.signOut { navigationActions.navigateTo(Route.MAP) }
  }

  val alertTitle: String =
      if (lostConnection) stringResource(R.string.connectivity_alert_offline_title)
      else stringResource(R.string.connectivity_alert_online_title)
  val alertMessage: String =
      if (lostConnection) stringResource(R.string.connectivity_alert_offline_message)
      else stringResource(R.string.connectivity_alert_online_message)

  BasicAlertDialog(
      onDismissRequest = { onAck() },
      content = {
        Column(
            Modifier.fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.background, shape = MaterialTheme.shapes.medium)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                  text = alertTitle,
                  style = MaterialTheme.typography.headlineSmall,
                  modifier = Modifier.padding(8.dp))
              Spacer(Modifier.padding(8.dp))
              Text(text = alertMessage, style = MaterialTheme.typography.bodyLarge)
              Spacer(Modifier.padding(8.dp))
              Row(
                  Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                  horizontalArrangement = Arrangement.SpaceAround) {
                    Spacer(Modifier.weight(0.2f))
                    Button(onClick = { onAck() }, modifier = Modifier.weight(1f)) {
                      Text(stringResource(R.string.no))
                    }
                    Spacer(Modifier.weight(0.2f))
                    Button(
                        onClick = {
                          onAck()
                          onAccept()
                        },
                        modifier = Modifier.weight(1f)) {
                          Text(stringResource(R.string.yes))
                        }
                    Spacer(Modifier.weight(0.2f))
                  }
            }
      },
  )
}
