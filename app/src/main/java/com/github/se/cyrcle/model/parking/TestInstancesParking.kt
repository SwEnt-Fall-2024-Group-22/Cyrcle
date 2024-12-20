package com.github.se.cyrcle.model.parking

import com.github.se.cyrcle.model.user.TestInstancesUser
import com.mapbox.geojson.Point

object TestInstancesParking {

  val referencePoint: Point = Point.fromLngLat(6.55, 46.65)
  val EPFLCenter: Point = Point.fromLngLat(6.566397, 46.518467)
  // This parking has to be close to the center point of the map launched by the end2end test
  val parking1 =
      Parking(
          "Test_spot_1",
          "Rue de la paix",
          null,
          Location(EPFLCenter),
          listOf("parkings/Test_spot_1/1.jpg"),
          emptyList(),
          0,
          emptyList(),
          ParkingCapacity.LARGE,
          ParkingRackType.TWO_TIER,
          ParkingProtection.COVERED,
          0.0,
          true,
          owner = TestInstancesUser.user1.public.userId,
          reportingUsers = emptyList(),
          2,
          3.0)
  val parking2 =
      Parking(
          "Test_spot_2",
          "Rude épais",
          null,
          Location(Point.fromLngLat(6.7, 46.3)),
          listOf("parkings/Test_spot_2/1.jpg", "parkings/Test_spot_2/2.jpg"),
          emptyList(),
          0,
          emptyList(),
          ParkingCapacity.SMALL,
          ParkingRackType.TWO_TIER,
          ParkingProtection.COVERED,
          0.0,
          true,
          owner = TestInstancesUser.user1.public.userId,
          reportingUsers = emptyList())
  val parking3 =
      Parking(
          "Test_spot_3",
          "Rue du pet",
          null,
          Location(Point.fromLngLat(7.1, 47.1)),
          listOf(
              "https://upload.wikimedia.org/wikipedia/commons/7/78/%22G%C3%A4nsemarkt%22_in_Amance_-_panoramio.jpg"),
          emptyList(),
          0,
          emptyList(),
          ParkingCapacity.LARGE,
          ParkingRackType.TWO_TIER,
          ParkingProtection.COVERED,
          0.0,
          true,
          owner = TestInstancesUser.user1.public.userId,
          reportingUsers = emptyList())
  val parking4 =
      Parking(
          "Test_spot_4",
          "",
          null,
          Location(Point.fromLngLat(7.111, 47.111)),
          listOf(
              "https://upload.wikimedia.org/wikipedia/commons/7/78/%22G%C3%A4nsemarkt%22_in_Amance_-_panoramio.jpg"),
          emptyList(),
          0,
          emptyList(),
          ParkingCapacity.LARGE,
          ParkingRackType.TWO_TIER,
          ParkingProtection.COVERED,
          0.0,
          true,
          owner = TestInstancesUser.user1.public.userId,
          reportingUsers = emptyList())
  val parking5 =
      Parking(
          "Test_spot_5",
          "",
          null,
          Location(Point.fromLngLat(7.112, 47.112)),
          listOf(
              "https://upload.wikimedia.org/wikipedia/commons/7/78/%22G%C3%A4nsemarkt%22_in_Amance_-_panoramio.jpg"),
          emptyList(),
          0,
          emptyList(),
          ParkingCapacity.LARGE,
          ParkingRackType.TWO_TIER,
          ParkingProtection.COVERED,
          0.0,
          true,
          owner = TestInstancesUser.user1.public.userId,
          reportingUsers = emptyList())
}
