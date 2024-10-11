package com.github.se.cyrcle.ui.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.github.se.cyrcle.model.parking.Location
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingCapacity
import com.github.se.cyrcle.model.parking.ParkingProtection
import com.github.se.cyrcle.model.parking.ParkingRackType
import com.github.se.cyrcle.model.parking.Point
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.Cerulean
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

// Function to convert ParkingProtection enum to human-readable string
fun convertProtectionToString(protection: ParkingProtection): String {
  return when (protection) {
    ParkingProtection.INDOOR -> "Indoor protection"
    ParkingProtection.COVERED -> "Covered protection"
    ParkingProtection.NONE -> "No protection"
  }
}

// Function to convert ParkingRackType enum to human-readable string
fun convertRackToString(rackType: ParkingRackType): String {
  return when (rackType) {
    ParkingRackType.TWO_TIER -> "Two-tier rack"
    ParkingRackType.U_RACK -> "U-Rack"
    ParkingRackType.VERTICAL -> "Vertical rack"
    ParkingRackType.WAVE -> "Wave rack"
    ParkingRackType.WALL_BUTTERFLY -> "Wall butterfly rack"
    ParkingRackType.POST_AND_RING -> "Post and ring rack"
    ParkingRackType.GRID -> "Grid rack"
    ParkingRackType.OTHER -> "Unidentified Rack"
  }
}

// Function to convert ParkingCapacity enum to human-readable string
fun convertCapacityToString(capacity: ParkingCapacity): String {
  return when (capacity) {
    ParkingCapacity.XSMALL -> "Less than 10 spots"
    ParkingCapacity.SMALL -> "10-25 spots"
    ParkingCapacity.MEDIUM -> "26-50 spots"
    ParkingCapacity.LARGE -> "51-100 spots"
    ParkingCapacity.XLARGE -> "More than 100 spots"
  }
}

// Sample parking spot data for demonstration
val parking1 =
    Parking(
        "Test_spot_1",
        null,
        null,
        Location(Point(46.2, 6.6)),
        listOf(
            "https://upload.wikimedia.org/wikipedia/commons/7/78/%22G%C3%A4nsemarkt%22_in_Amance_-_panoramio.jpg",
            "https://upload.wikimedia.org/wikipedia/commons/7/78/%22G%C3%A4nsemarkt%22_in_Amance_-_panoramio.jpg",
        ),
        ParkingCapacity.LARGE,
        ParkingRackType.TWO_TIER,
        ParkingProtection.COVERED,
        0.0,
        true)

// Another parking spot example
val parking2 =
    Parking(
        "Test_spot_2",
        null,
        null,
        Location(Point(46.3, 6.7)),
        listOf(
            "https://upload.wikimedia.org/wikipedia/commons/6/6b/Bicycle_parking_at_Alewife_station%2C_August_2001.jpg"), // Corrected URL
        ParkingCapacity.SMALL,
        ParkingRackType.TWO_TIER,
        ParkingProtection.COVERED,
        0.0,
        true)

// Third parking spot example
val parking3 =
    Parking(
        "Test_spot_3",
        null,
        null,
        Location(Point(47.1, 7.1)),
        listOf(
            "https://upload.wikimedia.org/wikipedia/commons/6/6b/Bicycle_parking_at_Alewife_station%2C_August_2001.jpg"), // Corrected URL
        ParkingCapacity.LARGE,
        ParkingRackType.TWO_TIER,
        ParkingProtection.COVERED,
        0.0,
        true)

// Main UI Composable function to display card with parking information
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CardScreen(navigationActions: NavigationActions, curParking: Parking = parking1) {
  // Scaffold provides the structure for the screen with a TopAppBar
  Scaffold(
      topBar = {
        TopAppBar(
            navigationActions,
            title = {
              Text(
                  text = "Description of ${curParking.uid}",
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  modifier = Modifier.testTag("TopAppBarTitle")) // Test tag for title
            })
      }) {
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .testTag("CardScreenBox") // Test tag for main container
            ) {
              Column(
                  modifier = Modifier.fillMaxSize().padding(it),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.SpaceBetween) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Display a row of images using LazyRow
                    LazyRow(
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(8.dp)
                                .testTag("ParkingImagesRow"), // Test tag for image row
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                          items(curParking.images.size) { index ->
                            AsyncImage(
                                model = curParking.images[index],
                                contentDescription = "Image $index",
                                modifier =
                                    Modifier.size(200.dp)
                                        .padding(2.dp)
                                        .testTag("ParkingImage$index"), // Test tag for each image
                                contentScale = ContentScale.Crop)
                          }
                        }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Column for parking info such as capacity, rack type, protection, etc.
                    Column(
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .testTag("InfoColumn"), // Test tag for info column
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                          // Row for displaying capacity and rack type
                          Row(
                              modifier = Modifier.fillMaxWidth().testTag("RowCapacityRack"),
                              horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f).testTag("CapacityColumn")) {
                                  Text(text = "Capacity:", fontWeight = FontWeight.Bold)
                                  Text(
                                      text = convertCapacityToString(curParking.capacity),
                                      color = Color.Gray)
                                }
                                Column(modifier = Modifier.weight(1f).testTag("RackTypeColumn")) {
                                  Text(text = "Rack Type:", fontWeight = FontWeight.Bold)
                                  Text(
                                      text = convertRackToString(curParking.rackType),
                                      color = Color.Gray)
                                }
                              }

                          // Row for displaying protection and price
                          Row(
                              modifier = Modifier.fillMaxWidth().testTag("RowProtectionPrice"),
                              horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f).testTag("ProtectionColumn")) {
                                  Text(text = "Protection:", fontWeight = FontWeight.Bold)
                                  Text(
                                      text = convertProtectionToString(curParking.protection),
                                      color = Color.Gray)
                                }
                                Column(modifier = Modifier.weight(1f).testTag("PriceColumn")) {
                                  Text(text = "Price:", fontWeight = FontWeight.Bold)
                                  val freeOrNot =
                                      if (curParking.price == 0.0) "Free"
                                      else curParking.price.toString()
                                  Text(text = freeOrNot, color = Color.Gray)
                                }
                              }

                          // Row for displaying if security is present
                          Row(
                              modifier = Modifier.fillMaxWidth().testTag("RowSecurity"),
                              horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f).testTag("SecurityColumn")) {
                                  Text(text = "Security Present:", fontWeight = FontWeight.Bold)
                                  val yesOrNo = if (curParking.hasSecurity) "Yes" else "No"
                                  Text(text = yesOrNo, color = Color.Gray)
                                }
                              }
                        }

                    // Column for action buttons like "Show in Map", "Add A Review", and "Report"
                    Column(
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .testTag("ButtonsColumn"), // Test tag for buttons column
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                          Button(
                              onClick = { /* Handle Return to Map */},
                              modifier =
                                  Modifier.fillMaxWidth()
                                      .height(60.dp)
                                      .testTag(
                                          "ShowInMapButton"), // Test tag for Show in Map button
                              colors =
                                  ButtonDefaults.buttonColors(
                                      containerColor = Cerulean, contentColor = Color.White)) {
                                Text(text = "Show in Map")
                              }

                          Button(
                              onClick = {},
                              modifier =
                                  Modifier.fillMaxWidth()
                                      .height(60.dp)
                                      .testTag("AddReviewButton"), // Test tag for Add Review button
                              colors =
                                  ButtonDefaults.buttonColors(
                                      containerColor = Cerulean, contentColor = Color.White)) {
                                Text(text = "Add A Review")
                              }

                          Button(
                              onClick = {},
                              modifier =
                                  Modifier.height(40.dp)
                                      .testTag("ReportButton"), // Test tag for Report button
                              colors =
                                  ButtonDefaults.buttonColors(
                                      containerColor = Red, contentColor = Color.White)) {
                                Text(text = "Report")
                              }
                        }

                    Spacer(modifier = Modifier.height(16.dp))
                  }
            }
      }
}
