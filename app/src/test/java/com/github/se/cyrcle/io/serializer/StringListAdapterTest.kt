package com.github.se.cyrcle.io.serializer

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StringListAdapterTest {

  private val stringListAdapter = StringListAdapter()

  @Test
  fun convertStringListTest() {
    fun testSerializationDeserialization(list: List<String>) {
      val serialized = stringListAdapter.serializeStringList(list)
      val deserialized = stringListAdapter.deserializeStringList(serialized)
      assertEquals(list, deserialized)
    }

    testSerializationDeserialization(listOf("a", "b", "c"))
    testSerializationDeserialization(listOf("x"))
    testSerializationDeserialization(listOf())
  }
}
