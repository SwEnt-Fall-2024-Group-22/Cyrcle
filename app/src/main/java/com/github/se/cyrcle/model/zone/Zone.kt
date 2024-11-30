package com.github.se.cyrcle.model.zone

import android.content.Context
import com.mapbox.geojson.BoundingBox
import java.io.File
import java.time.LocalDate
import java.util.UUID
import org.json.JSONObject

const val ZONE_DIR = "zones"

data class Zone(
    val boundingBox: BoundingBox,
    val name: String,
    val lastRefreshed: LocalDate,
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
  ) : this(boundingBox, name, LocalDate.now(), generateZoneUid())

  /** Returns a JSON representation of the zone. */
  fun toJson(): String {
    return """
            {
                "boundingBox": {
                    "south": ${boundingBox.south()},
                    "west": ${boundingBox.west()},
                    "north": ${boundingBox.north()},
                    "east": ${boundingBox.east()}
                },
                "name": "$name",
                "lastRefreshed": "$lastRefreshed",
                "uid": "$uid"
            }
        """
        .trimIndent()
  }

  companion object {
    /**
     * Creates a new zone from a JSON representation.
     *
     * @param json The JSON representation of the zone.
     */
    private fun fromJson(json: String): Zone? {
      return try {
        val jsonObject = JSONObject(json)
        val boundingBox =
            BoundingBox.fromLngLats(
                jsonObject.getJSONObject("boundingBox").getDouble("west"),
                jsonObject.getJSONObject("boundingBox").getDouble("south"),
                jsonObject.getJSONObject("boundingBox").getDouble("east"),
                jsonObject.getJSONObject("boundingBox").getDouble("north"))
        val name = jsonObject.getString("name")
        val lastRefreshed = LocalDate.parse(jsonObject.getString("lastRefreshed"))
        val uid = jsonObject.getString("uid")
        Zone(boundingBox, name, lastRefreshed, uid)
      } catch (e: Exception) {
        null
      }
    }

    /**
     * Store a zone as a JSON file inside the directory passed as a parameter.
     *
     * @param zone The zone to store.
     * @param context The context of the application, necessary to access the file system.
     */
    fun storeZone(zone: Zone, context: Context) {
      val zoneDir = File(context.filesDir, ZONE_DIR)
      if (!zoneDir.exists()) {
        zoneDir.mkdirs()
      }
      File(zoneDir, "${zone.uid}.json").writeText(zone.toJson())
    }

    /**
     * Load all the zones stored in the directory.
     *
     * @param context The context of the application, necessary to access the file system.
     */
    fun loadZones(context: Context): List<Zone> {
      val zoneDir = File(context.filesDir, ZONE_DIR)
      if (!zoneDir.exists()) {
        zoneDir.mkdirs()
      }
      return zoneDir.listFiles()?.mapNotNull { file -> fromJson(file.readText()) } ?: emptyList()
    }

    /**
     * Delete a zone from the directory passed as a parameter.
     *
     * @param zone The zone to delete.
     */
    fun deleteZone(zone: Zone, context: Context) {
      val zoneDir = File(context.filesDir, ZONE_DIR)
      if (!zoneDir.exists()) return
      try {
        File(zoneDir, "${zone.uid}.json").delete()
      } catch (e: Exception) { // If the file can't be deleted, we just ignore the exception.
        return
      }
    }

    /**
     * Refreshs a zone by just replacing the lastRefreshed field with the current time. and stores
     * the updated zone.
     */
    fun refreshZone(zone: Zone, context: Context) {
      val updatedZone = zone.copy(lastRefreshed = LocalDate.now())
      storeZone(updatedZone, context)
    }
  }
}

/** Generates a new UID for a zone. */
private fun generateZoneUid(): String {
  return UUID.randomUUID().toString()
}
