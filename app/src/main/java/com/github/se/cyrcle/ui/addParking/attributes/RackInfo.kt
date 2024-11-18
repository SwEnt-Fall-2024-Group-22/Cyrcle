package com.github.se.cyrcle.ui.addParking.attributes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.ParkingRackType
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.CyrcleTheme
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

@Composable
fun RackTypeHelpScreen(navigationActions: NavigationActions) {
  Scaffold(
      topBar = {
        TopAppBar(navigationActions, stringResource(id = R.string.rack_info_screen_title))
      }) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).testTag("rackTypeList"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp)) {
              itemsIndexed(
                  ParkingRackType.entries.filter { it != ParkingRackType.OTHER }.toTypedArray()) {
                      index,
                      rackType ->
                    RackTypeItem(rackType, isImageOnRight = index % 2 == 1)
                  }
            }
      }
}

@Composable
fun RackTypeItem(rackType: ParkingRackType, isImageOnRight: Boolean) {
  Card(
      modifier =
          Modifier.fillMaxWidth()
              .height(IntrinsicSize.Min)
              .padding(horizontal = 16.dp, vertical = 8.dp)
              .testTag("rackTypeItem_${rackType.name}"),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
          Text(
              text = rackType.description,
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.primary,
              textAlign = if (isImageOnRight) TextAlign.End else TextAlign.Start,
              modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
          Row(
              horizontalArrangement = Arrangement.spacedBy(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()) {
                if (!isImageOnRight) {
                  RackImage(rackType, Modifier.weight(1f).testTag("rackImage_${rackType.name}"))
                  RackText(
                      rackType, Modifier.weight(2f).testTag("rackDescription_${rackType.name}"))
                } else {
                  RackText(
                      rackType, Modifier.weight(2f).testTag("rackDescription_${rackType.name}"))
                  RackImage(rackType, Modifier.weight(1f).testTag("rackImage_${rackType.name}"))
                }
              }
        }
      }
}

@Composable
fun RackImage(rackType: ParkingRackType, modifier: Modifier = Modifier) {
  Image(
      painter = painterResource(R.drawable.rack), // TODO: Define the image resource for each
      contentDescription = "${rackType.description} image",
      modifier =
          modifier
              .size(100.dp)
              .clip(RoundedCornerShape(8.dp))
              .testTag("rackImage_${rackType.name}"),
      contentScale = ContentScale.Crop)
}

@Composable
fun RackText(rackType: ParkingRackType, modifier: Modifier = Modifier) {
  Text(
      text = stringResource(getRackDescriptionResource(rackType)),
      style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
      textAlign = TextAlign.Justify,
      modifier = modifier.testTag("rackDescription_${rackType.name}"))
}

fun getRackDescriptionResource(rackType: ParkingRackType): Int {
  return when (rackType) {
    ParkingRackType.TWO_TIER -> R.string.two_tier_rack_description
    ParkingRackType.U_RACK -> R.string.u_rack_description
    ParkingRackType.VERTICAL -> R.string.vertical_rack_description
    ParkingRackType.WAVE -> R.string.wave_rack_description
    ParkingRackType.WALL_BUTTERFLY -> R.string.wall_butterfly_rack_description
    ParkingRackType.POST_AND_RING -> R.string.post_and_ring_rack_description
    ParkingRackType.GRID -> R.string.grid_rack_description
    else -> R.string.other_rack_description
  }
}

@Preview
@Composable
fun RackTypeHelpScreenPreview() {
  val navHostController = rememberNavController()
  val navigationActions = NavigationActions(navHostController)
  CyrcleTheme { RackTypeHelpScreen(navigationActions) }
}
