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
    // val userReputationScore: Int = 0
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
