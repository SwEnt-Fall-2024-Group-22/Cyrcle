package com.github.se.cyrcle.model.parking

import com.mapbox.geojson.Point

object TestInstancesParking {
  val parking1 =
      Parking(
          "Test_spot_1",
          null,
          null,
          Location(Point.fromLngLat(6.6, 46.2)),
          listOf(
              "https://upload.wikimedia.org/wikipedia/commons/7/78/%22G%C3%A4nsemarkt%22_in_Amance_-_panoramio.jpg"),
          ParkingCapacity.LARGE,
          ParkingRackType.TWO_TIER,
          ParkingProtection.COVERED,
          0.0,
          true)
  val parking2 =
      Parking(
          "Test_spot_2",
          "Avenue de la Gare",
          null,
          Location(Point.fromLngLat(6.7, 46.3)),
          listOf(
              "https://upload.wikimedia.org/wikipedia/commons/7/78/%22G%C3%A4nsemarkt%22_in_Amance_-_panoramio.jpg",
              "https://upload.wikimedia.org/wikipedia/commons/7/78/%22G%C3%A4nsemarkt%22_in_Amance_-_panoramio.jpg"),
          ParkingCapacity.SMALL,
          ParkingRackType.TWO_TIER,
          ParkingProtection.COVERED,
          0.0,
          true)
  val parking3 =
      Parking(
          "Test_spot_3",
          null,
          null,
          Location(Point.fromLngLat(7.1, 47.1)),
          listOf(
              "https://upload.wikimedia.org/wikipedia/commons/7/78/%22G%C3%A4nsemarkt%22_in_Amance_-_panoramio.jpg"),
          ParkingCapacity.LARGE,
          ParkingRackType.TWO_TIER,
          ParkingProtection.COVERED,
          0.0,
          true)
}
