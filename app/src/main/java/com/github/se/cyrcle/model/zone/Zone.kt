package com.github.se.cyrcle.model.zone

import com.mapbox.geojson.BoundingBox
import org.json.JSONObject
import java.io.File
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

    /**
     * Returns a JSON representation of the zone.
     */
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
        """.trimIndent()
    }
    companion object {
        /**
         * Creates a new zone from a JSON representation.
         * @param json The JSON representation of the zone.
         */
        fun fromJson(json: String): Zone {
            val jsonObject = JSONObject(json)
            val boundingBox = BoundingBox.fromLngLats(
                jsonObject.getJSONObject("boundingBox").getDouble("west"),
                jsonObject.getJSONObject("boundingBox").getDouble("south"),
                jsonObject.getJSONObject("boundingBox").getDouble("east"),
                jsonObject.getJSONObject("boundingBox").getDouble("north")
            )
            val name = jsonObject.getString("name")
            val lastRefreshed = LocalTime.parse(jsonObject.getString("lastRefreshed"))
            val uid = jsonObject.getString("uid")
            return Zone(boundingBox, name, lastRefreshed, uid)
        }

        /**
         * Store a zone as a JSON file inside the directory passed as a parameter.
         * @param zone The zone to store.
         * @param zoneDir The directory where the zone will be stored.
         */
        fun storeZone(zone: Zone, zoneDir: File) {
            if (!zoneDir.exists()) {
                zoneDir.mkdirs()
            }
            File(zoneDir, "${zone.uid}.json").writeText(zone.toJson())
        }

        /**
         * Load all the zones stored in the directory passed as a parameter.
         * @param zoneDir The directory where the zones are stored.
         */
        fun loadZones(zoneDir: File): List<Zone> {
            if (!zoneDir.exists()) {
               zoneDir.mkdirs()
            }
            return zoneDir.listFiles()?.map { file -> fromJson(file.readText()) } ?: emptyList()
        }
    }
}

/** Generates a new UID for a zone. */
private fun generateZoneUid(): String {
    return UUID.randomUUID().toString()
}
