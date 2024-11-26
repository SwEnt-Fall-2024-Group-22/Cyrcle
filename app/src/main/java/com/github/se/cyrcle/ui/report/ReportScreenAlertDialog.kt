package com.github.se.cyrcle.ui.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.ui.theme.atoms.Text

@Composable
fun ReportScreenAlertDialog(onDismiss: () -> Unit, onAccept: () -> Unit) {
  AlertDialog(
      modifier = Modifier.testTag("ReportScreenAlertDialog"),
      onDismissRequest = { onDismiss() },
      title = {
        Text(
            text = stringResource(R.string.alert_title),
            modifier = Modifier.testTag("AlertDialogTitle"))
      },
      containerColor = MaterialTheme.colorScheme.surfaceBright,
      text = {
        Text(
            text = stringResource(R.string.alert_content),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
            textAlign = TextAlign.Justify,
            modifier = Modifier.testTag("AlertDialogContent"))
      },
      confirmButton = {
        Row(
            modifier = Modifier.fillMaxWidth().testTag("AlertDialogButtons"),
            horizontalArrangement = Arrangement.SpaceEvenly) {
              // Cancel button (No)
              TextButton(onClick = { onDismiss() }, modifier = Modifier.testTag("CancelButton")) {
                Text(
                    text = stringResource(R.string.no),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp))
              }
              // Confirmation button (Yes)
              TextButton(onClick = { onAccept() }, modifier = Modifier.testTag("AcceptButton")) {
                Text(
                    text = stringResource(R.string.yes),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp))
              }
            }
      })
}
