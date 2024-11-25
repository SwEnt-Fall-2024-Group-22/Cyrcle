package com.github.se.cyrcle.io

import androidx.datastore.core.Serializer
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

private const val END_OF_STRING_CHAR = 0.toChar()
private const val END_OF_STREAM = -1

private const val SERIALIZE_FALSE = 0
private const val SERIALIZE_TRUE = 1

/**
 * A class to easily serialize and deserialize class with basic variables inside. An example is
 * available in the test folder, under Settings.
 */
abstract class ProtoGenericSerializer<T>(
    override val defaultValue: T,
) : Serializer<T> {

  companion object {
    private fun writeNBytes(output: OutputStream, byteArray: ByteArray) {
      for (byte in byteArray) output.write(byte.toInt())
    }

    /**
     * Read a fixed number of bytes from the input stream.
     *
     * @param output The [OutputStream] to write to.
     * @param value The [String] to write.
     */
    fun writeString(output: OutputStream, value: String) {
      writeNBytes(output, value.toByteArray())
      output.write(END_OF_STRING_CHAR.code)
    }

    /**
     * Read a fixed number of bytes from the input stream.
     *
     * @param input The [InputStream] to read from.
     * @return The [String] read.
     * @throws IOException If an error occurred during deserialization (end of stream was reached).
     */
    fun readString(input: InputStream): String {
      val stringBuilder = StringBuilder()
      do {
        val newByte = input.read()
        if (newByte == END_OF_STREAM) throw IOException("End of stream reached")
        val newChar = newByte.toChar()
        stringBuilder.append(newChar)
      } while (newChar != END_OF_STRING_CHAR)
      // Remove the END_OF_STRING_CHAR that was added.
      stringBuilder.deleteCharAt(stringBuilder.length - 1)
      return stringBuilder.toString()
    }

    /**
     * Write a 32-bit integer to the output stream.
     *
     * @param output The [OutputStream] to write to.
     * @param value The [Int] to write.
     */
    fun writeInt32(output: OutputStream, value: Int) {
      val byteArray = ByteArray(Int.SIZE_BYTES)
      for (i in 0 until Int.SIZE_BYTES) byteArray[i] = (value shr 8 * i).toByte()
      writeNBytes(output, byteArray)
    }

    /**
     * Read a 32-bit integer from the input stream.
     *
     * @param input The [InputStream] to read from.
     * @return The [Int] read.
     * @throws IOException If an error occurred during deserialization (end of stream was reached).
     */
    fun readInt32(input: InputStream): Int {
      var value = 0
      for (i in 0 until Int.SIZE_BYTES) {
        val newByte = input.read()
        if (newByte == END_OF_STREAM) throw IOException("End of stream reached")
        value = value.shl(8) or newByte
      }
      return value
    }

    /**
     * Write a 64-bit integer to the output stream.
     *
     * @param output The [OutputStream] to write to.
     * @param value The [Long] to write.
     */
    fun writeLong64(output: OutputStream, value: Long) {
      val byteArray = ByteArray(Long.SIZE_BYTES)
      for (i in 0 until Long.SIZE_BYTES) byteArray[i] = (value shr 8 * i).toByte()
      writeNBytes(output, byteArray)
    }

    /**
     * Read a 64-bit integer from the input stream.
     *
     * @param input The [InputStream] to read from.
     * @return The [Long] read.
     * @throws IOException If an error occurred during deserialization (end of stream was reached).
     */
    fun readLong64(input: InputStream): Long {
      var value: Long = 0
      for (i in 0 until Long.SIZE_BYTES) {
        val newByte = input.read()
        if (newByte == END_OF_STREAM) throw IOException("End of stream reached")
        value = value.shl(8) or newByte.toLong()
      }
      return value
    }

    /**
     * Write a 32-bit floating point number to the output stream.
     *
     * @param output The [OutputStream] to write to.
     * @param value The [Float] to write.
     */
    fun writeFloat32(output: OutputStream, value: Float) {
      val valueRaw = value.toRawBits()
      val byteArray = ByteArray(Float.SIZE_BYTES)
      for (i in 0 until Float.SIZE_BYTES) byteArray[i] = (valueRaw shr 8 * i).toByte()
      writeNBytes(output, byteArray)
    }

    /**
     * Read a 32-bit floating point number from the input stream.
     *
     * @param input The [InputStream] to read from.
     * @return The [Float] read.
     * @throws IOException If an error occurred during deserialization (end of stream was reached).
     */
    fun readFloat32(input: InputStream): Float {
      var value = 0
      for (i in 0 until Float.SIZE_BYTES) {
        val newByte = input.read()
        if (newByte == END_OF_STREAM) throw IOException("End of stream reached")
        value = value.shl(8) or newByte
      }
      return Float.fromBits(value)
    }

    /**
     * Write a 64-bit floating point number to the output stream.
     *
     * @param output The [OutputStream] to write to.
     * @param value The [Double] to write.
     */
    fun writeDouble64(output: OutputStream, value: Double) {
      val valueRaw = value.toRawBits()
      val byteArray = ByteArray(Double.SIZE_BYTES)
      for (i in 0 until Double.SIZE_BYTES) byteArray[i] = (valueRaw shr 8 * i).toByte()
      writeNBytes(output, byteArray)
    }

    /**
     * Read a 64-bit floating point number from the input stream.
     *
     * @param input The [InputStream] to read from.
     * @return The [Double] read.
     * @throws IOException If an error occurred during deserialization (end of stream was reached).
     */
    fun readDouble64(input: InputStream): Double {
      var value: Long = 0
      for (i in 0 until Double.SIZE_BYTES) {
        val newByte = input.read()
        if (newByte == END_OF_STREAM) throw IOException("End of stream reached")
        value = value.shl(8) or newByte.toLong()
      }
      return Double.fromBits(value)
    }

    /**
     * Write a boolean to the output stream.
     *
     * @param output The [OutputStream] to write to.
     * @param value The [Boolean] to write.
     */
    fun writeBoolean(output: OutputStream, value: Boolean) {
      output.write(if (value) 1 else 0)
    }

    /**
     * Read a boolean from the input stream.
     *
     * @param input The [InputStream] to read from.
     * @return The [Boolean] read.
     * @throws IOException If the serialized value is not a valid boolean.
     */
    fun readBoolean(input: InputStream): Boolean {
      val newByte = input.read()
      if (newByte == END_OF_STREAM) throw IOException("End of stream reached")
      return when (newByte) {
        SERIALIZE_FALSE -> false
        SERIALIZE_TRUE -> true
        else -> throw IOException("Invalid serialized boolean value: [$newByte]")
      }
    }
  }
}
