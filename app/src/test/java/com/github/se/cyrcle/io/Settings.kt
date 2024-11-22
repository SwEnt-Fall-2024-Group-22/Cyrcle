package com.github.se.cyrcle.io

import java.io.InputStream
import java.io.OutputStream

data class Settings(
    val userName: String,
    val age: Int,
    val ageSinceEpoch: Long,
    val heightInCm: Float,
    val weightInMg: Double,
    val isDeveloper: Boolean
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Settings

    if (userName != other.userName) return false
    if (age != other.age) return false
    if (ageSinceEpoch != other.ageSinceEpoch) return false
    if (heightInCm != other.heightInCm) return false
    if (weightInMg != other.weightInMg) return false
    if (isDeveloper != other.isDeveloper) return false

    return true
  }

  override fun hashCode(): Int {
    var result = userName.hashCode()
    result = 31 * result + age
    result = 31 * result + ageSinceEpoch.hashCode()
    result = 31 * result + heightInCm.hashCode()
    result = 31 * result + weightInMg.hashCode()
    result = 31 * result + isDeveloper.hashCode()
    return result
  }

  class Serializer :
      ProtoGenericSerializer<Settings>(Settings("John Doe", 42, 420000, 4.2f, 4.425397, true)) {
    override suspend fun readFrom(input: InputStream): Settings {
      return Settings(
          readString(input),
          readInt32(input),
          readLong64(input),
          readFloat32(input),
          readDouble64(input),
          readBoolean(input))
    }

    override suspend fun writeTo(t: Settings, output: OutputStream) {
      writeString(output, t.userName)
      writeInt32(output, t.age)
      writeLong64(output, t.ageSinceEpoch)
      writeFloat32(output, t.heightInCm)
      writeDouble64(output, t.weightInMg)
      writeBoolean(output, t.isDeveloper)
    }
  }
}
