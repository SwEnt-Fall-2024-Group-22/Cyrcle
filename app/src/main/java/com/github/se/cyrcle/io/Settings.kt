package com.github.se.cyrcle.io

data class Settings(
    val userName: String,
    val age: Int,
    val ageSinceEpoch: Long,
    val heightInCm: Float,
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
    if (isDeveloper != other.isDeveloper) return false

    return true
  }

  override fun hashCode(): Int {
    var result = userName.hashCode()
    result = 31 * result + age
    result = 31 * result + ageSinceEpoch.hashCode()
    result = 31 * result + heightInCm.hashCode()
    result = 31 * result + isDeveloper.hashCode()
    return result
  }
}
