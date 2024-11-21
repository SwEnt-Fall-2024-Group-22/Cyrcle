package com.github.se.cyrcle.io

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * A generic serializable class that holds an object of type T to ease the serialization and
 * deserialization of objects.
 */
@Serializable data class GenericClass<T>(val data: T)

/**
 * Serialize an object of type T into a JSON string.
 *
 * @param obj The object to serialize.
 * @return The serialized JSON string.
 */
inline fun <reified T> serialize(obj: T): String {
  return Json.encodeToString(GenericClass(obj))
}

/**
 * Deserialize a JSON string into an object of type T.
 *
 * @param jsonString The JSON string to deserialize.
 * @return The deserialized object of type T.
 */
inline fun <reified T> deserialize(jsonString: String): T? {
  val genericClass: GenericClass<T> = Json.decodeFromString(jsonString)
  return genericClass.data
}
