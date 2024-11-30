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

  @TypeConverter
  fun serialize(point: Point): String {
    return JsonObject()
        .apply {
          addProperty("longitude", point.longitude())
          addProperty("latitude", point.latitude())
        }
        .toString()
  }

  @TypeConverter
  fun deserialize(data: String): Point {
    JsonParser.parseString(data).asJsonObject.let {
      val longitude = it.get("longitude").asDouble
      val latitude = it.get("latitude").asDouble
      return Point.fromLngLat(longitude, latitude)
    }
  }

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
