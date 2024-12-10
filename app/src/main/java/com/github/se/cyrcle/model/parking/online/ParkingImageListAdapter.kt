package com.github.se.cyrcle.model.parking.online

import androidx.room.TypeConverter
import com.github.se.cyrcle.model.parking.ParkingImage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ParkingImageListAdapter {

  private val gson = Gson()
  private val listType = object : TypeToken<List<ParkingImage>>() {}.type

  @TypeConverter
  fun fromParkingImageList(value: List<ParkingImage>?): String? {
    return gson.toJson(value)
  }

  @TypeConverter
  fun toParkingImageList(value: String?): List<ParkingImage>? {
    return gson.fromJson(value, listType)
  }
}
