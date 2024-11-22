package com.github.se.cyrcle.io

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SettingsSerializerTest {
  private val settings =
      Settings(
          "John Doe", // 0x4a, 0x6f, 0x68, 0x6e, 0x20, 0x44, 0x6f, 0x65
          0x4B4B4B4B,
          0x4C4C4C4C4C4C4C4CL,
          Float.fromBits(0x4D4D4D4D),
          true)
  private val serializedJohnDoe =
      byteArrayOf(
          0x4a,
          0x6f,
          0x68,
          0x6e,
          0x20,
          0x44,
          0x6f,
          0x65,
          0x00,
          0x4b,
          0x4b,
          0x4b,
          0x4b,
          0x4c,
          0x4c,
          0x4c,
          0x4c,
          0x4c,
          0x4c,
          0x4c,
          0x4c,
          0x4d,
          0x4d,
          0x4d,
          0x4d,
          0x01)

  @Test
  fun testSerialize() {
    val outStream = ByteArrayOutputStream()
    val serializer = Settings.Serializer()
    runBlocking { serializer.writeTo(settings, outStream) }
    assert(outStream.toByteArray().contentEquals(serializedJohnDoe)) { "Serialization failed" }
  }

  @Test
  fun testDeserialize() {
    val inputStream = ByteArrayInputStream(serializedJohnDoe)
    val serializer = Settings.Serializer()
    val deserializedSettings = runBlocking { serializer.readFrom(inputStream) }
    assert(settings == deserializedSettings) { "Deserialization failed" }
  }

  @Test
  fun testSerializeAndDeserialize() {
    val serializer = Settings.Serializer()
    val outStream = ByteArrayOutputStream()

    runBlocking { serializer.writeTo(settings, outStream) }

    val inputStream = ByteArrayInputStream(outStream.toByteArray())
    val deserializedSettings = runBlocking { serializer.readFrom(inputStream) }

    assert(settings == deserializedSettings) { "Deserialized settings do not match the original" }
  }
}
