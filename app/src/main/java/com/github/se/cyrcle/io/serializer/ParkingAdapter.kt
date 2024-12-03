package com.github.se.cyrcle.io.serializer

import androidx.room.TypeConverter
import com.github.se.cyrcle.model.parking.Location
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingCapacity
import com.github.se.cyrcle.model.parking.ParkingProtection
import com.github.se.cyrcle.model.parking.ParkingRackType
import com.github.se.cyrcle.model.parking.TileUtils
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import com.mapbox.geojson.Point
import java.lang.reflect.Type

/**
 * Adapter for serializing and deserializing Parking objects. Functions annotated
 * with @TypeConverter are used by Room to convert the data type of a field in the database.
 */
class ParkingAdapter : JsonSerializer<Parking>, JsonDeserializer<Parking> {

  /** GSON instance with a custom adapter for Point objects. */
  private val gson =
      GsonBuilder()
          .registerTypeAdapter(Point::class.java, PointAdapter())
          .registerTypeAdapter(Location::class.java, LocationAdapter())
          .create()

  /** Type token for a map of Strings to Objects. */
  private val serializedParkingType = object : TypeToken<Map<String, Any>>() {}.type

  @TypeConverter
  fun serializeParkingCapacity(parkingCapacity: ParkingCapacity): Int {
    return parkingCapacity.ordinal
  }

  @TypeConverter
  fun deserializeParkingCapacity(ordinal: Int): ParkingCapacity {
    return ParkingCapacity.entries[ordinal]
  }

  @TypeConverter
  fun serializeParkingRackType(parkingRackType: ParkingRackType): Int {
    return parkingRackType.ordinal
  }

  @TypeConverter
  fun deserializeParkingRackType(ordinal: Int): ParkingRackType {
    return ParkingRackType.entries[ordinal]
  }

  @TypeConverter
  fun serializeParkingProtection(parkingProtection: ParkingProtection): Int {
    return parkingProtection.ordinal
  }

  @TypeConverter
  fun deserializeParkingProtection(ordinal: Int): ParkingProtection {
    return ParkingProtection.entries[ordinal]
  }

  /**
   * Serializes a Parking object to a map of Strings to Objects. Used within the
   * ParkingRepositoryFirestore to insert a parking in the database
   *
   * @param parking The Parking object to serialize.
   * @return The serialized map of Strings to Objects.
   */
  fun serializeParking(parking: Parking): Map<String, Any> {
    val json = gson.toJson(parking)
    return gson.fromJson(json, serializedParkingType)
  }

  /**
   * Deserializes a map of Strings to Objects to a Parking object. Used within the
   * ParkingRepositoryFirestore to retrieve a parking from the database
   *
   * @param map The map of Strings to Objects to deserialize.
   * @return The deserialized Parking object.
   */
  fun deserializeParking(map: Map<String, Any>): Parking {
    val json = gson.toJson(map)
    val tempParking = gson.fromJson(json, Parking::class.java)
    // Older versions of parking don't have the tile field
    return if (tempParking.tile == null)
        tempParking.copy(tile = TileUtils.getTileFromPoint(tempParking.location.center))
    else tempParking
  }

  /**
   * Serializes a Parking object to a JSON object. Used internally by GSON when this class is
   * declared as a TypeAdapter.
   *
   * @param src The Parking object to serialize.
   * @param typeOfSrc The type of the source object.
   * @param context The serialization context.
   * @return The serialized JSON object.
   */
  override fun serialize(
      src: Parking,
      typeOfSrc: Type,
      context: JsonSerializationContext
  ): JsonElement {
    return gson.toJsonTree(src)
  }

  /**
   * Deserializes a JSON object to a Parking object. Used internally by GSON when this class is
   * declared as a TypeAdapter.
   *
   * @param json The JSON object to deserialize.
   * @param typeOfT The type of the target object.
   * @param context The deserialization context.
   * @return The deserialized Parking object.
   */
  override fun deserialize(
      json: JsonElement,
      typeOfT: Type,
      context: JsonDeserializationContext
  ): Parking {
    return gson.fromJson(json, Parking::class.java)
  }
}
