package com.github.se.cyrcle.model.zone

import com.mapbox.geojson.BoundingBox
import java.time.LocalTime
import java.util.UUID

data class Zone(
    val boundingBox: BoundingBox,
    val name: String,
    val lastRefreshed: LocalTime,
    val uid: String
) {
  /**
   * Zone constructor that set the time of the last refresh to the current time, and generates a new
   * UID.
   *
   * @param boundingBox The bounding box of the zone.
   * @name The name of the zone chosen by the user.
   */
  constructor(
      boundingBox: BoundingBox,
      name: String
  ) : this(boundingBox, name, LocalTime.now(), generateZoneUid())
}

/** Generates a new UID for a zone. */
private fun generateZoneUid(): String {
  return UUID.randomUUID().toString()
}
