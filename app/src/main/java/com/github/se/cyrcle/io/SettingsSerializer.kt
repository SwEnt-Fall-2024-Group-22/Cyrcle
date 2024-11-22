package com.github.se.cyrcle.io

import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream

private const val END_OF_STRING_CHAR = 0.toChar()
private const val END_OF_STREAM_CHAR = (-1).toChar()

class SettingsSerializer : Serializer<Settings> {
  override val defaultValue: Settings
    get() = Settings("John Doe", 42, 420000, 4.2f, true)

  override suspend fun readFrom(input: InputStream): Settings {
    return Settings(
        readString(input),
        readInt32(input),
        readLong64(input),
        readFloat32(input),
        readBoolean(input))
  }

  override suspend fun writeTo(t: Settings, output: OutputStream) {
    writeString(output, t.userName)
    writeInt32(output, t.age)
    writeLong64(output, t.ageSinceEpoch)
    writeFloat32(output, t.heightInCm)
    writeBoolean(output, t.isDeveloper)
  }

  companion object {
    private fun writeNByte(output: OutputStream, byteArray: ByteArray) {
      for (byte in byteArray) output.write(byte.toInt())
    }

    fun writeString(output: OutputStream, value: String) {
      writeNByte(output, value.toByteArray())
      output.write(END_OF_STRING_CHAR.toInt())
    }

    fun readString(input: InputStream): String {
      val stringBuilder = StringBuilder()
      do {
        val byte = input.read().toChar()
        if (byte == END_OF_STRING_CHAR) return stringBuilder.toString()
        stringBuilder.append(byte)
      } while (byte != END_OF_STREAM_CHAR)
      return ""
    }

    fun writeInt32(output: OutputStream, value: Int) {
      val byteArray = ByteArray(Int.SIZE_BYTES)
      for (i in 0 until Int.SIZE_BYTES) byteArray[i] = (value shr 8 * i).toByte()
      writeNByte(output, byteArray)
    }

    fun readInt32(input: InputStream): Int {
      var value: Int = 0
      for (i in 0 until Int.SIZE_BYTES) value = value.shl(8) or input.read()
      return value
    }

    fun writeLong64(output: OutputStream, value: Long) {
      val byteArray = ByteArray(Long.SIZE_BYTES)
      for (i in 0 until Long.SIZE_BYTES) byteArray[i] = (value shr 8 * i).toByte()
      writeNByte(output, byteArray)
    }

    fun readLong64(input: InputStream): Long {
      var value: Long = 0
      for (i in 0 until Long.SIZE_BYTES) value = value.shl(8) or input.read().toLong()
      return value
    }

    fun writeFloat32(output: OutputStream, value: Float) {
      val valueRaw = value.toRawBits()
      val byteArray = ByteArray(Float.SIZE_BYTES)
      for (i in 0 until Float.SIZE_BYTES) byteArray[i] = (valueRaw shr 8 * i).toByte()
      writeNByte(output, byteArray)
    }

    fun readFloat32(input: InputStream): Float {
      var value: Int = 0
      for (i in 0 until Float.SIZE_BYTES) value = value.shl(8) or input.read().toInt()
      return Float.fromBits(value)
    }

    fun writeDouble64(output: OutputStream, value: Double) {
      val valueRaw = value.toRawBits()
      val byteArray = ByteArray(Double.SIZE_BYTES)
      for (i in 0 until Double.SIZE_BYTES) byteArray[i] = (valueRaw shr 8 * i).toByte()
      writeNByte(output, byteArray)
    }

    fun readDouble64(input: InputStream): Double {
      var value: Long = 0
      for (i in 0 until Double.SIZE_BYTES) value = value.shl(8) or input.read().toLong()
      return value.toDouble()
    }

    fun writeBoolean(output: OutputStream, value: Boolean) {
      output.write(if (value) 1 else 0)
    }

    fun readBoolean(input: InputStream): Boolean {
      return when (input.read()) {
        0 -> false
        1 -> true
        else -> true // throw IllegalArgumentException("Invalid boolean value")
      }
    }
  }
}
