package com.github.se.cyrcle.model.user

object TestInstancesUser {

  val user1 =
      User(
          public =
              UserPublic(
                  userId = "user1",
                  username = "john_doe",
                  profilePictureUrl = "https://example.com/profile/john_doe.jpg"),
          details =
              UserDetails(
                  firstName = "John",
                  lastName = "Doe",
                  email = "john.doe@example.com",
                  favoriteParkings = listOf("Test_spot_1", "Test_spot_2")))

  val newUser =
      User(
          public = UserPublic(userId = "usr", username = "new_user", profilePictureUrl = ""),
          details =
              UserDetails(
                  firstName = "New",
                  lastName = "User",
                  email = "newuser@example.com",
                  favoriteParkings = emptyList()))

  val updatedUser =
      User(
          public =
              UserPublic(
                  userId = "usr",
                  username = "updated_user",
                  profilePictureUrl = "https://example.com/profile.png"),
          details =
              UserDetails(
                  firstName = "Updated",
                  lastName = "User",
                  email = "updateduser@example.com",
                  favoriteParkings = emptyList()))
}
