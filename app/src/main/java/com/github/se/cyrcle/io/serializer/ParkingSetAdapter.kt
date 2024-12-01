package com.github.se.cyrcle.io.serializer

import androidx.room.TypeConverter
import com.github.se.cyrcle.model.parking.Parking
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

/** Adapter for serializing and deserializing sets of Parking objects. */
class ParkingSetAdapter {

  /** GSON instance with a custom adapter for Parking objects. */
  private val gson =
      GsonBuilder().registerTypeAdapter(Parking::class.java, ParkingAdapter()).create()

  /** Type token for a mutable set of Parking objects. */
  private val parkingSetType = object : TypeToken<MutableSet<Parking>>() {}.type

  /**
   * Serializes a set of Parking objects to a JSON string. Used internally by Room when this class
   * is declared as a TypeConverter.
   *
   * @param parkings The set of Parking objects to serialize.
   * @return The serialized JSON string.
   */
  @TypeConverter
  fun serialize(parkings: MutableSet<Parking>): String {
    return gson.toJson(parkings)
  }

  /**
   * Deserializes a JSON string to a set of Parking objects. Used internally by Room when this class
   * is declared as a TypeConverter.
   *
   * @param data The JSON string to deserialize.
   * @return The deserialized set of Parking objects.
   */
  @TypeConverter
  fun deserialize(data: String): MutableSet<Parking> {
    if (data.isBlank()) return mutableSetOf()
    return gson.fromJson(data, parkingSetType)
  }
}
