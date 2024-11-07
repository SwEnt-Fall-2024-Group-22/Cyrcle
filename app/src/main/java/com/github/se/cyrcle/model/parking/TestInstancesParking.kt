package com.github.se.cyrcle.model.parking

import com.mapbox.geojson.Point

object TestInstancesParking {

  val referencePoint = Point.fromLngLat(6.55, 46.65)
  val EPFLCenter = Point.fromLngLat(6.566397, 46.518467)

  val parkingEnd2End =
      Parking(
          "E2E",
          null,
          null,
          Location(EPFLCenter),
          listOf(
              "https://upload.wikimedia.org/wikipedia/commons/7/78/%22G%C3%A4nsemarkt%22_in_Amance_-_panoramio.jpg"),
          ParkingCapacity.LARGE,
          ParkingRackType.TWO_TIER,
          ParkingProtection.COVERED,
          0.0,
          true,
          2,
          3.0)
  val parking1 =
      Parking(
          "Test_spot_1",
          "Rue de la paix",
          null,
          Location(Point.fromLngLat(6.6, 46.2)),
          listOf(
              "https://upload.wikimedia.org/wikipedia/commons/7/78/%22G%C3%A4nsemarkt%22_in_Amance_-_panoramio.jpg"),
          ParkingCapacity.LARGE,
          ParkingRackType.TWO_TIER,
          ParkingProtection.COVERED,
          0.0,
          true,
          2,
          3.0)
  val parking2 =
      Parking(
          "Test_spot_2",
          "Rude épais",
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
          "Rue du pet",
          null,
          Location(Point.fromLngLat(7.1, 47.1)),
          listOf(
              "https://upload.wikimedia.org/wikipedia/commons/7/78/%22G%C3%A4nsemarkt%22_in_Amance_-_panoramio.jpg"),
          ParkingCapacity.LARGE,
          ParkingRackType.TWO_TIER,
          ParkingProtection.COVERED,
          0.0,
          true)
  val parking4 =
      Parking(
          "Test_spot_4",
          "",
          null,
          Location(Point.fromLngLat(7.111, 47.111)),
          listOf(
              "https://upload.wikimedia.org/wikipedia/commons/7/78/%22G%C3%A4nsemarkt%22_in_Amance_-_panoramio.jpg"),
          ParkingCapacity.LARGE,
          ParkingRackType.TWO_TIER,
          ParkingProtection.COVERED,
          0.0,
          true)
  val parking5 =
      Parking(
          "Test_spot_5",
          "",
          null,
          Location(Point.fromLngLat(7.112, 47.112)),
          listOf(
              "https://upload.wikimedia.org/wikipedia/commons/7/78/%22G%C3%A4nsemarkt%22_in_Amance_-_panoramio.jpg"),
          ParkingCapacity.LARGE,
          ParkingRackType.TWO_TIER,
          ParkingProtection.COVERED,
          0.0,
          true)
}
