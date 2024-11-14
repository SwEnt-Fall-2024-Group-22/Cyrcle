package com.github.se.cyrcle.model.user

/**
 * Data class representing the public information of a user.
 *
 * @property userId The unique identifier of the user.
 * @property username The username of the user.
 * @property profilePictureUrl The path to the profile picture of the user on the cloud storage.
 *   (Not implemented)
 */
data class UserPublic(
    val userId: String,
    val username: String,
    val profilePictureUrl: String = "",
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
    val favoriteParkings: List<String> = emptyList(),
    // val lastLoginTime: Timestamp? = null,
    val wallet: Wallet = Wallet.empty()
)

/**
 * Data class representing a user.
 *
 * @property public Public information of the user.
 * @property details Private information of the user.
 */
data class User(val public: UserPublic, val details: UserDetails?)
