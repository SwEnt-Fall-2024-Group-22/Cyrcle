package com.github.se.cyrcle.model.user

object UserTestInstances {

  // Mock Users
  val user1 =
      User(
          userId = "user1",
          username = "john_doe",
          firstName = "John",
          lastName = "Doe",
          email = "john.doe@example.com",
          profilePictureUrl = "https://example.com/profile/john_doe.jpg",
          homeAddress = "123 Main St, New York, NY",
          favoriteParkingSpots = listOf("mockParking1", "mockParking2"),
          // lastKnownLocation = mockLocation1,
          // lastLoginTime = Timestamp(0,0), // Now using Timestamp
          // accountCreationDate = Timestamp(0,0),
          numberOfContributedSpots = 5,
          userReputationScore = 120)

  val newUser =
      User(
          userId = "usr",
          username = "new_user",
          firstName = "New",
          lastName = "User",
          email = "newuser@example.com",
          profilePictureUrl = "",
          homeAddress = "123 Main St",
          favoriteParkingSpots = emptyList(),
          // lastKnownLocation = Location(Point.fromLngLat(0.0, 0.0)),
          // lastLoginTime = Timestamp(0,0),
          // accountCreationDate = Timestamp(0,0)
      )

  // Act: Update an existing user
  val updatedUser =
      User(
          userId = "usr",
          username = "updaed_user",
          firstName = "Updated",
          lastName = "User",
          email = "updateduser@example.com",
          profilePictureUrl = "https://example.com/profile.png",
          homeAddress = "456 Updated St",
          favoriteParkingSpots = emptyList(),
          // lastKnownLocation = Location(Point.fromLngLat(0.0, 0.0)),
          // lastLoginTime = Timestamp(0,0),
          // accountCreationDate = Timestamp(0,0)
      )
}
