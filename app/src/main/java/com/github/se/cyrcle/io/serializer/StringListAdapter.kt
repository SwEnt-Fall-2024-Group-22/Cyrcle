package com.github.se.cyrcle.io.serializer

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/** Adapter for serializing and deserializing a list of Strings. */
class StringListAdapter {

  private val gson = Gson()

  /** Type token for a list of Strings. */
  private val stringListType = object : TypeToken<List<String>>() {}.type

  /**
   * Serializes a list of Strings to a Json object.
   *
   * @param strList The list of Strings to serialize.
   * @return The serialized Json object.
   */
  @TypeConverter
  fun serializeStringList(strList: List<String>): String {
    return gson.toJson(strList)
  }

  /**
   * Deserializes a Json object to a list of Strings.
   *
   * @param data The Json object to deserialize.
   * @return The deserialized list of Strings.
   */
  @TypeConverter
  fun deserializeStringList(data: String): List<String> {
    return gson.fromJson(data, stringListType)
  }
}
