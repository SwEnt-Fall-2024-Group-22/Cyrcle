package com.github.se.cyrcle.model.parking

object TestInstancesParking {
  val parking1 =
      Parking(
          "Test_spot_1",
          null,
          null,
          Location(Point(46.2, 6.6)),
          listOf(
              "https://en.wikipedia.org/wiki/Bicycle_parking#/media/File:Bicycle_parking_at_Alewife_station,_August_2001.jpg"),
          ParkingCapacity.LARGE,
          ParkingRackType.TWO_TIER,
          ParkingProtection.COVERED,
          0.0,
          true)
  val parking2 =
      Parking(
          "Test_spot_2",
          null,
          null,
          Location(Point(46.3, 6.7)),
          listOf(
              "https://en.wikipedia.org/wiki/Bicycle_parking#/media/File:Bicycle_parking_at_Alewife_station,_August_2001.jpg"),
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
          Location(Point(47.1, 7.1)),
          listOf(
              "https://en.wikipedia.org/wiki/Bicycle_parking#/media/File:Bicycle_parking_at_Alewife_station,_August_2001.jpg"),
          ParkingCapacity.LARGE,
          ParkingRackType.TWO_TIER,
          ParkingProtection.COVERED,
          0.0,
          true)
}
