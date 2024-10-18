package com.github.se.cyrcle.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.TopLevelDestinations
import com.github.se.cyrcle.ui.theme.CyrcleTheme

@Composable
fun AttributesPicker(navigationActions: NavigationActions, parkingViewModel: ParkingViewModel) {
  Scaffold(bottomBar = { BottomBarAddAttr(navigationActions) }) { padding ->
    Column(modifier = Modifier.padding(padding)) {
      Text("Attributes Picker", modifier = Modifier.testTag("AttributesPickerScreen"))
    }
    Text("Attributes Picker screen, where user select capaacites/rack , add pics ect...")
  }
}

@Composable
fun BottomBarAddAttr(navigationActions: NavigationActions) {
  CyrcleTheme {
    Box(Modifier.background(Color.White)) {
      Row(
          Modifier.fillMaxWidth().wrapContentHeight().padding(16.dp).background(Color.White),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically) {
            Button(
                { navigationActions.navigateTo(TopLevelDestinations.MAP) },
                modifier = Modifier.testTag("cancelButton"),
                colors = ButtonDefaults.buttonColors().copy(containerColor = Color.Transparent)) {
                  Text(
                      "Cancel",
                      color = MaterialTheme.colorScheme.primary,
                      fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                      fontWeight = FontWeight.Bold,
                      textAlign = TextAlign.Center)
                }

            VerticalDivider(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.height(32.dp).width(1.dp),
                thickness = 2.dp)
            Button(
                { navigationActions.navigateTo(TopLevelDestinations.MAP) },
                modifier = Modifier.testTag("submitButton"),
                colors = ButtonDefaults.buttonColors().copy(containerColor = Color.Transparent)) {
                  Text(
                      "Submit",
                      modifier = Modifier.width(100.dp),
                      color = MaterialTheme.colorScheme.primary,
                      fontWeight = FontWeight.Bold,
                      textAlign = TextAlign.Center)
                }
          }
    }
  }
}
