package com.github.se.cyrcle.model.user

const val MAX_NOTE_LENGTH = 48

/**
 * Data class representing the public information of a user.
 *
 * @property userId The unique identifier of the user.
 * @property username The username of the user.
 * @property profilePictureCloudPath The path to the profile picture of the user on the cloud
 *   storage. (Not implemented)
 */
data class UserPublic(
    val userId: String,
    val username: String,
    val profilePictureCloudPath: String? = "",
    // val accountCreationDate: Timestamp? = null,
    // val numberOfContributedSpots: Int = 0,
    val userReputationScore: Double = 0.0
)

/**
 * Data class representing the private information of a user.
 *
 * @property firstName The first name of the user.
 * @property lastName The last name of the user.
 * @property email The email of the user.
 * @property favoriteParkings The list of favorite parkings of the user.
 * @property wallet The wallet containing the coins of the user.
 */
data class UserDetails(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val reportedParkings: List<String> = emptyList(),
    val reportedReviews: List<String> = emptyList(),
    val reportedImages: List<String> = emptyList(),
    val userImages: List<String> =
        emptyList(), // Only destinationPaths are stored to load from repository
    val reviewedParkings: List<String> = emptyList(),
    val favoriteParkings: List<String> = emptyList(),
    // val lastLoginTime: Timestamp? = null,
    val wallet: Wallet = Wallet.empty(),
    val personalNotes: Map<String, String> = emptyMap(),
    val isAdmin: Boolean = false
)

/**
 * For information that are only relevant to the session and won't be stored in the database
 *
 * @property profilePictureUrl The URL of the profile picture
 * @property profilePictureUri The URI of the profile picture,(local path on the device)
 */
data class LocalSession(
    val profilePictureUrl: String? = "",
    val profilePictureUri: String? = "",
)

/**
 * Data class representing a user.
 *
 * @property public Public information of the user.
 * @property details Private information of the user.
 * @property localSession Information that are only relevant to the session and won't be stored in
 *   the database
 */
data class User(
    val public: UserPublic,
    val details: UserDetails?,
    val localSession: LocalSession? = null
)

object UserLevelDisplay {
  // Represents a level range with its associated symbol and color
  data class LevelRange(val symbol: String, val color: String)

  // Map of level ranges (0-9, 10-19, etc.) to their display properties
  private val levelRanges =
      mapOf(
          0 to LevelRange("⭒", "#2B2B2B"), // Dark Gray
          10 to LevelRange("☆", "#6B4F4F"), // Even Darker Gray
          20 to LevelRange("✧", "#104E8B"), // Dark Dodger Blue
          30 to LevelRange("✵", "#27408B"), // Dark Royal Blue
          40 to LevelRange("❁", "#006400"), // Dark Green
          50 to LevelRange("❂", "#228B22"), // Forest Green
          60 to LevelRange("ღ", "#FF6347"), // Dark Orange (Tomato)
          70 to LevelRange("დ", "#4B0082"), // Dark Purple (Indigo)
          80 to LevelRange("ლ", "#8B0000"), // Dark Red
          90 to LevelRange("☤", "#B8860B"), // Dark Gold (Goldenrod)
          100 to LevelRange("♔", "rainbow"))

  /** Gets the appropriate level range properties for a given score */
  fun getLevelRange(score: Double): LevelRange {
    val level = score.toInt()
    val rangeStart = ((level / 10) * 10).coerceAtMost(100)
    return levelRanges[rangeStart] ?: LevelRange("⭒", "#2B2B2B") // Default fallback
  }
}
