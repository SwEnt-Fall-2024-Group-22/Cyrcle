package com.github.se.cyrcle.ui.report

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.github.se.cyrcle.R
import com.github.se.cyrcle.ui.theme.atoms.Text

@Composable
fun ReportScreenAlertDialog(
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.testTag("parkingDetailsAlertDialog"),
        onDismissRequest = { onDismiss() },
        title = { Text("Are You Sure?") },
        containerColor = MaterialTheme.colorScheme.surfaceBright,
        text = {
            Text(
                text = "We take reports very seriously. Please make sure that the information you have mentioned is accurate. If enough users also submit reports on this object, it will be flagged for administrator review. Do you still want to add this Report?",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                textAlign = TextAlign.Justify // Ensures justified alignment
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Cancel button (No)
                TextButton(
                    onClick = { onDismiss() },
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .testTag("cancelButton")
                ) {
                    Text(
                        text = stringResource(R.string.no),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp)
                    )
                }
                // Confirmation button (Yes)
                TextButton(
                    onClick = { onAccept() },
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .testTag("acceptButton")
                ) {
                    Text(
                        text = stringResource(R.string.yes),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp)
                    )
                }
            }
        }
    )
}