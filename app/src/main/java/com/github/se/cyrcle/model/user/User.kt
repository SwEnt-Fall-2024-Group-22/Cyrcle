package com.github.se.cyrcle.model.user

/**
 * Data class representing a user.
 *
 * @property userId Unique identifier of the user.
 * @property username Username of the user.
 * @property firstName First name of the user.
 * @property lastName Last name of the user.
 * @property email Email of the user.
 * @property profilePictureUrl URL of the profile picture of the user.
 * @property favoriteParkings List of unique identifiers of the user's favorite parkings.
 */
data class User(
    val userId: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val profilePictureUrl: String,
    val favoriteParkings: List<String>,
    // val lastLoginTime: Timestamp? = null,
    // val accountCreationDate: Timestamp? = null,
    // val numberOfContributedSpots: Int = 0,
    // val userReputationScore: Int = 0
)
