package com.github.se.cyrcle.io.serializer

import androidx.room.TypeConverter
import com.github.se.cyrcle.model.parking.Parking
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

/** Adapter for serializing and deserializing sets of Parking objects. */
class ParkingSetAdapter {

  private val gson =
      GsonBuilder().registerTypeAdapter(Parking::class.java, ParkingAdapter()).create()

  private val parkingSetType = object : TypeToken<MutableSet<Parking>>() {}.type

  @TypeConverter
  fun serialize(parkings: MutableSet<Parking>): String {
    return gson.toJson(parkings)
  }

  @TypeConverter
  fun deserialize(data: String): MutableSet<Parking> {
    if (data.isBlank()) return mutableSetOf()
    return gson.fromJson(data, parkingSetType)
  }
}
