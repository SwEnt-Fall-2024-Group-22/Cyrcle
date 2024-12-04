package com.github.se.cyrcle.io.serializer

import androidx.room.TypeConverter
import com.github.se.cyrcle.model.parking.Location
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.mapbox.geojson.Point
import java.lang.reflect.Type

/** Adapter for serializing and deserializing Location objects. */
class LocationAdapter : JsonSerializer<Location>, JsonDeserializer<Location> {

  private val gson = GsonBuilder().registerTypeAdapter(Point::class.java, PointAdapter()).create()

  /**
   * Serializes a Location object to a Json string.
   *
   * @param location The Location object to serialize.
   * @return The serialized Json string.
   */
  @TypeConverter
  fun serializeLocation(location: Location): String {
    return gson.toJson(location)
  }

  /**
   * Deserializes a Json object to a Location object.
   *
   * @param data The Json string to deserialize.
   * @return The deserialized Location object.
   */
  @TypeConverter
  fun deserializeLocation(data: String): Location {
    return gson.fromJson(data, Location::class.java)
  }

  /**
   * Serializes a Location object to a Json object.
   *
   * @param src The Location object to serialize.
   * @param typeOfSrc The type of the source object.
   * @param context The serialization context.
   * @return The serialized Json object.
   */
  override fun serialize(
      src: Location?,
      typeOfSrc: Type?,
      context: JsonSerializationContext?
  ): JsonElement {
    return gson.toJsonTree(src)
  }

  /**
   * Deserializes a Json object to a Location object.
   *
   * @param json The Json object to deserialize.
   * @param typeOfT The type of the target object.
   * @param context The deserialization context.
   * @return The deserialized Location object.
   */
  override fun deserialize(
      json: JsonElement?,
      typeOfT: Type?,
      context: JsonDeserializationContext?
  ): Location {
    return gson.fromJson(json, Location::class.java)
  }
}
