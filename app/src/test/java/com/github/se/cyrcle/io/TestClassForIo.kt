package com.github.se.cyrcle.io

import kotlinx.serialization.Serializable

@Serializable
class TestClassForIo(val name: String, val someInt: Int) {
  companion object {
    fun getDefaultInstance(): TestClassForIo {
      return TestClassForIo("", 0)
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as TestClassForIo

    if (name != other.name) return false
    if (someInt != other.someInt) return false

    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + someInt
    return result
  }
}
