package com.github.se.cyrcle.io.serializer

import androidx.room.TypeConverter
import com.github.se.cyrcle.model.parking.Parking
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import com.mapbox.geojson.Point
import java.lang.reflect.Type

/** Adapter for serializing and deserializing Parking objects. */
class ParkingAdapter : JsonSerializer<Parking>, JsonDeserializer<Parking> {

  /** GSON instance with a custom adapter for Point objects. */
  private val gson = GsonBuilder().registerTypeAdapter(Point::class.java, PointAdapter()).create()

  /** Type token for a map of Strings to Objects. */
  private val serializedParkingType = object : TypeToken<Map<String, Any>>() {}.type

  /**
   * Serializes a Parking object to a JSON string. Used internally by Room when this class is
   * declared as a TypeConverter.
   *
   * @param parking The Parking object to serialize.
   * @return The serialized JSON string.
   */
  @TypeConverter
  fun serialize(parking: Parking): String {
    return gson.toJson(parking)
  }

  /**
   * Deserializes a JSON string to a Parking object. Used internally by Room when this class is
   * declared as a TypeConverter.
   *
   * @param data The JSON string to deserialize.
   * @return The deserialized Parking object.
   */
  @TypeConverter
  fun deserialize(data: String): Parking {
    return gson.fromJson(data, Parking::class.java)
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
    return gson.fromJson(json, Parking::class.java)
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
