package com.github.se.cyrcle.io.serializer

import androidx.room.TypeConverter
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.mapbox.geojson.Point
import java.lang.reflect.Type

/**
 * Adapter for serializing and deserializing Point objects. This is necessary as we need to store
 * the points as an object with two fields, longitude and latitude, in Firestore to be able to make
 * complex queries.
 */
class PointAdapter : JsonSerializer<Point>, JsonDeserializer<Point> {

  /**
   * Serializes a Point object to a JSON string with two fields, longitude and latitude. Used
   * internally by Room when this class is declared as a TypeConverter.
   *
   * @param point The Point object to serialize.
   * @return The serialized JSON string.
   */
  @TypeConverter
  fun serialize(point: Point): String {
    return JsonObject()
        .apply {
          addProperty("longitude", point.longitude())
          addProperty("latitude", point.latitude())
        }
        .toString()
  }

  /**
   * Deserializes a JSON string with two fields, longitude and latitude, to a Point object. Used
   * internally by Room when this class is declared as a TypeConverter.
   *
   * @param data The JSON string to deserialize.
   * @return The deserialized Point object.
   */
  @TypeConverter
  fun deserialize(data: String): Point {
    JsonParser.parseString(data).asJsonObject.let {
      val longitude = it.get("longitude").asDouble
      val latitude = it.get("latitude").asDouble
      return Point.fromLngLat(longitude, latitude)
    }
  }

  /**
   * Serializes a Point object to a JSON object with two fields, longitude and latitude. Used
   * internally by GSON when this class is declared as a TypeAdapter.
   *
   * @param src The Point object to serialize.
   * @param typeOfSrc The type of the source object.
   * @param context The serialization context.
   * @return The serialized JSON object.
   */
  override fun serialize(
      src: Point,
      typeOfSrc: Type,
      context: JsonSerializationContext
  ): JsonElement {
    return JsonObject().apply {
      addProperty("longitude", src.longitude())
      addProperty("latitude", src.latitude())
    }
  }

  /**
   * Deserializes a JSON object with two fields, longitude and latitude, to a Point object. Used
   * internally by GSON when this class is declared as a TypeAdapter.
   *
   * @param json The JSON object to deserialize.
   * @param typeOfT The type of the target object.
   * @param context The deserialization context.
   * @return The deserialized Point object.
   */
  override fun deserialize(
      json: JsonElement,
      typeOfT: Type,
      context: JsonDeserializationContext
  ): Point {
    val jsonObject = json.asJsonObject
    val longitude = jsonObject.get("longitude").asDouble
    val latitude = jsonObject.get("latitude").asDouble
    return Point.fromLngLat(longitude, latitude)
  }
}
