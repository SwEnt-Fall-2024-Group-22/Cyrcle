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

  private val gson = GsonBuilder().registerTypeAdapter(Point::class.java, PointAdapter()).create()
  private val serializedParkingType = object : TypeToken<Map<String, Any>>() {}.type

  @TypeConverter
  fun serialize(parking: Parking): String {
    return gson.toJson(parking)
  }

  @TypeConverter
  fun deserialize(data: String): Parking {
    return gson.fromJson(data, Parking::class.java)
  }

  fun serializeParking(parking: Parking): Map<String, Any> {
    val json = gson.toJson(parking)
    return gson.fromJson(json, serializedParkingType)
  }

  fun deserializeParking(map: Map<String, Any>): Parking {
    val json = gson.toJson(map)
    return gson.fromJson(json, Parking::class.java)
  }

  override fun serialize(
      src: Parking,
      typeOfSrc: Type,
      context: JsonSerializationContext
  ): JsonElement {
    return gson.toJsonTree(src)
  }

  override fun deserialize(
      json: JsonElement,
      typeOfT: Type,
      context: JsonDeserializationContext
  ): Parking {
    return gson.fromJson(json, Parking::class.java)
  }
}
