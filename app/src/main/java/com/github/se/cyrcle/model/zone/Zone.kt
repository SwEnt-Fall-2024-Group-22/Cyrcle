package com.github.se.cyrcle.model.zone

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.gson.BoundingBoxTypeAdapter
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

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
    return gson.toJson(this)
  }

  companion object {
    /**
     * Creates a new Gson instance with the necessary type adapters to serialize and deserialize a
     * Zone
     */
    private val gson: Gson =
        GsonBuilder()
            .registerTypeAdapter(BoundingBox::class.java, BoundingBoxTypeAdapter())
            .registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter())
            .create()

    /**
     * Creates a new zone from a JSON representation.
     *
     * @param json The JSON representation of the zone.
     */
    private fun fromJson(json: String): Zone? {
      return try {
        gson.fromJson(json, Zone::class.java)
      } catch (e: Exception) {
        // If the JSON can't be parsed, we return null, the UI will filter out the invalid zones.
        null
      }
    }

    fun createZone(boundingBox: BoundingBox, name: String, context: Context): Zone {
      val zone = Zone(boundingBox, name)
      storeZone(zone, context)
      return zone
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

class LocalDateTypeAdapter : TypeAdapter<LocalDate>() {
  private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

  override fun write(out: JsonWriter, value: LocalDate) {
    out.value(value.format(formatter))
  }

  override fun read(`in`: JsonReader?): LocalDate {
    return LocalDate.parse(`in`?.nextString(), formatter)
  }
}
