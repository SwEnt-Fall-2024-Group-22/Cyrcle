package com.github.se.cyrcle.io

import kotlinx.serialization.SerializationException
import org.junit.Assert.assertThrows
import org.junit.Test

class StorageSerializerTest {

  private val testClassForIoDefault = TestClassForIo.getDefaultInstance()
  private val defaultJson = "{\"data\":{\"name\":\"\",\"someInt\":0}}"

  private val testClassForIoSpecific = TestClassForIo("test", 69420)
  private val specificJson = "{\"data\":{\"name\":\"test\",\"someInt\":69420}}"

  @Test
  fun testSerialize() {
    val serializedTestClassForIoDefault = serialize(testClassForIoDefault)
    assert(serializedTestClassForIoDefault == defaultJson)

    val testClassForIo = TestClassForIo("test", 69420)
    val serializedTestClassForIoSpecific = serialize(testClassForIo)
    assert(serializedTestClassForIoSpecific == "{\"data\":{\"name\":\"test\",\"someInt\":69420}}")

    // TODO Verify special characters like ", \, etc.

    // TODO fuzzing
  }

  @Test
  fun testDeserializeWithGoodJson() {
    val deserializedTestClassForIoDefault = deserialize<TestClassForIo>(defaultJson)
    assert(deserializedTestClassForIoDefault == testClassForIoDefault)

    val deserializedTestClassForIoSpecific = deserialize<TestClassForIo>(specificJson)
    assert(deserializedTestClassForIoSpecific == TestClassForIo("test", 69420))

    // TODO Verify special characters like ", \, etc.

    // TODO fuzzing
  }

  @Test
  fun testDeserializeWithBadJson() {
    val badJson = "bad json"
    assertThrows(SerializationException::class.java) { deserialize<TestClassForIo>(badJson) }

    val emptyString = ""
    assertThrows(SerializationException::class.java) { deserialize<TestClassForIo>(emptyString) }
  }

  @Test
  fun testSerializeDeserialize() {
    val testClassForIo = testClassForIoDefault
    val serializedDefault = serialize(testClassForIo)
    val deserializedDefault = deserialize<TestClassForIo>(serializedDefault)
    assert(testClassForIoDefault == deserializedDefault)

    val serializerSpecific = serialize(testClassForIoSpecific)
    val deserializedSpecific = deserialize<TestClassForIo>(serializerSpecific)
    assert(testClassForIoSpecific == deserializedSpecific)

    // TODO Verify special characters like ", \, etc.

    // TODO fuzzing
  }
}
