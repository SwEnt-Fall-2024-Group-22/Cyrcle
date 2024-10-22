package com.github.se.cyrcle.model.user

object UserTestInstances {

  val user1 =
      User(
          userId = "user1",
          username = "john_doe",
          firstName = "John",
          lastName = "Doe",
          email = "john.doe@example.com",
          profilePictureUrl = "https://example.com/profile/john_doe.jpg",
          favoriteParkings = listOf("Test_spot_1", "Test_spot_2"),
          // homeAddress = "123 Main St, New York, NY",
          // lastLoginTime = Timestamp(0,0), // Now using Timestamp
          // accountCreationDate = Timestamp(0,0),
          // numberOfContributedSpots = 5,
          // userReputationScore = 120
      )

  val newUser =
      User(
          userId = "usr",
          username = "new_user",
          firstName = "New",
          lastName = "User",
          email = "newuser@example.com",
          profilePictureUrl = "",
          favoriteParkings = emptyList(),
          // homeAddress = "123 Main St",
          // lastLoginTime = Timestamp(0,0),
          // accountCreationDate = Timestamp(0,0)
      )

  // Act: Update an existing user
  val updatedUser =
      User(
          userId = "usr",
          username = "updated_user",
          firstName = "Updated",
          lastName = "User",
          email = "updateduser@example.com",
          profilePictureUrl = "https://example.com/profile.png",
          favoriteParkings = emptyList(),
          // homeAddress = "456 Updated St",
          // lastLoginTime = Timestamp(0,0),
          // accountCreationDate = Timestamp(0,0)
      )
}
